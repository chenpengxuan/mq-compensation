/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */
package com.ymatou.mq.compensation.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.baidu.disconf.client.DisconfMgrBean;
import com.baidu.disconf.client.config.DisClientConfig;
import com.ymatou.mq.compensation.util.Constants;


/**
 * 数据库定时任务配置
 * 
 * @author qianmin 2016年8月18日 下午3:53:16
 *
 */
@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean mqCompensation(DataSource dataSource, DisconfMgrBean disconfMgrBean) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setConfigLocation(new ClassPathResource("quartz.properties"));
        schedulerFactoryBean.setStartupDelay(20);

        if (DisClientConfig.getInstance().ENV.equals(Constants.ENV_STG)) {
            schedulerFactoryBean.setAutoStartup(false);
        }
        return schedulerFactoryBean;
    }


}
