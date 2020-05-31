package com.yzchnb.bimdbdao.dao;

import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface EntityNodeRepo extends MongoRepository<EntityNode, String> {
    EntityNode findOneByName(String name);
}
