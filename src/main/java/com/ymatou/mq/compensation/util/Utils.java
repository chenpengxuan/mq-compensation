/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 * @author luoshiqian 2017/3/9 13:08
 */
public class Utils {

    public static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * 返回字符串形式的异常完整堆栈
     * 
     * @param throwable
     * @return 空字符串<tt>""</tt>，如果输入是<tt>null</tt>
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    public static final String[] DATE_PATTERNS = new String[] {
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH", "yyyy-MM-dd", "yyyy-MM",
            "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM/dd HH", "yyyy/MM/dd", "yyyy/MM"
    };

    public static Date parseDate(String dateStr) {
        try {
            Date date = DateUtils.parseDate(dateStr,DATE_PATTERNS);
            return date;
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static String version() {
        try {
            Resource resource = new ClassPathResource("version.txt");
            return StreamUtils.copyToString(resource.getInputStream(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            logger.error("Failed to read version", e);
            return "Failed to read version:" + e.getMessage();
        }
    }
}
