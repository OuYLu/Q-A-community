package com.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.community.mapper")
public class SmartCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCommunityApplication.class, args);
    }
}
