package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
@ImportAutoConfiguration(RedissonAutoConfiguration.class)
@EnableRemoteDiscoveryClient(basePackages = "com.boot.invoker")
public class ServiceApplication1 {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		System.setProperty("server.port", "9093");
		SpringApplication.run(ServiceApplication1.class, args);
	}
}
