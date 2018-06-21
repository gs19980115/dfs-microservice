package com.njugs.dfs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.FileSystemUtils;

import java.io.File;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaServer
public class NameNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NameNodeApplication.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {

            File file = new File("Blocks");
            FileSystemUtils.deleteRecursively(file);
            file.mkdir();

            file = new File("Files");
            FileSystemUtils.deleteRecursively(file);
            file.mkdir();

        };
    }

}
