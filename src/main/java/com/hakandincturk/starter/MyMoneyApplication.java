package com.hakandincturk.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(basePackages = {"com.hakandincturk"})
@EntityScan(basePackages = {"com.hakandincturk"})
@EnableJpaRepositories(basePackages = {"com.hakandincturk"})
@SpringBootApplication(scanBasePackages = "com.hakandincturk")
@EnableAsync
public class MyMoneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyMoneyApplication.class, args);
	}

}
