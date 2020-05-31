package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.dao.AutoIncreEntityRepo;
import com.yzchnb.bimdbdao.dao.EntityNodeMongoClient;
import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.AutoIncreEntity;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.NodeToRelation;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.yzchnb.bimdbdao.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class Transformer {
    @Resource
    private AutoIncreEntityRepo autoIncreEntityRepo;
    @Resource
    private EntityNodeRepo entityNodeRepo;
    @Resource
    private EntityNodeMongoClient entityNodeMongoClient;

    public Pair<Set<EntityNode>, Set<EntityNode>> transformBatch(Set<EntityNode> nodes){
        Map<String, EntityNode> nameToEntityNodes = new HashMap<>(nodes.size());
        for (EntityNode node : nodes) {
            nameToEntityNodes.compute(node.getName(), (k, v) -> {
                if(v == null){
                    return node;
                }
                v.addLinks(node.getLinks());
                return v;
            });
        }
        nodes = new HashSet<>(nameToEntityNodes.values());
//        for (EntityNode node : nodes) {
//            for (NodeToRelation link : node.getLinks()) {
//
//            }
//        }
        Pair<Set<EntityNode>, Set<EntityNode>> existsAndNonExistsPair = entityNodeMongoClient.queryExists(nodes);

        Set<EntityNode> nonRepeatEntityNodes = existsAndNonExistsPair.getSecond();
        int id = autoIncreEntityRepo.getAndSetCurrCount(nonRepeatEntityNodes.size());
        for (EntityNode node : nonRepeatEntityNodes) {
            node.setUniqueId(id);
            id++;
        }
        existsAndNonExistsPair.setSecond(nonRepeatEntityNodes);
        return existsAndNonExistsPair;
    }
}
