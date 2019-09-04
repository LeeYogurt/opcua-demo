package com.rookie.opcua;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author yugo
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.rookie.opcua.mapper")
public class OpcUaApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpcUaApplication.class, args);
    }
}
