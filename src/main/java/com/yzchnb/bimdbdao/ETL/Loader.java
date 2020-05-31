package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.NodeToRelation;
import org.springframework.data.domain.Example;
import com.yzchnb.bimdbdao.util.Pair;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class Loader {
    @Resource
    private EntityNodeRepo entityNodeRepo;

    private ExecutorService es;

    public void loadBatch(Set<EntityNode> nodes){
        if(es == null || es.isTerminated() || es.isShutdown()){
            initES();
        }
        es.submit(() -> {
            entityNodeRepo.saveAll(nodes);
        });
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
        es = Executors.newFixedThreadPool(1);
    }

}
