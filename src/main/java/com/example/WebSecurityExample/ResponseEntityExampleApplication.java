package com.example.WebSecurityExample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@EnableCaching
public class ResponseEntityExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResponseEntityExampleApplication.class, args);
	}
	@Bean
	public PlatformTransactionManager toImplementTanscation(MongoDatabaseFactory dbFactory){
		return new MongoTransactionManager(dbFactory);
	}
}
