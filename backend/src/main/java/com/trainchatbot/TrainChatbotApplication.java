package com.trainchatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TrainChatbotApplication
 * 
 * This is the entry point of the Spring Boot application.
 * It initializes the server and scans for components.
 */
@SpringBootApplication
public class TrainChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainChatbotApplication.class, args);
    }
}
