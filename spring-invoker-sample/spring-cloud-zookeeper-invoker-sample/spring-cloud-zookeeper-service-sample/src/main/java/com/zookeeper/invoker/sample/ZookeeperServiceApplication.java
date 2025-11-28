package com.zookeeper.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableRemoteDiscoveryClient
@EnableDiscoveryClient
@SpringBootApplication
public class ZookeeperServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(ZookeeperServiceApplication.class, args);
	}


}
