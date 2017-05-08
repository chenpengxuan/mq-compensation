/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.mq.compensation.service;

import org.quartz.Job;
import org.quartz.SchedulerException;

import java.util.Date;

/**
 * 
 * @author qianmin 2016年8月18日 下午3:03:48
 *
 */
public interface SchedulerService {

    void addJob(Class<? extends Job> job, String jobName, String cronExpression);

    void modifyScheduler(String jobName, String cronExpression) throws SchedulerException;

    void pauseScheduler(String jobName) throws SchedulerException;

    void resumeScheduler(String jobName) throws SchedulerException;

    void removeScheduler(String jobName);

    Date getNextFireTime(String jobName) throws SchedulerException;
}
