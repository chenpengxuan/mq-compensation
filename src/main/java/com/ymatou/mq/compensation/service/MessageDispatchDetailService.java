/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.compensation.service;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.nin;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static com.ymatou.mq.infrastructure.util.MongoHelper.*;

import java.util.Date;
import java.util.List;

import org.bson.conversions.Bson;
import org.joda.time.DateTime;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.ymatou.mq.infrastructure.model.CallbackMessage;
import com.ymatou.mq.infrastructure.model.MessageDispatchDetail;
import com.ymatou.mq.infrastructure.repository.MessageDispatchDetailRepository;
import com.ymatou.mq.infrastructure.support.enums.DispatchStatusEnum;
import com.ymatou.mq.infrastructure.util.NetUtil;

/**
 * @author luoshiqian 2017/5/2 16:45
 */
@Component
public class MessageDispatchDetailService {

    @Autowired
    private MessageDispatchDetailRepository dispatchDetailRepository;

    public void updateStatus(CallbackMessage message, DispatchStatusEnum statusEnum) {
        updateStatus(message, statusEnum, Lists.newArrayList());
    }

    public void updateStatus(CallbackMessage message, DispatchStatusEnum statusEnum, List<Integer> notInStatus) {

        String dbName = getDbName(message.getAppId(), message.getId());
        String collectionName = getMessageDetailCollectionName(message.getQueueCode());
        Bson doc = eq("_id", buildMessageDetailId(message.getId(), message.getCallbackKey()));

        if (!CollectionUtils.isEmpty(notInStatus)) {
            doc = combine(doc, nin("status", notInStatus));
        }

        Bson set = combine(
                set("status", statusEnum.getCode()),
                set("lastFrom", message.getLastFrom().getCode()),
                set("lastResp", message.getResponse()),
                set("lastTime", message.getResponseTime()),
                set("dealIp", NetUtil.getHostIp()),
                set("updateTime", new Date()));
        dispatchDetailRepository.updateOne(dbName, collectionName, doc, set, false);
    }

    /**
     * 查找需要补单数据
     *
     * @param appId
     * @param queueCode
     * @param callbackKey
     * @param delayMinutes
     * @param spanHours
     */
    public List<MessageDispatchDetail> findNeedCompensate(String appId, String queueCode, String callbackKey,
            int delayMinutes, int spanHours) {
        DateTime now = DateTime.now();

        DateTime startTime = now.minusHours(spanHours);
        DateTime endTime = now.minusMinutes(delayMinutes);

        // 以结束时间做为当前月找查db
        String dbName = getDbNameByDate(appId, endTime.toDate());
        String collectionName = getMessageDetailCollectionName(queueCode);
        Query<MessageDispatchDetail> query =
                dispatchDetailRepository.newQuery(MessageDispatchDetail.class, dbName, collectionName,
                        ReadPreference.secondaryPreferred());
        query.and(
                query.criteria("consumerId").equal(callbackKey),
                query.criteria("status").equal(DispatchStatusEnum.INIT.getCode()),
                query.criteria("createTime").greaterThanOrEq(startTime.toDate()),
                query.criteria("createTime").lessThanOrEq(endTime.toDate()));
        List<MessageDispatchDetail> curMonthList = query.limit(100).asList();

        // 如果跨月了，需要到开始时间所在月份db查找
        if (startTime.getMonthOfYear() != endTime.getMonthOfYear()) {
            String lastMonthDbName = getDbNameByDate(appId, startTime.toDate());
            Query<MessageDispatchDetail> lastMonthQuery =
                    dispatchDetailRepository.newQuery(MessageDispatchDetail.class, lastMonthDbName, collectionName,
                            ReadPreference.secondaryPreferred());
            lastMonthQuery.and(
                    lastMonthQuery.criteria("consumerId").equal(callbackKey),
                    lastMonthQuery.criteria("status").equal(DispatchStatusEnum.INIT.getCode()),
                    lastMonthQuery.criteria("createTime").greaterThanOrEq(startTime.toDate()),
                    lastMonthQuery.criteria("createTime").lessThanOrEq(endTime.toDate()));
            List<MessageDispatchDetail> lastMonthList = lastMonthQuery.limit(100).asList();

            curMonthList.addAll(lastMonthList);
        }
        return curMonthList;
    }



    public void ensureIndex(String dbName, String collectionName) {
        DBCollection dbCollection = dispatchDetailRepository.getCollection(dbName, collectionName);

        DBObject indexMsgId = new BasicDBObject();
        indexMsgId.put("msgId", 1);
        dbCollection.createIndex(indexMsgId);

        DBObject indexBizId = new BasicDBObject();
        indexBizId.put("bizId", 1);
        dbCollection.createIndex(indexBizId);

        DBObject index_status_createTime = new BasicDBObject();
        index_status_createTime.put("status", 1);
        index_status_createTime.put("createTime", 1);
        dbCollection.createIndex(index_status_createTime);
    }
}
