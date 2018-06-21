package com.njugs.datanode;

import com.njugs.datanode.storage.StorageProperties;
import com.njugs.datanode.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@EnableEurekaClient
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class DatanodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatanodeApplication.class, args);
    }
    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
