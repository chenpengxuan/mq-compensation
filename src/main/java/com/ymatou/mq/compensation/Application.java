/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.compensation;

import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import org.mongodb.morphia.logging.slf4j.SLF4JLoggerImplFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;


@EnableAspectJAutoProxy(proxyTargetClass = true)
@Configuration
@EnableAutoConfiguration
        (exclude = {DataSourceAutoConfiguration.class ,DataSourceTransactionManagerAutoConfiguration.class,
                JpaBaseConfiguration.class, HibernateJpaAutoConfiguration.class, PersistenceExceptionTranslationAutoConfiguration.class,
                MongoAutoConfiguration.class,MongoDataAutoConfiguration.class
        })
@ComponentScan(basePackages = "com.ymatou.mq")
public class Application {

    public static void main(String[] args) {
        MorphiaLoggerFactory.registerLogger(SLF4JLoggerImplFactory.class);
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    }

}
