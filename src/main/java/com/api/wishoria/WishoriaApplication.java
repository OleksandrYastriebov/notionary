package com.api.wishoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WishoriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WishoriaApplication.class, args);
	}

}
