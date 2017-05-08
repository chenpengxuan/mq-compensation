/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.service.job;

import static com.ymatou.mq.infrastructure.service.MessageConfigService.callbackConfigMap;
import static com.ymatou.mq.infrastructure.util.MessageHelper.fromMessageDispatchDetail;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.ymatou.mq.compensation.service.MessageCompensateService;
import com.ymatou.mq.compensation.service.MessageDispatchDetailService;
import com.ymatou.mq.infrastructure.model.*;
import com.ymatou.mq.infrastructure.repository.MessageRepository;
import com.ymatou.mq.infrastructure.service.AsyncHttpInvokeService;
import com.ymatou.mq.infrastructure.service.HttpInvokeResultService;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.infrastructure.support.ErrorReportClient;
import com.ymatou.mq.infrastructure.support.enums.CallbackFromEnum;
import com.ymatou.mq.infrastructure.support.enums.CompensateFromEnum;
import com.ymatou.mq.infrastructure.support.enums.CompensateStatusEnum;
import com.ymatou.mq.infrastructure.support.enums.DispatchStatusEnum;
import com.ymatou.mq.infrastructure.util.MessageHelper;
import com.ymatou.mq.infrastructure.util.RetryPolicyUtils;

/**
 * 处理补单回调
 * 
 * @author luoshiqian 2017/4/12 18:13
 */
@Component
public class CallbackJobExecutor implements HttpInvokeResultService {

    public static final Logger logger = LoggerFactory.getLogger(CallbackJobExecutor.class);

    @Autowired
    private MessageConfigService messageConfigService;
    @Autowired
    private MessageCompensateService messageCompensateService;
    @Autowired
    private MessageDispatchDetailService dispatchDetailService;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ErrorReportClient errorReportClient;


    public void execute(String callbackKey) throws Exception {

        CallbackConfig callbackConfig = callbackConfigMap.get(callbackKey);
        if (callbackConfig == null) {// 可能被删除了
            return;
        }
        // checkToCompensate
        checkToCompensate(callbackKey);

        // compensateMessages
        compensateMessages(callbackKey);
    }

    public void compensateMessages(String callbackKey) throws Exception{
        CallbackConfig callbackConfig = callbackConfigMap.get(callbackKey);

        String appId = callbackConfig.getQueueConfig().getAppConfig().getAppId();
        String queueCode = callbackConfig.getQueueConfig().getCode();

        // 查找补单库数据 开始补单
        List<MessageCompensate> compensateList = messageCompensateService.findCompensate(appId,queueCode, callbackKey);
        if (callbackConfig.isCallbackEnable()) {// 启用补单回调
            for (MessageCompensate compensate : compensateList) {
                CallbackMessage message = MessageHelper.fromCompensation(compensate, CallbackFromEnum.COMPENSATE);

                if(!isCompenateOverTimeWithUpdate(message,callbackConfig,false)){ //没有超过重试时间
                    new AsyncHttpInvokeService(message, callbackConfig, this).send();
                }
            }
        } else {
            // 不启用补单回调 修改补单状态为失败
            List<String> compensateIdList =
                    compensateList.stream().map(MessageCompensate::getId).collect(Collectors.toList());

            if(!CollectionUtils.isEmpty(compensateIdList)){

                for (MessageCompensate compensate : compensateList) {
                    CallbackMessage message = MessageHelper.fromCompensation(compensate, CallbackFromEnum.COMPENSATE);
                    dispatchDetailService.updateStatus(message, DispatchStatusEnum.FAIL, Lists.newArrayList(
                            DispatchStatusEnum.SUCCESS.getCode(),DispatchStatusEnum.FAIL.getCode()));
                }
                messageCompensateService.updateStatusByMulti(appId,queueCode, compensateIdList, CompensateStatusEnum.FAIL,
                        "补单未启用、失败");
            }
        }
    }


