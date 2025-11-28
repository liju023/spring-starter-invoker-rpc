package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
@EnableRemoteDiscoveryClient(basePackages = "com.boot.invoker")
public class AmqpServiceApplication {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(AmqpServiceApplication.class, args);
	}
}
