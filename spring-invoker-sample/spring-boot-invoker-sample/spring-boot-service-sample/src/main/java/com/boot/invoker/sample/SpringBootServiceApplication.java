package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import com.i360day.invoker.support.RemoteExporter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;


@EnableAsync
@SpringBootApplication
//@RemoteScan(basePackages = "com.boot")
@EnableRemoteDiscoveryClient(basePackages = "com.boot")
public class SpringBootServiceApplication {

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init(){
		Map<String, RemoteExporter> beansOfType = applicationContext.getBeansOfType(RemoteExporter.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootServiceApplication.class, args);
	}
}
