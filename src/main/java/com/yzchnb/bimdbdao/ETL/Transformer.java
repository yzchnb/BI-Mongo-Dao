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
        nodes.forEach(node ->
            nameToEntityNodes.compute(node.getName(), (k, v) -> {
                if(v == null){
                    return node;
                }
                v.addLinks(node.getLinks());
                return v;
            })
        );
        nodes = new HashSet<>(nameToEntityNodes.values());
        Pair<Set<EntityNode>, Set<EntityNode>> existsAndNonExistsPair = entityNodeMongoClient.queryExists(nodes);
        existsAndNonExistsPair.getFirst().forEach((e -> {
            if(e.getLinks() == null){
                e.setLinks(new HashSet<>());
            }
            e.getLinks().addAll(nameToEntityNodes.get(e.getName()).getLinks());
        }));
        Set<EntityNode> nonRepeatEntityNodes = existsAndNonExistsPair.getSecond();
        int id = autoIncreEntityRepo.getAndSetCurrCount(nonRepeatEntityNodes.size());
        for (EntityNode node : nonRepeatEntityNodes) {
            node.setUniqueId(id);
            id++;
        }
        nodes.clear();
        nodes.addAll(existsAndNonExistsPair.getFirst());
        nodes.addAll(nonRepeatEntityNodes);
        setLinksUniqueIds(nodes);
        return existsAndNonExistsPair;
    }

    private void setLinksUniqueIds(Set<EntityNode> entityNodes){
        Map<String, EntityNode> nameToEntityNodes = new HashMap<>(entityNodes.size());
        for (EntityNode node : entityNodes) {
            nameToEntityNodes.put(node.getName(), node);
        }
        for (EntityNode entityNode : entityNodes) {
            for (NodeToRelation link : entityNode.getLinks()) {
                if(link.getUniqueId() == null || link.getUniqueId() == 0){
                    nameToEntityNodes.computeIfPresent(link.getNode(), (k, v) -> {
                        link.setUniqueId(v.getUniqueId());
                        return v;
                    });
                }
            }
        }
    }
}
