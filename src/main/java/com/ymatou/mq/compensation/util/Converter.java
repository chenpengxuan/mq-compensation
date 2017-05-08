/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.mq.compensation.util;



import org.springframework.beans.BeanUtils;

/**
 * @author tuwenjie 2016年5月10日 下午4:51:07
 *
 */
public abstract class Converter {

    /**
     * 将某个对象转换为指定类型的对象
     * 如果原对象与目标对象有相同的属性，则这些属性值自动填充到目标对象
     * 
     * @param source 原对象
     * @param targetClaz 目标类型，<em>该类必须有默认构造函数</em>
     * @param ignoreProperties 忽略的属性名
     * @return
     */
    public static <T> T convert(Object source, Class<T> targetClaz, String... ignoreProperties) {

        assert targetClaz != null;

        if (source == null) {
            return null;
        }
        T result = null;
        try {
            result = targetClaz.newInstance();
        } catch (Exception t) {
            throw new RuntimeException("Failed to new instance of class " + targetClaz, t);
        }

        convert(source,result,ignoreProperties);

        return result;
    }


    public static <T> void convert(Object source, T t, String... ignoreProperties) {

        BeanUtils.copyProperties(source, t, ignoreProperties);

    }

}
