package com.mgnrega.dashboard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @Value("${app.name:MGNREGA-Dashboard}")
    private String appName;

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", appName + " API is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("states", "/api/states");
        endpoints.put("districts", "/api/districts");
        endpoints.put("performance", "/api/performance");
        endpoints.put("health", "/actuator/health");

        response.put("endpoints", endpoints);

        return response;
    }
}