/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * @author luoshiqian 2017/4/12 17:37
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(DbProps dbProps) {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbProps.getDriver());
        dataSource.setUrl(dbProps.getUrl());
        dataSource.setUsername(dbProps.getUsername());
        dataSource.setPassword(dbProps.getPassword());
        dataSource.setInitialSize(dbProps.getInitialSize());
        dataSource.setMinIdle(dbProps.getMinIdle());
        dataSource.setMaxActive(dbProps.getMaxActive());

        dataSource.setTimeBetweenConnectErrorMillis(
                Integer.valueOf(DataSourceSettingEnum.timeBetweenEvictionRunsMillis.getValue()));
        dataSource.setMinEvictableIdleTimeMillis(
                Integer.valueOf(DataSourceSettingEnum.minEvictableIdleTimeMillis.getValue()));
        dataSource.setValidationQuery(DataSourceSettingEnum.validationQuery.getValue());
        dataSource.setTestWhileIdle(Boolean.valueOf(DataSourceSettingEnum.testWhileIdle.getValue()));
        dataSource.setTestOnBorrow(Boolean.valueOf(DataSourceSettingEnum.testOnBorrow.getValue()));
        dataSource.setDefaultAutoCommit(false);

        return dataSource;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DbProps dbProps) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource(dbProps));
        return transactionManager;
    }

    @Bean(name = "transactionTemplate")
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate;
    }
}
