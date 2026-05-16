package com.jzy.aicodeplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("com.jzy.aicodeplatform.mapper")
@EnableCaching
public class YuAiCodeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuAiCodeAppApplication.class, args);
    }
}
