/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.compensation.config;

import com.ymatou.performancemonitorclient.PerformanceMonitorAdvice;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 性能监控配置
 *
 * @author luoshiqian
 */
@Aspect
@Configuration
public class PerformanceConfig {


    @Bean(name = "performanceMonitorAdvice")
    public PerformanceMonitorAdvice performanceMonitorAdvice(BizConfig bizConfig) {
        PerformanceMonitorAdvice performanceMonitorAdvice = new PerformanceMonitorAdvice();
        performanceMonitorAdvice.setAppId("compensation-mq.iapi.ymatou.com");
        performanceMonitorAdvice.setServerUrl(bizConfig.getPerformanceServerUrl());
        return performanceMonitorAdvice;
    }

    @Bean(name = "performancePointcut")
    public AspectJExpressionPointcut aspectJExpressionPointcut() {
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();

        aspectJExpressionPointcut.setExpression(
                "execution(* com.ymatou.mq.infrastructure.repository.*Repository.*(..))"
                        + "|| execution(* com.ymatou.mq.compensation.service..*.*(..))"
        );

        return aspectJExpressionPointcut;
    }


    /**
     * 对应xml
     * <aop:config>
     * <aop:advisor advice-ref="performanceMonitorAdvice"
     * pointcut-ref="performancePointcut" />
     * </aop:config>
     *
     * @return
     */
    @Bean
    public Advisor performanceMonitorAdvisor(BizConfig bizConfig) {
        return new DefaultPointcutAdvisor(aspectJExpressionPointcut(), performanceMonitorAdvice(bizConfig));
    }

}
