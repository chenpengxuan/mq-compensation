/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.service.job;

import static com.ymatou.mq.infrastructure.util.MongoHelper.*;

import java.util.Date;

import com.ymatou.mq.compensation.service.MessageCompensateService;
import com.ymatou.mq.compensation.service.MessageDispatchDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.mq.infrastructure.repository.AppConfigRepository;
import com.ymatou.mq.infrastructure.repository.MessageRepository;


/**
 * @author luoshiqian 2017/4/17 13:59
 */
@Component
public class EnsureIndexExecutor {

    @Autowired
    private MessageCompensateService compensateService;

    @Autowired
    private MessageDispatchDetailService messageDispatchDetailService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AppConfigRepository appConfigRepository;

    public void ensureIndex() {


        Date date = new Date();
        appConfigRepository.getAllAppConfig().stream().forEach(appConfig -> {
            String dbName = getDbNameByDate(appConfig.getAppId(), date);
            appConfig.getMessageCfgList().forEach(queueConfig -> {
                String messageColName = getMessageCollectionName(queueConfig.getCode());
                messageRepository.ensureIndex(dbName, messageColName);

                String detailColName = getMessageDetailCollectionName(queueConfig.getCode());
                messageDispatchDetailService.ensureIndex(dbName, detailColName);

                String compColName = getMessageCompensateCollectionName(appConfig.getAppId(),queueConfig.getCode());
                compensateService.ensureIndex(compColName);
            });
        });
    }


}
