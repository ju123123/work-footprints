package com.workfootprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WorkFootprintApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkFootprintApplication.class, args);
    }
}
