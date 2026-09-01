package com.myfarmproduce.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.myfarmproduce")
@EntityScan(basePackages = "com.myfarmproduce.domain.entity")
@EnableJpaRepositories(basePackages = "com.myfarmproduce.infrastructure.repository")
public class MyFarmProduceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyFarmProduceApplication.class, args);
    }
}
