package com.scholarmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScholarmateBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScholarmateBackendApplication.class, args);
    }

}
