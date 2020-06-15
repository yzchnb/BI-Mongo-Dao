package com.yzchnb.bimdbdao.web;


import com.yzchnb.bimdbdao.config.ResponseFormat;
import com.yzchnb.bimdbdao.dao.EntityNodeMongoClient;
import com.yzchnb.bimdbdao.dao.EntityNodeRepo;
import com.yzchnb.bimdbdao.entity.EntityNode;
import com.yzchnb.bimdbdao.entity.RelationById;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Resource
    private EntityNodeRepo entityNodeRepo;
    @Resource
    private EntityNodeMongoClient entityNodeMongoClient;

    @GetMapping("/getSingleLinksByName/{nodeName}")
    public EntityNode getSingleLinkByName(@PathVariable("nodeName")String nodeName){
        return entityNodeRepo.findOneByName(nodeName);
    }

    @GetMapping("/getSingleLinksById/{uniqueId}")
    public EntityNode getSingleLinksById(@PathVariable("uniqueId") int uniqueId){
        return entityNodeRepo.findOneByUniqueId(uniqueId);
    }

    @GetMapping("/getEntityNameById/{uniqueId}")
    public String getEntityNameById(@PathVariable("uniqueId")int uniqueId){
        return entityNodeMongoClient.getEntityNameById(uniqueId);
    }

    @GetMapping("/getEntityIdByName/{nodeName}")
    public Integer getEntityIdByName(@PathVariable("nodeName")String nodeName){
        return entityNodeMongoClient.getEntityIdByName(nodeName);
    }

    @GetMapping("/getBatch/{start}/{size}")
    public List<EntityNode> getBatch(@PathVariable("start")int start, @PathVariable("size") int size){
        return entityNodeMongoClient.getBatch(start, size);
    }

    @PostMapping("/getBatchNamesByIds")
    public List<EntityNode> getBatchNamesByIds(@RequestBody List<Integer> ids){
        return entityNodeMongoClient.queryBatchByIds(ids);
    }

    @PostMapping("/getBatchRelations")
    public Map<Integer, Set<RelationById>> getBatchRelations(@RequestBody Map<Integer, List<Integer>> pairs){
        return entityNodeMongoClient.queryBatchRelations(pairs);
    }

    @GetMapping("/getSingleLinksByNamePageable/{nodeName}/{startFrom}/{limit}")
    public EntityNode getSingleLinkByNamePageable(@PathVariable("nodeName")String nodeName, @PathVariable("startFrom")int startFrom, @PathVariable("limit")int limit){
        Integer id = getEntityIdByName(nodeName);
        return entityNodeMongoClient.getSingleLinksByIdPageable(id, startFrom, limit);
    }

    @GetMapping("/getSingleLinksByIdPageable/{uniqueId}/{startFrom}/{limit}")
    public EntityNode getSingleLinksByIdPageable(@PathVariable("uniqueId") int uniqueId, @PathVariable("startFrom")int startFrom, @PathVariable("limit")int limit){
        return entityNodeMongoClient.getSingleLinksByIdPageable(uniqueId, startFrom, limit);
    }



    @GetMapping("/getMaxUniqueId")
    public Integer getMaxUniqueId(){
        return entityNodeMongoClient.getMaxUniqueId();
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
