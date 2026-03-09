package com.api.notionary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NotionaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotionaryApplication.class, args);
	}

}
