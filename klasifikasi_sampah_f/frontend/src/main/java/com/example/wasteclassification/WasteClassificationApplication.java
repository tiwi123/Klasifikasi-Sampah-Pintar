package com.example.wasteclassification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WasteClassificationApplication {

    public static void main(String[] args) {
        // Running the Spring Boot application
        SpringApplication.run(WasteClassificationApplication.class, args);

        // Print confirmation message once the application has started
        System.out.println("Waste Classification Application is running...");
    }
}
