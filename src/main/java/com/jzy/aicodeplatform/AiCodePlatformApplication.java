package com.jzy.aicodeplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class AiCodePlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCodePlatformApplication.class, args);
	}

}
