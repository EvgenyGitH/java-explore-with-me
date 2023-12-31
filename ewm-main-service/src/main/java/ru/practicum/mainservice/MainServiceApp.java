package ru.practicum.mainservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication//(scanBasePackages = {"ru.practicum.statsclient", "ru.practicum.mainservice"})
@ComponentScan(basePackages = {"ru.practicum.statsclient", "ru.practicum.mainservice"})
public class MainServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(MainServiceApp.class, args);
    }
}