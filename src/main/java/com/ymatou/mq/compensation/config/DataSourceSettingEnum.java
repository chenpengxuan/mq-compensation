/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.compensation.config;

/**
 * @author luoshiqian 2016/8/18 10:44
 */
public enum DataSourceSettingEnum {

    initialSize("20"),

    minIdle("5"),

    maxActive("100"),

    /**
     * 配置获取连接等待超时的时间
     */
    maxWait("60000"),
    /**
     * 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
     */
    timeBetweenEvictionRunsMillis("60000"),

    /**
     * 配置一个连接在池中最小生存的时间，单位是毫秒
     */
    minEvictableIdleTimeMillis("300000"),

    validationQuery("SELECT 'x'"),

    testWhileIdle("true"),

    testOnBorrow("false"),
    /**
     * datasource 单位秒
     * 查询timeout throws SQLException;
     */
    queryTimeout("30");

    String value;

    DataSourceSettingEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
