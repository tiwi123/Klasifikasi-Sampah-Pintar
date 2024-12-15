package com.example.wasteclassification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.wasteclassification")
public class WasteClassificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(WasteClassificationApplication.class, args);
    }
}