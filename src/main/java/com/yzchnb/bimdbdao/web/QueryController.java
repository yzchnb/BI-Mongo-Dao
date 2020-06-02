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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Resource
    private EntityNodeRepo entityNodeRepo;
    @Resource
    private EntityNodeMongoClient entityNodeMongoClient;

    @GetMapping("/getSingleLinksByName/{nodeName}")
    public ResponseFormat getSingleLinkByName(@PathVariable("nodeName")String nodeName){
        EntityNode node = entityNodeRepo.findOneByName(nodeName);
        return wrap(node);
    }

    @GetMapping("/getSingleLinksById/{uniqueId}")
    public ResponseFormat getSingleLinksById(@PathVariable("uniqueId") int uniqueId){
        EntityNode node = entityNodeRepo.findOneByUniqueId(uniqueId);
        return wrap(node);
    }

    @GetMapping("/getEntityNameById/{uniqueId}")
    public ResponseFormat getEntityNameById(@PathVariable("uniqueId")int uniqueId){
        EntityNode node = entityNodeRepo.findOneByUniqueId(uniqueId);
        return wrap(node.getName());
    }

    @GetMapping("/getEntityIdByName/{nodeName}")
    public ResponseFormat getEntityNameById(@PathVariable("nodeName")String nodeName){
        EntityNode node = entityNodeRepo.findOneByName(nodeName);
        return wrap(node.getUniqueId());
    }

    @GetMapping("/getBatch/{start}/{size}")
    public ResponseFormat getBatch(@PathVariable("start")int start, @PathVariable("size") int size){
        return wrap(entityNodeMongoClient.getBatch(start, size));
    }

    @GetMapping("/getBatchIds/{start}/{size}")
    public ResponseFormat getBatchIds(@PathVariable("start")int start, @PathVariable("size") int size){
        List<EntityNode> nodes = entityNodeMongoClient.getBatch(start, size);
        List<Integer> nodesIds = nodes.stream().map(EntityNode::getUniqueId).collect(Collectors.toList());
        return wrap(nodesIds);
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
