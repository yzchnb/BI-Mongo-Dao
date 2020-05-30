package com.yzchnb.bimdbdao.web;

import com.yzchnb.bimdbdao.ETL.ETLFacade;
import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.io.File;
import java.util.Arrays;

@RestController
@RequestMapping("/test")
public class IndexController {

    @Resource
    private EntityNodeRepo entityNodeRepo;

    @Resource
    private ETLFacade etlFacade;

    @GetMapping("/add")
    public String test(){
        EntityNode node = new EntityNode();
        node.setName("TestNode");
        node.setUniqueId(1);
        node.setLinks(Arrays.asList("nodeA", "nodeB"),  Arrays.asList("r1", "r2"));
        EntityNode res = entityNodeRepo.insert(node);
        System.out.println(res.get_id());
        return "ojbk";
    }

    @GetMapping("/startETL/{batchSize}")
    public String startETL(@PathVariable("batchSize")int batchSize, @PathParam("source") String source){
        try{
            batchSize = batchSize > 0 ? batchSize : 50;
            if(source == null){
                etlFacade.startETL(batchSize);
            }else{
                File f = new File(source);
                if(f.exists()){
                    etlFacade.startETL(source, batchSize);
                }else{
                    etlFacade.startETL(batchSize);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return "Failed";
        }
        return "Success";
    }
}
