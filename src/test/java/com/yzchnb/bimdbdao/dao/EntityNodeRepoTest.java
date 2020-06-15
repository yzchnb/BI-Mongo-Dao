package com.yzchnb.bimdbdao.dao;

import com.yzchnb.bimdbdao.entity.EntityNode;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EntityNodeRepoTest {

    @Test
    void contextLoad(){
        Set<String> names = new HashSet<>(1000);
        while(names.size() < 1000){
            String n = client.getEntityNameById(1);
            if(n != null){
                names.add(n);
            }
        }
        this.names = names;
    }

    @Resource
    EntityNodeRepo repo;
    @Resource
    EntityNodeMongoClient client;

    private Set<String> names;

    @Test
    void testFindOneByName(){
        contextLoad();
        long start = System.currentTimeMillis();
        names.forEach(n -> repo.findOneByName(n));
        long end = System.currentTimeMillis();
        System.out.println("查询" + names.size() + "个节点的全部信息，平均所花时间：" + (end - start) / 1000. / names.size() + "秒。");
    }


}