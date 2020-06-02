package com.yzchnb.bimdbdao.ETL;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.yzchnb.bimdbdao.config.Logger;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.NodeToRelation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class Extractor {
    @Value("${jsonSource}")
    private String source;

    @Resource
    private Logger logger;

    public String getSource() {
        return source;
    }

    private JSONReader reader;
    private boolean finished = false;

    private void initData() {
        try{
            reader = new JSONReader(new FileReader(source));
            reader.startArray();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        logger.info("Start Reading " + source);
    }

    Set<EntityNode> getBatch(int batchSize){
        if(finished){
            return null;
        }
        if(reader == null){
            initData();
        }
        Set<EntityNode> set = new HashSet<>();
        for(int i = 0; i < batchSize && reader.hasNext(); i++){
            JSONObject o = reader.readObject(JSONObject.class);
            EntityNode node = new EntityNode();
            node.setName(o.getString("name"));
            JSONObject m = o.getJSONObject("properties");
            m.forEach((k, v) -> {
                JSONArray a = (JSONArray) v;
                for (Object n : a) {
                    node.addLink((String)n, k, 1);
                }
            });
            List<EntityNode> reverses = new ArrayList<>(node.getLinks().size());
            for (NodeToRelation link : node.getLinks()) {
                EntityNode reversed = new EntityNode();
                reversed.setName(link.getNode());
                reversed.addLink(node.getName(), link.getRelation(), -1);
                reverses.add(reversed);
            }
            set.add(node);
            set.addAll(reverses);
        }
        logger.info("Read Batch " + set.size() + " from " + source);
        System.out.println("Extractor read batch from " + source);
        if(!reader.hasNext()){
            logger.info("End for file " + source);
            reader.endArray();
            reader.close();
            reader = null;
            finished = true;
        }
        return set;
    }

    void setSource(String source){
        this.source = source;
        this.finished = false;
        initData();
    }
}
