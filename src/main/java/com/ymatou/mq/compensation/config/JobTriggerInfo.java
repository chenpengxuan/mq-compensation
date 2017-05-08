/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.compensation.config;

import java.util.List;

/**
 * @author luoshiqian 2017/4/18 14:24
 */
public class JobTriggerInfo {

    private String jobName;

    private List trigger;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public List getTrigger() {
        return trigger;
    }

    public void setTrigger(List trigger) {
        this.trigger = trigger;
    }
}
