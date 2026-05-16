package com.jzy.aicodeplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.jzy.aicodeplatform.mapper")
@ComponentScan("com.jzy")
public class YuAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(YuAiCodeUserApplication.class, args);
    }
}
