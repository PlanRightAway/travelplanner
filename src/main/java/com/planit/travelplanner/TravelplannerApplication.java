package com.planit.travelplanner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class TravelplannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TravelplannerApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public WebClient.Builder configureWebClientBuilder() {
		return WebClient.builder();
	}
	
	@Bean(name = "routeFinder")
	public ExecutorService configureRouteFinderExecutorService() {
		return new ThreadPoolExecutor(3, 4, 2L, TimeUnit.HOURS, new LinkedBlockingQueue<Runnable>());
	}
	@Bean(name = "backTracer")
	public ExecutorService configureBackTracerExecutorService() {
		return new ThreadPoolExecutor(3, 4, 2L, TimeUnit.HOURS, new LinkedBlockingQueue<Runnable>());
	}


}
