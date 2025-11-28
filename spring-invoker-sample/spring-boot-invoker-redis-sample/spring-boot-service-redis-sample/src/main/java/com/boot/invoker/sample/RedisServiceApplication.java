package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import com.i360day.invoker.support.RemoteExporter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.util.Map;


@SpringBootApplication
//@ImportAutoConfiguration(RedissonAutoConfiguration.class)
@ImportAutoConfiguration(RedisAutoConfiguration.class)
@EnableRemoteDiscoveryClient(basePackages = "com.boot.invoker")
public class RedisServiceApplication {

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init(){
		Map<String, RemoteExporter> beansOfType = applicationContext.getBeansOfType(RemoteExporter.class);
	}
	public static void main(String[] args) {
		SpringApplication.run(RedisServiceApplication.class, args);
	}
}
