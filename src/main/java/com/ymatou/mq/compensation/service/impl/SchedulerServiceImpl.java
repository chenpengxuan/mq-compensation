/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.mq.compensation.service.impl;

import com.ymatou.mq.compensation.service.SchedulerService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * 
 * @author qianmin 2016年8月18日 下午3:04:02
 *
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Autowired
    private Scheduler scheduler;

    @Override
    @Transactional
    public void addJob(Class<? extends Job> job, String jobName, String cronExpression){
        try {
            List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(new JobKey(jobName));
            if (triggerList == null || triggerList.isEmpty()) {
                JobDetail jobDetail = JobBuilder.newJob(job)
                        .withIdentity(jobName)
                        // .storeDurably(false) //Job是非持久性的，若没有活动的Trigger与之相关联，该Job会从Scheduler中删除掉
                        // .requestRecovery(true)
                        // //Scheduler非正常停止(进程停止或机器关闭等)时，Scheduler再次启动时，该Job会重新执行一次
                        .build();
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                                .withMisfireHandlingInstructionFireAndProceed())
                        .build();
                scheduler.scheduleJob(jobDetail, trigger);
            }
            else {
                modifyScheduler(jobName, cronExpression);
            }
        } catch (SchedulerException e) {
            logger.warn("add job warm some other has done", e);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void modifyScheduler(String jobName, String cronExpression) throws SchedulerException {
        // 获取job的原trigger
        List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(new JobKey(jobName));
        Trigger oldTrigger = triggerList.get(0); // job与trigger一一对应， job有且只有一个trigger

        String oldExpr = ((CronTrigger)oldTrigger).getCronExpression();
        if(!cronExpression.equals(oldExpr)){

            // 借助于原trigger相关联的triggerBuilder修改trigger
            TriggerBuilder tb = oldTrigger.getTriggerBuilder();

            Trigger newTrigger = tb.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

            scheduler.rescheduleJob(oldTrigger.getKey(), newTrigger);
        }
    }

    @Override
    @Transactional
    public void pauseScheduler(String jobName) throws SchedulerException {
        scheduler.pauseJob(new JobKey(jobName));
    }

    @Override
    @Transactional
    public void resumeScheduler(String jobName) throws SchedulerException {
        scheduler.resumeJob(new JobKey(jobName));
    }

    @Override
    @Transactional
    public void removeScheduler(String jobName){
        JobKey jobKey = new JobKey(jobName);
        try {
            if(scheduler.checkExists(jobKey)){
                scheduler.pauseJob(jobKey);
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            logger.warn("removeScheduler warm some other has done", e);
        }

    }

    @Override
    public Date getNextFireTime(String jobName) throws SchedulerException {
        List<? extends Trigger> triggerList = scheduler.getTriggersOfJob(new JobKey(jobName));
        Trigger oldTrigger = triggerList.get(0);
        return oldTrigger.getNextFireTime();
    }
}
