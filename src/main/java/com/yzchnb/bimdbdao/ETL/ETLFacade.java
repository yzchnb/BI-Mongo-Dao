package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.dao.EntityNodeMongoClient;
import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.NodeToRelation;
import org.springframework.beans.factory.annotation.Value;
import com.yzchnb.bimdbdao.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class ETLFacade {
    @Resource
    private Extractor extractor;
    @Resource
    private Transformer transformer;
    @Resource
    private Loader loader;
    @Resource
    private EntityNodeMongoClient entityNodeMongoClient;
    @Resource
    private EntityNodeRepo entityNodeRepo;

    @Value("${jsonSourceDir}")
    private String sourceDir;

    public void startETL(String source, int batchSize) throws Exception{
        extractor.setSource(source);
        this.startETL(batchSize);
    }

    private boolean ETLing = false;

    public void startETL(int batchSize) throws Exception{
        if(ETLing){
            throw new Exception("ETL is preceding");
        }
        ETLing = true;
        for(int i = 0;; i++){
            Set<EntityNode> set = extractor.getBatch(batchSize);
            if(set == null || set.size() == 0){
                break;
            }
            Set<EntityNode> nodes = transformer.transformBatch(set);
            System.out.println("Loading Batch " + i + " Batch Size: " + nodes.size());
            loader.loadBatch(nodes);
        }
        ETLing = false;
        System.out.println("Finshed ETL for file: " + extractor.getSource());
    }

    public void startETLByDir(String sourceDir, int batchSize){
        File f = new File(sourceDir);
        File[] files = f.listFiles();
        if(files == null){
            System.out.println("Using Default sourceDir");
            f = new File(this.sourceDir);
            files = f.listFiles();
        }
        if(files == null){
            System.out.println("Files is null!");
            return;
        }
        List<File> fileList = Arrays.stream(files).filter((g) -> g.getName().endsWith(".json")).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
        for (File file : fileList) {
            try{
                extractor.setSource(file.getAbsolutePath());
                this.startETL(batchSize);
            }catch (Exception e){
                System.out.println("Encountered error while ETLing " + file.getAbsolutePath());
            }
        }
    }

    public void startETLByDir(int batchSize){
        startETLByDir(this.sourceDir, batchSize);
    }

    public void resetLinksIds(int batchSize){
        int maxUniqueId = entityNodeMongoClient.getMaxUniqueId();
        Map<String, Integer> innerCache = new HashMap<>();
        ExecutorService es = Executors.newFixedThreadPool(20);
        for(int i = 0; i < maxUniqueId; i += batchSize){
            List<EntityNode> list = entityNodeMongoClient.getBatch(i, batchSize);
            for (EntityNode entityNode : list) {
                innerCache.put(entityNode.getName(), entityNode.getUniqueId());
            }
            Set<EntityNode> updated = new HashSet<>();
            for (EntityNode entityNode : list) {
                boolean hasAdded = false;
                for (NodeToRelation link : entityNode.getLinks()) {
                    if(link.getUniqueId() == null || link.getUniqueId() == 0){
                        if(!hasAdded){
                            updated.add(entityNode);
                            hasAdded = true;
                        }
                    }
                    if(null == innerCache.computeIfPresent(link.getNode(), (k, v) -> {
                        link.setUniqueId(v);
                        return v;
                    })){
                        EntityNode found = entityNodeRepo.findOneByName(link.getNode());
                        if(found != null){
                            link.setUniqueId(found.getUniqueId());
                        }else{
                            System.out.println("Can't find " + link.getNode() + "while resetting links Ids");
                        }
                    }
                }
            }
            int copyi = i;
            es.submit(() -> {
                System.out.println("Resetting links Ids batch from " + copyi + " to " + (copyi + updated.size() + ". Batch size:" + updated.size()));
                entityNodeRepo.saveAll(updated);
            });
        }
        es.shutdown();
        while(!es.isTerminated()){
            try{
                Thread.sleep(1000);

                System.out.println("Waiting for resetting jobs");
            }catch (InterruptedException e){
                System.out.println("Interrupted!");
                return;
            }
        }
    }
}
