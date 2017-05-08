/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
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
 * @author luoshiqian 2017/4/17 13:59
 */
@DisallowConcurrentExecution
public class EnsureIndexJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(EnsureIndexJob.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("begin ensureIndex");
        EnsureIndexExecutor executor = SpringContextHolder.getBean(EnsureIndexExecutor.class);

        try {
            executor.ensureIndex();
        } catch (Exception e) {
            logger.error("exec EnsureIndexJob error", e);
        }
    }
}
