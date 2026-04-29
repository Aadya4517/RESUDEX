package com.resudex.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // health check
    @GetMapping("/health")
    public String health() {
        return "Resudex backend is running";
    }
}
