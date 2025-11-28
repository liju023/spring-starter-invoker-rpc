package com.invoker.sample.consul;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableRemoteDiscoveryClient
@SpringBootApplication
@EnableDiscoveryClient
public class ConsulServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(ConsulServiceApplication.class, args);
	}


}
