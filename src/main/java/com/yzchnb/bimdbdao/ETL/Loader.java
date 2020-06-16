package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.dao.EntityNodeMongoClient;
import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.NodeToRelation;
import org.springframework.data.domain.Example;
import com.yzchnb.bimdbdao.util.Pair;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

@Component
public class Loader {
    @Resource
    private EntityNodeRepo entityNodeRepo;

    @Resource
    private EntityNodeMongoClient entityNodeMongoClient;

    private ExecutorService es;

    private int batch = 10;

    public void loadBatch(Pair<Set<EntityNode>, Set<EntityNode>> pair){
        if(pair.getFirst().size() == 0 && pair.getSecond().size() == 0){
            return;
        }
        if(es == null || es.isTerminated() || es.isShutdown()){
            initES();
        }
        es.submit(() -> {
            entityNodeRepo.saveAll(pair.getSecond());
            System.out.println("Loader saved " + pair.getSecond().size() + " non existed nodes");
        });
        es.submit(() -> {
            int count = entityNodeMongoClient.pushLinksInExistedLinks(pair.getFirst());
            System.out.println("Loader modified " + count + " existed nodes");
        });
        es.shutdown();
        try{
            while(!es.awaitTermination(500, TimeUnit.MILLISECONDS)) {}
        }catch (InterruptedException e){
            System.out.println("Interrupted!");
            return;
        }

        
    }

    private void initES(){
        es = Executors.newFixedThreadPool(2);
    }

}
