package org.example.managerapi.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SparkConfigurationProperties {
    private String sparkSubmitPath;
    private String jarPath;
    private String mainClass;
    private String master;
    private String configFileStagingLocation;
    private String logFileAppBaseLocation;
    private String appType;
}

