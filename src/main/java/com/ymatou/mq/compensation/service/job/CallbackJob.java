/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.service.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ymatou.mq.infrastructure.util.SpringContextHolder;

/**
 * @author luoshiqian 2017/4/12 18:12
 */
@DisallowConcurrentExecution
public class CallbackJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(CallbackJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        CallbackJobExecutor executor = SpringContextHolder.getBean(CallbackJobExecutor.class);
        String callbackKey = jobExecutionContext.getJobDetail().getKey().getName();
        logger.info("begin CallbackJob:{}",callbackKey);
        try {
            executor.execute(callbackKey);
        } catch (Exception e) {
            logger.error("exec CallbackJob error", e);
        }
    }
}
