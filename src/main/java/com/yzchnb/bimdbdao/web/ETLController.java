package com.yzchnb.bimdbdao.web;

import com.yzchnb.bimdbdao.ETL.ETLFacade;
import com.yzchnb.bimdbdao.dao.EntityNodeMongoClient;
import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mongo")
public class ETLController {

    @Resource
    private ETLFacade etlFacade;

    @GetMapping("/test")
    public String test(){
        return "OK";
    }

    @GetMapping("/startETL/{batchSize}")
    public String startETL(@PathVariable("batchSize")int batchSize, @RequestBody(required = false) String source){
        try{
            batchSize = batchSize > 0 ? batchSize : 50;
            if(source == null){
                etlFacade.startETL(batchSize);
            }else{
                File f = new File(source);
                if(f.exists()){
                    etlFacade.startETL(source, batchSize);
                }else{
                    throw new Exception("source " + source + " doesn't exist.");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return "Failed";
        }
        return "Success";
    }

    @GetMapping("/startETLByDir/{batchSize}")
    public String startETLByDir(@PathVariable("batchSize")int batchSize, @RequestBody(required = false) String source){
        try{
            batchSize = batchSize > 0 ? batchSize : 50;
            if(source == null){
                etlFacade.startETLByDir(batchSize);
            }else{
                File f = new File(source);
                if(f.exists() && f.isDirectory() && f.list() != null){
                    etlFacade.startETLByDir(source, batchSize);
                }else{
                    throw new Exception("source " + source + " doesn't exist.");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return "Failed";
        }
        return "Success";
    }

    @GetMapping("/resetLinksId/{batchSize}")
    public String resetLinksId(@PathVariable("batchSize")int batchSize){
        try{
            etlFacade.resetLinksIds(batchSize);
        }catch (Exception e){
            e.printStackTrace();
            return "Failed";
        }
        return "Success";
    }

}
