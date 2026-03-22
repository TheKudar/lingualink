package com.lingualink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class LingualinkApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LingualinkApplication.class);

        app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
            ConfigurableEnvironment env = event.getEnvironment();

            System.out.println("ACTIVE PROFILES = " + String.join(", ", env.getActiveProfiles()));
            System.out.println("DATASOURCE URL = " + env.getProperty("spring.datasource.url"));
            System.out.println("DATASOURCE USERNAME = " + env.getProperty("spring.datasource.username"));
            System.out.println("DATASOURCE PASSWORD = " + env.getProperty("spring.datasource.password"));
        });

        app.run(args);
    }
}