    public void checkToCompensate(String callbackKey) {
        CallbackConfig callbackConfig = callbackConfigMap.get(callbackKey);

        String appId = callbackConfig.getQueueConfig().getAppConfig().getAppId();
        String queueCode = callbackConfig.getQueueConfig().getCode();

        int checkCompensateDelay = callbackConfig.getQueueConfig().getCheckCompensateDelay();
        int checkCompensateTimeSpan = callbackConfig.getQueueConfig().getCheckCompensateTimeSpan();
        if (checkCompensateDelay != 0 && checkCompensateTimeSpan != 0) {
            List<MessageDispatchDetail> needCompensateList =
                    dispatchDetailService.findNeedCompensate(appId, queueCode, callbackConfig.getCallbackKey(),
                            checkCompensateDelay, checkCompensateTimeSpan);
            if(!CollectionUtils.isEmpty(needCompensateList)){
                logger.error("checkToCompensate callbackKey:{}, appId:{},queueCode:{},nums:{} ", callbackKey, appId,
                        queueCode, needCompensateList.size());
                // 先保存补单数据、再修改分发明细状态为补单中
                for (MessageDispatchDetail dispatchDetail : needCompensateList) {
                    Message message = messageRepository.getById(appId, queueCode, dispatchDetail.getMsgId());

                    // 保存补单数据
                    MessageCompensate messageCompensate =
                            fromMessageDispatchDetail(dispatchDetail, CompensateFromEnum.COMPENSATE);
                    messageCompensate.setBody(message.getBody());

                    messageCompensateService.saveCompensate(messageCompensate);

                    // 分发明细状态为补单中 下次就不会再检测了
                    CallbackMessage callbackMessage =
                            MessageHelper.fromCompensation(messageCompensate, CallbackFromEnum.COMPENSATE);
                    dispatchDetailService.updateStatus(callbackMessage, DispatchStatusEnum.COMPENSATE);
                }
            }
        } else {
            logger.info(
                    "appId:{},queueCode:{},callbackKey:{} closed checkToCompensate ,checkCompensateDelay:{},checkCompensateTimeSpan:{}",
                    appId, queueCode, callbackConfig.getCallbackKey(), checkCompensateDelay, checkCompensateTimeSpan);
        }
    }


    /**
     * 回调成功
     * 
     * @param message
     * @param callbackConfig
     */
    @Override
    public void onInvokeSuccess(CallbackMessage message, CallbackConfig callbackConfig) {

        try {
            // 修改消息分发表状态为成功
            dispatchDetailService.updateStatus(message, DispatchStatusEnum.SUCCESS);

            // 修改补单表状态为成功
            messageCompensateService.updateStatus(message, CompensateStatusEnum.SUCCESS,true);
        } finally {
            logger.info("callback success,bizId:{},callbackKey:{},url:{},req:{},resp:{}.", message.getBizId(),
                    callbackConfig.getCallbackKey(), callbackConfig.getUrl(), message.getBody(), message.getResponse());
        }
    }

    /**
     * 回调失败
     * 
     * @param message
     * @param callbackConfig
     */
    @Override
    public void onInvokeFail(CallbackMessage message, CallbackConfig callbackConfig) {

        // 补单表 修改补单次数、updatetime
        try {
            if(!isCompenateOverTimeWithUpdate(message,callbackConfig,true)){
                //没有超过重试时间
                // 找到下次补单时间
                Date nextTime = RetryPolicyUtils.getNextTime(callbackConfig.getRetryPolicy(), message.getRetryNums());

                if (null == nextTime) {
                    message.setResponse("超出重试次数、失败|上次结果:" + message.getResponse());
                    dispatchDetailService.updateStatus(message, DispatchStatusEnum.FAIL);

                    messageCompensateService.updateStatus(message, CompensateStatusEnum.FAIL,true);

                } else {
                    // 重新设置下次补单时间
                    message.setNextTime(nextTime);
                    messageCompensateService.updateStatus(message,CompensateStatusEnum.COMPENSATE,true);
                }
                errorReportClient.sendErrorReport(message,callbackConfig);
            }
        } finally {
            logger.error("callback fail,bizId:{},callbackKey:{},url:{},req:{},resp:{}.", message.getBizId(),
                    callbackConfig.getCallbackKey(), callbackConfig.getUrl(), message.getBody(), message.getResponse());
        }


    }

    /**
     * 是否超过重试时间
     *  超过重试时间修改状态为失败
     * @param message
     * @param callbackConfig
     * @return
     */
    private boolean isCompenateOverTimeWithUpdate(CallbackMessage message,CallbackConfig callbackConfig,boolean isIncreaseCount){
        int duration =
                Minutes.minutesBetween(new DateTime(message.getCreateTime().getTime()), DateTime.now()).getMinutes();
        if (duration > callbackConfig.getRetryTimeout()) {
            // 超出重试时间

            message.setResponse("超出重试时间、失败|上次结果:" + message.getResponse());

            // 消息分发状态为失败
            dispatchDetailService.updateStatus(message, DispatchStatusEnum.FAIL,Lists.newArrayList(
                    DispatchStatusEnum.SUCCESS.getCode(),DispatchStatusEnum.FAIL.getCode()));

            // 修改补单状态为失败
            messageCompensateService.updateStatus(message, CompensateStatusEnum.FAIL,isIncreaseCount);

            errorReportClient.sendErrorReport(message,callbackConfig);

            return true;
        }
        return false;
    }


}
