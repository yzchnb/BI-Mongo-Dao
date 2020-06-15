package com.yzchnb.bimdbdao.dao;

import com.mongodb.bulk.BulkWriteResult;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.NodeToRelation;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.yzchnb.bimdbdao.util.Pair;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class EntityNodeMongoClient {

    @Resource
    private EntityNodeRepo entityNodeRepo;

    @PostConstruct
    public void init(){
        ensureIndexes();
    }

    private void ensureIndexes(){
        Index indexOnName = new Index();
        indexOnName.on("name", Sort.Direction.ASC);
        indexOnName.unique();
        Index indexOnNameAndNode = new Index();
        indexOnNameAndNode.on("name", Sort.Direction.ASC);
        indexOnNameAndNode.on("links.node", Sort.Direction.ASC);
        Index indexOnUniqueId = new Index();
        indexOnUniqueId.on("uniqueId", Sort.Direction.ASC);
        indexOnUniqueId.unique();
        String res = mongoTemplate.indexOps("EntityNode").ensureIndex(indexOnName);
        System.out.println(res);
        res = mongoTemplate.indexOps("EntityNode").ensureIndex(indexOnNameAndNode);
        System.out.println(res);
        res = mongoTemplate.indexOps("EntityNode").ensureIndex(indexOnUniqueId);
        System.out.println(res);
    }

    @Resource
    private MongoTemplate mongoTemplate;

    public List<EntityNode> getBatch(int startUniqueId, int size){
        Query query = new Query();
        query.addCriteria(Criteria.where("uniqueId").gte(startUniqueId).lt(startUniqueId + size));
        query.with(Sort.by(Sort.Order.asc("uniqueId")));
        query.limit(size);
        List<EntityNode> l = mongoTemplate.find(query, EntityNode.class, "EntityNode");
        return l;
    }

    public String getEntityNameById(int uniqueId){
        Query q = new Query();
        q.addCriteria(Criteria.where("uniqueId").is(uniqueId));
        q.fields().include("name");
        EntityNode node = mongoTemplate.findOne(q, EntityNode.class, "EntityNode");
        if(node != null){
            return node.getName();
        }
        return null;
    }

    public Integer getEntityIdByName(String name){
        Query q = new Query();
        q.addCriteria(Criteria.where("name").is(name));
        q.fields().include("uniqueId");
        EntityNode node = mongoTemplate.findOne(q, EntityNode.class, "EntityNode");
        if(node != null){
            return node.getUniqueId();
        }
        return null;
    }

    public int getMaxUniqueId(){
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "uniqueId")).limit(1);
        EntityNode entityNode = mongoTemplate.findOne(query, EntityNode.class, "EntityNode");
        if(entityNode == null){
            return -1;
        }
        return entityNode.getUniqueId();
    }


    public List<EntityNode> queryBatchByIds(Collection<Integer> ids){
        Query q = new Query();
        q.addCriteria(Criteria.where("uniqueId").in(ids));
        q.fields().include("name").include("uniqueId");
        return mongoTemplate.find(q, EntityNode.class, "EntityNode");
    }

    public List<EntityNode> queryBatchByNames(Collection<String> names){
        Query q = new Query();
        q.addCriteria(Criteria.where("name").in(names));
        return mongoTemplate.find(q, EntityNode.class, "EntityNode");
    }

    public Pair<Set<EntityNode>, Set<EntityNode>> queryExists(Set<EntityNode> nodes){
        List<String> names = nodes.stream().map(EntityNode::getName).collect(Collectors.toList());
        Set<EntityNode> existsSet = new HashSet<>(queryBatchByNames(names));
        Set<String> existsNames = existsSet.stream().map(EntityNode::getName).collect(Collectors.toSet());
        Set<EntityNode> nonExistsSet = nodes.stream().filter(n -> !existsNames.contains(n.getName())).collect(Collectors.toSet());
        return new Pair<>(existsSet, nonExistsSet);
    }

}
