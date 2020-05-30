package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class Loader {
    @Resource
    private EntityNodeRepo entityNodeRepo;

    public void loadBatch(Set<EntityNode> nodes){
        entityNodeRepo.saveAll(nodes);
    }
}
