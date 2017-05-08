/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.mq.compensation.service;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Updates.*;
import static com.ymatou.mq.infrastructure.repository.MessageCompensateRepository.dbName;
import static com.ymatou.mq.infrastructure.util.MongoHelper.buildCompensateId;
import static com.ymatou.mq.infrastructure.util.MongoHelper.getMessageCompensateCollectionName;

import java.util.Date;
import java.util.List;

import org.bson.conversions.Bson;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.ymatou.mq.infrastructure.model.CallbackMessage;
import com.ymatou.mq.infrastructure.model.MessageCompensate;
import com.ymatou.mq.infrastructure.repository.MessageCompensateRepository;
import com.ymatou.mq.infrastructure.support.enums.CompensateStatusEnum;
import com.ymatou.mq.infrastructure.util.NetUtil;

/**
 * @author luoshiqian 2017/5/2 16:54
 */
@Component
public class MessageCompensateService{

    @Autowired
    private MessageCompensateRepository messageCompensateRepository;

    public void saveCompensate(MessageCompensate messageCompensate){
        messageCompensateRepository.saveCompensate(messageCompensate);
    }


    public List<MessageCompensate> findCompensate(String appId, String queueCode, String callbackKey){
        String collectionName = getMessageCompensateCollectionName(appId,queueCode);

        Query<MessageCompensate> query = messageCompensateRepository.newQuery(MessageCompensate.class, dbName,
                collectionName, ReadPreference.secondaryPreferred());
        query.field("consumerId").equal(callbackKey)
                .field("status").equal(CompensateStatusEnum.COMPENSATE.getCode())
                .field("nextTime").lessThanOrEq(new Date());

        return query.limit(100).asList();
    }

    /**
     * 修改补单状态
     * @param message
     * @param statusEnum
     */
    public void updateStatus(CallbackMessage message, CompensateStatusEnum statusEnum, boolean isIncreaseCount){
        String collectionName = getMessageCompensateCollectionName(message.getAppId(),message.getQueueCode());

        Bson doc = eq("_id", buildCompensateId(message.getId(),message.getCallbackKey()));
        Bson set = combine(
                set("status", statusEnum.getCode()),
                set("lastResp", message.getResponse()),
                set("lastTime",message.getResponseTime()),
                set("lastFrom", message.getLastFrom().getCode()),
                set("dealIp", NetUtil.getHostIp()),
                set("updateTime", new Date())
        );

        if(message.getNextTime() != null){
            set = combine(set("nextTime", message.getNextTime()),set);
        }
        if(isIncreaseCount){
            set = combine(inc("compensateNum",1),set);
        }
        messageCompensateRepository.updateOne(dbName,collectionName,doc,set);
    }

    /**
     * 批量修改补单状态
     * @param queueCode
     * @param compensateIdList
     * @param statusEnum
     */
    public void updateStatusByMulti(String appId,String queueCode, List<String> compensateIdList, CompensateStatusEnum statusEnum,String reason){
        String collectionName = getMessageCompensateCollectionName(appId,queueCode);

        Bson doc = in("_id", compensateIdList);
        Bson set = combine(
                set("status", statusEnum.getCode()),
                set("lastResp", reason),
                set("dealIp", NetUtil.getHostIp()),
                set("updateTime", new Date())
        );
        messageCompensateRepository.updateOne(dbName,collectionName,doc,set);
    }


    public void ensureIndex(String collectionName){
        DBCollection dbCollection = messageCompensateRepository.getCollection(dbName, collectionName);

        DBObject indexBizId = new BasicDBObject();
        indexBizId.put("bizId", 1);
        dbCollection.createIndex(indexBizId);

        DBObject indexCreateTime = new BasicDBObject();
        indexCreateTime.put("createTime", 1);
        dbCollection.createIndex(indexCreateTime);

        DBObject index_consumerId_status_nextTime = new BasicDBObject();
        index_consumerId_status_nextTime.put("consumerId", 1);
        index_consumerId_status_nextTime.put("status", 1);
        index_consumerId_status_nextTime.put("nextTime", 1);
        dbCollection.createIndex(index_consumerId_status_nextTime);
    }
}
