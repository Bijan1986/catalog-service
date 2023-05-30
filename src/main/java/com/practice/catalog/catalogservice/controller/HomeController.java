package com.practice.catalog.catalogservice.controller;

import com.practice.catalog.catalogservice.config.PolarProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hello")
@RequiredArgsConstructor
public class HomeController {
    private final PolarProperties polarProperties;

    @GetMapping
    public String getProperty() {
        return polarProperties.getGreeting();
    }

}
