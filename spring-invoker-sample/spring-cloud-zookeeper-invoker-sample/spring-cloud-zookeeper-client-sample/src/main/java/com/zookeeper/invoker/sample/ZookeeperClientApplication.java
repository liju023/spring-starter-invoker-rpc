package com.zookeeper.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@EnableRemoteDiscoveryClient
@EnableDiscoveryClient
@SpringBootApplication
public class ZookeeperClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZookeeperClientApplication.class, args);
	}
}
