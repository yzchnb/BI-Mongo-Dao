package com.yzchnb.bimdbdao.ETL;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

@Component
public class Extractor {
    @Value("${jsonSource}")
    private String source;

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
                    node.addLink((String)n, k);
                }
            });
            set.add(node);
        }
        if(!reader.hasNext()){
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
    }
}
