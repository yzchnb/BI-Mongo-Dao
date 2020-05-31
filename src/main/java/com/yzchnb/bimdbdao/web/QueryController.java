package com.yzchnb.bimdbdao.web;


import com.yzchnb.bimdbdao.config.ResponseFormat;
import com.yzchnb.bimdbdao.dao.EntityNodeMongoClient;
import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Resource
    private EntityNodeRepo entityNodeRepo;
    @Resource
    private EntityNodeMongoClient entityNodeMongoClient;

    @GetMapping("/getSingleLinks/{nodeName}")
    public ResponseFormat getSingleLink(@PathVariable("nodeName")String nodeName){
        EntityNode node = entityNodeRepo.findOneByName(nodeName);
        return wrap(node);
    }

    @GetMapping("/getBatch/{start}/{size}")
    public ResponseFormat getBatch(@PathVariable("start")int start, @PathVariable("size") int size){
        return wrap(entityNodeMongoClient.getBatch(start, size));
    }

    @GetMapping("/getMaxUniqueId")
    public ResponseFormat getMaxUniqueId(){
        return wrap(entityNodeMongoClient.getMaxUniqueId());
    }

    private ResponseFormat wrap(Object o){
        ResponseFormat format = new ResponseFormat();
        if(o == null){
            format.setCode(-1);
            return format;
        }
        format.setCode(0);
        format.setData(o);
        return format;
    }
}
