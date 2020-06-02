package com.yzchnb.bimdbdao.config;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class Logger {

    private BufferedWriter bf;

    @PostConstruct
    public void initLogger(){
        try{
            String fileName = "Log " + LocalDateTime.now().toString() + ".log";
            bf = new BufferedWriter(new FileWriter(fileName));
        }catch (Exception e){
            System.out.println("Failed init Logger");
            System.exit(-1);
        }
    }

    public void info(String info){
        info = "[" + LocalDateTime.now() + "] --- " + info;
        try{
            bf.write(info);
            bf.newLine();
            bf.flush();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("IOException while writing logs");
        }
    }
}
