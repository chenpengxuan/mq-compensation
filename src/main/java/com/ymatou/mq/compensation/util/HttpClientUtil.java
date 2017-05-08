/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */
package com.ymatou.mq.compensation.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author qianmin 2016年8月2日 下午12:02:25
 *
 */
public class HttpClientUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static Integer CONN_TIME_OUT = 3000;
    public static Integer SOCKET_TIME_OUT = 10000;
    public static Integer DEFAULT_MAX_PER_ROUTE = 40;
    public static Integer MAX_TOTAL = 400;

    public static RequestConfig requestConfig;
    public static HttpClient httpClient;

    static {
        requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONN_TIME_OUT)
                .setSocketTimeout(SOCKET_TIME_OUT).build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        cm.setMaxTotal(MAX_TOTAL);

        httpClient = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
    }
    
    /**
     * 发送Get请求
     * 
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException 
     */
    public static String sendGet(String url, Map<String, Object> paramsMap) throws Exception{
        String result = null;
        
        HttpGet httpGet = new HttpGet(url);
        List<NameValuePair> params = initParams(paramsMap);
        String str = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        
        try {
            httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + str));
            logger.info("executing request" + httpGet.getRequestLine());
            logger.info("request header: " + Arrays.toString(httpGet.getAllHeaders()));
            
            HttpResponse response = httpClient.execute(httpGet);
            
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            logger.info("response message:" + result);
        } finally {
            httpGet.releaseConnection();
        }

        return result;
    }
    
    public static String sendPost(String url, Map<String, Object> paramsMap, String contentType) throws IOException {
        String result = null;

        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> body = initParams(paramsMap);
        UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(body, "UTF-8");
        httpPost.setEntity(postEntity); // set request body
        httpPost.addHeader("Content-Type", contentType); // 设置body类型
        logger.info("executing request" + httpPost.getRequestLine());
        logger.info("request header: " + Arrays.toString(httpPost.getAllHeaders()));
        logger.info("request body: " + body);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            logger.info("response message:" + result);
        } finally {
            httpPost.releaseConnection();
        }

        return result;
    }

    /**
     * 发送Post请求
     * 
     * @param url
     * @param body
     * @param contentType
     * @return
     * @throws IOException
     */
    public static String sendPost(String url, String body, String contentType) throws Exception {
        String result = null;

        HttpPost httpPost = new HttpPost(url);
        StringEntity postEntity = new StringEntity(body, "UTF-8");
        httpPost.setEntity(postEntity); // set request body
        httpPost.addHeader("Content-Type", contentType); // 设置body类型

        logger.info("executing request" + httpPost.getRequestLine());
        logger.info("request header: " + Arrays.toString(httpPost.getAllHeaders()));
        logger.info("request body: " + body);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            logger.info("response message:" + result);
        } finally {
            httpPost.releaseConnection();
        }

        return result;
    }
    
    /**
     * 发送post请求， 获取应答码
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public static int sendPost(String url) throws Exception {
        int statusCode = -1;
        HttpPost httpPost = new HttpPost(url);
        try {
            logger.info("executing request" + httpPost.getRequestLine());
            logger.info("request header: " + Arrays.toString(httpPost.getAllHeaders()));

            HttpResponse response = httpClient.execute(httpPost);

            statusCode = response.getStatusLine().getStatusCode();
            logger.info("response status code:" + statusCode);
        } finally {
            httpPost.releaseConnection();
        }

        return statusCode;
    }
    
    private static List<NameValuePair> initParams(Map<String, Object> paramsMap) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (paramsMap == null)
            return params;
        
        Iterator<String> iterator = paramsMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            params.add(new BasicNameValuePair(key, String.valueOf(paramsMap.get(key))));
        }
        
        return params;
    }
}
