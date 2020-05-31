package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.beans.factory.annotation.Value;
import com.yzchnb.bimdbdao.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Set;

@Component
public class ETLFacade {
    @Resource
    private Extractor extractor;
    @Resource
    private Transformer transformer;
    @Resource
    private Loader loader;

    @Value("${jsonSourceDir}")
    private String sourceDir;

    public void startETL(String source, int batchSize){
        extractor.setSource(source);
        this.startETL(batchSize);
    }

    public void startETL(int batchSize){
        for(int i = 0;; i++){
            Set<EntityNode> set = extractor.getBatch(batchSize);
            if(set == null || set.size() == 0){
                break;
            }
            Pair<Set<EntityNode>, Set<EntityNode>> pairs = transformer.transformBatch(set);
            System.out.println("Loading Batch " + i + " Batch Size: " + (pairs.getSecond().size() + pairs.getFirst().size()));
            loader.loadBatch(pairs);
        }
    }

    public void startETLByDir(String sourceDir, int batchSize){
        File f = new File(sourceDir);
        File[] files = f.listFiles();
        if(files == null){
            System.out.println("Using Default sourceDir");
            f = new File(this.sourceDir);
            files = f.listFiles();
        }
        for (File file : files) {
            extractor.setSource(file.getAbsolutePath());
            this.startETL(batchSize);
        }
    }

    public void startETLByDir(int batchSize){
        startETLByDir(this.sourceDir, batchSize);
    }
}
