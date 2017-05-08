/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.config;

import static com.ymatou.mq.infrastructure.service.MessageConfigService.callbackConfigMap;
import static com.ymatou.mq.infrastructure.service.MessageConfigService.needRemoveCallBackConfigList;

import javax.annotation.PostConstruct;

import com.ymatou.mq.compensation.service.job.EnsureIndexJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ymatou.mq.compensation.service.SchedulerService;
import com.ymatou.mq.compensation.service.job.CallbackJob;
import com.ymatou.mq.infrastructure.service.MessageConfigService;
import com.ymatou.mq.infrastructure.support.ConfigReloadListener;

/**
 * @author luoshiqian 2017/4/12 17:43
 */
@Component
public class SchedulerConfig implements ConfigReloadListener {

    @Autowired
    private SchedulerService schedulerService;
    @Autowired
    private MessageConfigService messageConfigService;

    @Autowired
    private BizConfig bizConfig;

    @PostConstruct
    public void init() {
        messageConfigService.addConfigCacheListener(this);

        // init mongo index scheduler
        schedulerService.addJob(EnsureIndexJob.class, "EnsureIndexJobName",
                bizConfig.getMongoIndexCronExpr());

        // 初始化所有scheduler
        initCallbackScheduler();
    }

    private void initCallbackScheduler(){
        callbackConfigMap.values().stream().forEach(callbackConfig -> {
            if(callbackConfig.getEnable()){
                schedulerService.addJob(CallbackJob.class, callbackConfig.getCallbackKey(),
                        bizConfig.getCronExpr());
            }else {
                schedulerService.removeScheduler(callbackConfig.getCallbackKey());
            }
        });
    }


    @Override
    @Transactional
    public void callback() {

        schedulerService.addJob(EnsureIndexJob.class, "EnsureIndexJobName",
                bizConfig.getMongoIndexCronExpr());

        initCallbackScheduler();

        needRemoveCallBackConfigList.stream().forEach(callbackConfig -> {
            schedulerService.removeScheduler(callbackConfig.getCallbackKey());
        });
    }
}
