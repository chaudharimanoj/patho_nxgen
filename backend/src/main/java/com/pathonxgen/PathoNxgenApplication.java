package com.pathonxgen;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Patho NXGen Platform - Entry point.
 * Modular monolith: each business capability lives under modules/*
 * with its own controller/service/repository/entity/dto layers.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class PathoNxgenApplication {
    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		System.setProperty("user.timezone", "Asia/Kolkata");

        SpringApplication.run(PathoNxgenApplication.class, args);
    }
}
