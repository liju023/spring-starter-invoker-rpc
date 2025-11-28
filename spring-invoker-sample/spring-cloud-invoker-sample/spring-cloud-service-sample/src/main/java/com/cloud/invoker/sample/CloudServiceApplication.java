package com.cloud.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@EnableRemoteDiscoveryClient
public class CloudServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(CloudServiceApplication.class, args);
	}


}
