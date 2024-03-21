package org.example.managerapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spark-properties.output-file-writer")
public class SparkOutputFileWriterProperties extends SparkConfigurationProperties {}