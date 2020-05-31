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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class Loader {
    @Resource
    private EntityNodeRepo entityNodeRepo;

    private ExecutorService es;

    public void loadBatch(Pair<Set<EntityNode>, Set<EntityNode>> pairs){
        if(es == null){
            initES();
        }
        es.submit(() -> {
            entityNodeRepo.saveAll(pairs.getFirst());
        });
        es.submit(() -> {
            entityNodeRepo.saveAll(pairs.getSecond());
        });
        es.shutdown();
        while(es.isTerminated()){
            try{
                System.out.println("Waiting for saveAll finished");
                Thread.sleep(500);
            }catch (InterruptedException e){
                System.out.println("Interrupted!");
            }
        }
        
    }

    private void initES(){
        es = new ThreadPoolExecutor(10,
                20,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5),
                new DefaultManagedAwareThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

}
