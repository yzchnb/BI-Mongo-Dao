package com.yzchnb.bimdbdao.ETL;

import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class ETLFacade {
    @Resource
    private Extractor extractor;
    @Resource
    private Transformer transformer;
    @Resource
    private Loader loader;

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
            set = transformer.transformBatch(set);
            System.out.println("Loading Batch " + i + " Batch Size: " + set.size());
            loader.loadBatch(set);
        }

    }
}
