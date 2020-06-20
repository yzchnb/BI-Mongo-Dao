package com.yzchnb.bimdbdao.dao;

import com.mongodb.bulk.BulkWriteResult;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.NodeToRelation;
import com.yzchnb.bimdbdao.entity.RelationById;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.yzchnb.bimdbdao.util.Pair;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EntityNodeMongoClient {

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
        Index indexOnIdAndNodeId = new Index();
        indexOnIdAndNodeId.on("uniqueId", Sort.Direction.ASC);
        indexOnIdAndNodeId.on("links.uniqueId", Sort.Direction.ASC);
        String res = mongoTemplate.indexOps("EntityNode").ensureIndex(indexOnName);
        System.out.println(res);
        res = mongoTemplate.indexOps("EntityNode").ensureIndex(indexOnNameAndNode);
        System.out.println(res);
        res = mongoTemplate.indexOps("EntityNode").ensureIndex(indexOnUniqueId);
        System.out.println(res);
        res = mongoTemplate.indexOps("EntityNode").ensureIndex(indexOnIdAndNodeId);
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
        q.fields().exclude("links").exclude("_id");
        List<EntityNode> list = mongoTemplate.find(q, EntityNode.class, "EntityNode");
        list.forEach(l -> l.set_id(null));
        return list;
    }

    public List<EntityNode> queryBatchIfExistByNames(Collection<String> names){
        Query q = new Query();
        q.addCriteria(Criteria.where("name").in(names));
        q.fields().exclude("links").exclude("_id");
        List<EntityNode> list = mongoTemplate.find(q, EntityNode.class, "EntityNode");
        return list;
    }

    public int pushLinksInExistedLinks(Set<EntityNode> nodes){
        BulkOperations bops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "EntityNode");
        List<org.springframework.data.util.Pair<Query, Update>> ops = new ArrayList<>(nodes.size());
        nodes.forEach((n) -> {
            Query q = new Query().addCriteria(Criteria.where("uniqueId").is(n.getUniqueId()));
            Update u = new Update();
            u.addToSet("links").each(n.getLinks());
            ops.add(org.springframework.data.util.Pair.of(q, u));
        });
        bops.updateMulti(ops);
        BulkWriteResult res = bops.execute();
        return res.getModifiedCount();
    }

    public Pair<Set<EntityNode>, Set<EntityNode>> queryExists(Set<EntityNode> nodes){
        List<String> names = nodes.stream().map(EntityNode::getName).collect(Collectors.toList());
        Set<EntityNode> existsSet = new HashSet<>(queryBatchIfExistByNames(names));
        Set<String> existsNames = existsSet.stream().map(EntityNode::getName).collect(Collectors.toSet());
        Set<EntityNode> nonExistsSet = nodes.stream().filter(n -> !existsNames.contains(n.getName())).collect(Collectors.toSet());
        return new Pair<>(existsSet, nonExistsSet);
    }

    public Map<Integer, Set<RelationById>> queryBatchRelations(Map<Integer, List<Integer>> pairs){
        Map<Integer, Set<RelationById>> relations = new HashMap<>(32);
        pairs.forEach((left, links) -> {
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(Aggregation.match(Criteria.where("uniqueId").is(left)));
            operations.add(Aggregation.unwind("links"));
            operations.add(Aggregation.match(Criteria.where("links.uniqueId").in(links)));

            operations.add(Aggregation.group("uniqueId")
                    .first("uniqueId").as("uniqueId")
                    .push("links").as("links"));
            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<EntityNode> results = mongoTemplate.aggregate(aggregation, "EntityNode", EntityNode.class);
            List<EntityNode> list = results.getMappedResults();
            if(list.size() == 1){
                EntityNode l = list.get(0);
                l.getLinks().forEach(link -> {
                    RelationById r = new RelationById();
                    int startId, endId;
                    if(link.getDirection() == 1){
                        startId = l.getUniqueId(); endId = link.getUniqueId();
                    }else{
                        endId = l.getUniqueId(); startId = link.getUniqueId();
                    }
                    r.setStartUniqueId(startId);
                    r.setRelation(link.getRelation());
                    r.setEndUniqueId(endId);
                    relations.compute(l.getUniqueId(), (k, v) -> {
                        if(v == null){
                            v = new HashSet<>();
                        }
                        v.add(r);
                        return v;
                    });
                });
            }
        });
        return relations;
    }


    public EntityNode getSingleLinksByIdPageable(Integer id, int startFrom, int limit){
        List<AggregationOperation> operations = new ArrayList<>(6);
        operations.add(Aggregation.match(Criteria.where("uniqueId").is(id)));
        operations.add(Aggregation.unwind("links"));
        Sort.by(Sort.Direction.ASC, "links.uniqueId");
        operations.add(Aggregation.sort(Sort.by(Sort.Direction.ASC, "links.uniqueId")));
        operations.add(Aggregation.skip(startFrom));
        operations.add(Aggregation.limit(limit));

        operations.add(Aggregation.group("uniqueId")
                .first("uniqueId").as("uniqueId")
                .first("name").as("name")
                .push("links").as("links"));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<EntityNode> results = mongoTemplate.aggregate(aggregation, "EntityNode", EntityNode.class);
        List<EntityNode> list = results.getMappedResults();
        if(list.size() == 1){
            return list.get(0);
        }
        return null;
    }

    public int getSingleLinksCount(Integer id){
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("uniqueId").is(id)));
        operations.add(Aggregation.unwind("links"));
        operations.add(Aggregation.count().as("linksCount"));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "EntityNode", Map.class);
        List<Map> list = results.getMappedResults();
        if(list.size() == 1){
            return (Integer)list.get(0).get("linksCount");
        }
        return 0;
    }

}
