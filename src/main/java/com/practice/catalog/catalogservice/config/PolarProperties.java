package com.practice.catalog.catalogservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "polar")
@Data
public class PolarProperties {
    private String greeting;
}
