package com.yzchnb.bimdbdao.dao;

import com.yzchnb.bimdbdao.entity.AutoIncreEntity;
import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Repository
public class AutoIncreEntityRepo {

    @Resource
    private MongoTemplate mongoTemplate;


    public int getAndSetCurrCount(int batchSize){
        String collectionName = "AutoIncreEntity";
        List<AutoIncreEntity> list = mongoTemplate.findAll(AutoIncreEntity.class, collectionName);
        AutoIncreEntity aie;
        if(list.size() == 0){
            aie = new AutoIncreEntity(1);
            mongoTemplate.insert(aie);
        }else{
            aie = list.get(0);
        }
        int currCount = aie.getCurrCount();
        aie.setCurrCount(aie.getCurrCount() + batchSize);
        mongoTemplate.save(aie, collectionName);
        return currCount;
    }
}
