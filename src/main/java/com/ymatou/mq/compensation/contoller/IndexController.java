/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.contoller;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ymatou.mq.compensation.config.JobTriggerInfo;
import com.ymatou.mq.compensation.util.Constants;
import com.ymatou.mq.compensation.util.Utils;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ymatou.mq.compensation.service.job.EnsureIndexExecutor;


@RestController
public class IndexController {

    @Autowired
    private EnsureIndexExecutor ensureIndexExecutor;
    @Autowired
    private Scheduler scheduler;

    @RequestMapping("/version")
    public String version() {
        return Utils.version();
    }

    @RequestMapping("/warmup")
    public String warmup() {
        return "ok";
    }


    @RequestMapping("/index")
    public String ensureIndex() {
        ensureIndexExecutor.ensureIndex();
        return "success";
    }

    @RequestMapping("/status")
    public Object status() throws Exception{
        Map<String,Object> map = Maps.newHashMap();
        map.put("meta",scheduler.getMetaData());

        Set<JobKey> jobKeys =  scheduler.getJobKeys(null);

        List<JobTriggerInfo> jobTriggerInfoList = Lists.newArrayList();
        for(JobKey jobKey: jobKeys){
            JobTriggerInfo jobTriggerInfo = new JobTriggerInfo();
            jobTriggerInfo.setJobName(jobKey.getName());
            jobTriggerInfo.setTrigger(scheduler.getTriggersOfJob(jobKey));
            jobTriggerInfoList.add(jobTriggerInfo);
        }
        map.put("triggers",jobTriggerInfoList);
        return JSON.toJSONStringWithDateFormat(map, Constants.DATE_FORMAT_YMD_HMS);
    }
}
