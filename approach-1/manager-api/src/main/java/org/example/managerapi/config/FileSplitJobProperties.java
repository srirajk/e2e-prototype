package org.example.managerapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spark-properties.file-splitter")
public class FileSplitJobProperties extends SparkConfigurationProperties {}

