package org.example.managerapi.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.utility.SparkDeploymentUtility;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SparkConfigurationProperties.class)
public class SparkDeploymentConfiguration {

    private SparkConfigurationProperties sparkConfigurationProperties;
    private final ObjectMapper mapper;

    public SparkDeploymentConfiguration(final SparkConfigurationProperties sparkConfigurationProperties,
                                        final ObjectMapper mapper) {
        this.sparkConfigurationProperties = sparkConfigurationProperties;
        this.mapper = mapper;
    }

    @Bean
    public SparkDeploymentUtility sparkDeploymentUtility() {
        return SparkDeploymentUtility.builder()
                .sparkSubmitPath(sparkConfigurationProperties.sparkSubmitPath())
                .jarPath(sparkConfigurationProperties.jarPath())
                .mainClass(sparkConfigurationProperties.mainClass())
                .master(sparkConfigurationProperties.master())
                .mapper(mapper)
                .configFileLocation(sparkConfigurationProperties.configFileStagingLocation())
                .build();
    }

}
