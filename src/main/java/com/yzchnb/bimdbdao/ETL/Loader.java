package com.yzchnb.bimdbdao.ETL;

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

    private ExecutorService es;

    private int batch = 10;

    public void loadBatch(Set<EntityNode> nodes){
        if(nodes.size() == 0){
            return;
        }
        if(es == null || es.isTerminated() || es.isShutdown()){
            initES();
        }
        List<Set<EntityNode>> batches = new ArrayList<>(batch + 1);
        Iterator<EntityNode> iter = nodes.iterator();
        for (int i = 0; i < nodes.size(); i++) {
            int batchIndex = i / (nodes.size() / batch);
            if(batchIndex == batches.size()){
                batches.add(new HashSet<>(nodes.size() / batch));
            }
            batches.get(batchIndex).add(iter.next());
        }
        for (Set<EntityNode> entityNodes : batches) {
            es.submit(() -> {
                entityNodeRepo.saveAll(entityNodes);
            });
        }
        es.shutdown();
        while(!es.isTerminated()){
            try{
                System.out.println("Waiting for saveAll finished");
                Thread.sleep(1000);
            }catch (InterruptedException e){
                System.out.println("Interrupted!");
                return;
            }
        }
        
    }

    private void initES(){
        es = Executors.newFixedThreadPool(batch + 1);
    }

}
