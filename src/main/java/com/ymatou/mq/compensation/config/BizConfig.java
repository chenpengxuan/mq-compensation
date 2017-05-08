/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.config;

import org.springframework.stereotype.Component;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;

@Component
@DisconfFile(fileName = "biz.properties")
public class BizConfig {


    private int serverPort;

    private String performanceServerUrl;

    private String cronExpr;

    private String mongoIndexCronExpr;



    @DisconfFileItem(name = "server.port")
    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }


    @DisconfFileItem(name = "biz.cronExpr")
    public String getCronExpr() {
        return cronExpr;
    }

    public void setCronExpr(String cronExpr) {
        this.cronExpr = cronExpr;
    }


    @DisconfFileItem(name = "biz.mongoIndexCronExpr")
    public String getMongoIndexCronExpr() {
        return mongoIndexCronExpr;
    }

    public void setMongoIndexCronExpr(String mongoIndexCronExpr) {
        this.mongoIndexCronExpr = mongoIndexCronExpr;
    }

    @DisconfFileItem(name = "performance.server.url")
    public String getPerformanceServerUrl() {
        return performanceServerUrl;
    }

    public void setPerformanceServerUrl(String performanceServerUrl) {
        this.performanceServerUrl = performanceServerUrl;
    }
}
