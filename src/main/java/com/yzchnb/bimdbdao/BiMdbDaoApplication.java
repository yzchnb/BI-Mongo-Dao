package com.yzchnb.bimdbdao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/index")
public class BiMdbDaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiMdbDaoApplication.class, args);
    }

    @GetMapping("/test")
    public String d(){
        return "Hello";
    }

}
