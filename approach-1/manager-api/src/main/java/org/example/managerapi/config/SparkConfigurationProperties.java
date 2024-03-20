package org.example.managerapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spark")
public record SparkConfigurationProperties(String sparkSubmitPath, String jarPath, String mainClass, String master, String configFileStagingLocation) {}
