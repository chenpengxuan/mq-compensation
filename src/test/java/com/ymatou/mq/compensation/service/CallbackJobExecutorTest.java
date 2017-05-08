/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.compensation.service;

import static com.ymatou.mq.infrastructure.service.MessageConfigService.callbackConfigMap;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ymatou.mq.compensation.BaseTest;
import com.ymatou.mq.compensation.service.job.CallbackJobExecutor;
import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.Message;
import com.ymatou.mq.infrastructure.model.MessageCompensate;
import com.ymatou.mq.infrastructure.model.MessageDispatchDetail;
import com.ymatou.mq.infrastructure.repository.MessageCompensateRepository;
import com.ymatou.mq.infrastructure.repository.MessageDispatchDetailRepository;
import com.ymatou.mq.infrastructure.repository.MessageRepository;
import com.ymatou.mq.infrastructure.service.MessageService;
import com.ymatou.mq.infrastructure.support.enums.CompensateStatusEnum;
import com.ymatou.mq.infrastructure.support.enums.DispatchStatusEnum;

/**
 * @author luoshiqian 2017/4/14 14:39
 */
public class CallbackJobExecutorTest extends BaseTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageCompensateRepository compensateRepository;

    @Autowired
    private MessageDispatchDetailRepository messageDispatchDetailRepository;

    String callbackKey = "testdashen_publishfailendTime_c0";
    @Autowired
    private CallbackJobExecutor callbackJobExecutor;

    @Test
    public void testCheckToCompensate()throws Exception {
        Message message = buildMessageData();
        callbackJobExecutor.checkToCompensate(callbackKey);

        CallbackConfig callbackConfig = callbackConfigMap.get(callbackKey);

        MessageDispatchDetail messageDispatchDetail = messageDispatchDetailRepository.findById(
                message.getId() + "_" + callbackKey, message.getId(),
                callbackConfig.getQueueConfig().getAppConfig().getAppId(), callbackConfig.getQueueConfig().getCode());

        assertEquals(messageDispatchDetail.getStatus(), DispatchStatusEnum.COMPENSATE.getCode());

        MessageCompensate messageCompensate = compensateRepository
                .findByQueueCodeAndId(callbackConfig.getQueueConfig().getAppConfig().getAppId(),
                        callbackConfig.getQueueConfig().getCode(), message.getId() + "_" + callbackKey);
        assertEquals(messageCompensate.getStatus(), CompensateStatusEnum.COMPENSATE.getCode());
    }

    @Test
    public void testCompensate()throws Exception{
        callbackJobExecutor.compensateMessages(callbackKey);

        TimeUnit.SECONDS.sleep(305);
    }




    public Message buildMessageData(){
        String id = new ObjectId().toHexString();
        Message message = new Message();
        message.setId(id);
        message.setAppId("testdashen");
        message.setBizId("test233");
        message.setBody("{\"orderId\":1321321}");
        message.setClientIp("127.0.0.1");
        message.setQueueCode("publishfailendTime");
        message.setRecvIp("127.0.0.1");
        message.setCreateTime(DateTime.now().minusMinutes(15).toDate());

        messageService.saveMessage(message);

        Message dbMessage = messageRepository.getById("testdashen","publishfailendTime",id);

        Assert.assertNotNull(dbMessage);

        return message;
    }

}
