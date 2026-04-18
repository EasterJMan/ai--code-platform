package com.jzy.aicodeplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@MapperScan("com.jzy.aicodeplatform.mapper")
@SpringBootApplication
public class AiCodePlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCodePlatformApplication.class, args);
	}

}
