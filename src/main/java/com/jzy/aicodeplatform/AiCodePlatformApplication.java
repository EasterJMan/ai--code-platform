package com.jzy.aicodeplatform;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.jzy.aicodeplatform.mapper")
@SpringBootApplication(exclude = RedisEmbeddingStoreAutoConfiguration.class)
public class AiCodePlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCodePlatformApplication.class, args);
	}

}
