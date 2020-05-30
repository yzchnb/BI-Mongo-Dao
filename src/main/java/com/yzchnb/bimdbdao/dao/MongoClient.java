package com.yzchnb.bimdbdao.dao;

import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;

@Component
public class MongoClient {

    @Resource
    private MongoTemplate mongoTemplate;

    public void add(Collection<?> batch, String collection){
        new Query();
        mongoTemplate.insert(batch, collection);
    }

    public Object find(String collection, String id){
        return mongoTemplate.findById(id, EntityNode.class);
    }

}
