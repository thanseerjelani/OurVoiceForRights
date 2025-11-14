package com.mgnrega.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableRetry
public class MgnregaDashboardApplication {
	public static void main(String[] args) {
		SpringApplication.run(MgnregaDashboardApplication.class, args);
	}
}