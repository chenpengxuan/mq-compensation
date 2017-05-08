/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.compensation.config;

import com.baidu.disconf.client.DisconfMgrBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisconfMgr {

    @Bean(name = "disconfMgrBean", destroyMethod = "destroy")
    public DisconfMgrBean disconfMgrBean() {
        DisconfMgrBean disconfMgrBean = new DisconfMgrBean();
        disconfMgrBean.setScanPackage("com.ymatou.mq");

        return disconfMgrBean;
    }
}
