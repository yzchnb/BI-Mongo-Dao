package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.dao.AutoIncreEntityRepo;
import com.yzchnb.bimdbdao.entity.AutoIncreEntity;
import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Component
public class Transformer {
    @Resource
    private AutoIncreEntityRepo autoIncreEntityRepo;

    public Set<EntityNode> transformBatch(Set<EntityNode> nodes){
        int id = autoIncreEntityRepo.getAndSetCurrCount(nodes.size());
        for (EntityNode node : nodes) {
            node.setUniqueId(id);
            id++;
        }
        return nodes;
    }
}
