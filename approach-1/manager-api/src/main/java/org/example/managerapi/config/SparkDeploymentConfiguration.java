package org.example.managerapi.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.utility.SparkDeploymentUtility;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableConfigurationProperties(SparkConfigurationProperties.class)
public class SparkDeploymentConfiguration {

    private SparkConfigurationProperties sparkConfigurationProperties;
    private final ObjectMapper mapper;

    private final FileSplitJobProperties fileSplitJobProperties;
    private final SparkOutputFileWriterProperties sparkOutputFileWriterProperties;
    public SparkDeploymentConfiguration(final ObjectMapper mapper,
                                        final FileSplitJobProperties fileSplitJobProperties,
                                        final SparkOutputFileWriterProperties sparkOutputFileWriterProperties) {
        this.mapper = mapper;
        this.fileSplitJobProperties = fileSplitJobProperties;
        this.sparkOutputFileWriterProperties = sparkOutputFileWriterProperties;
    }

    @Bean("fileSplitJobDeploymentUtility")
    public SparkDeploymentUtility fileSplitSparkDeploymentUtility() {
        return SparkDeploymentUtility.builder()
                .sparkSubmitPath(fileSplitJobProperties.getSparkSubmitPath())
                .jarPath(fileSplitJobProperties.getJarPath())
                .mainClass(fileSplitJobProperties.getMainClass())
                .master(fileSplitJobProperties.getMaster())
                .logFileAppBaseLocation(fileSplitJobProperties.getLogFileAppBaseLocation())
                .sparkJobType(fileSplitJobProperties.getAppType())
                .mapper(mapper)
                .configFileAppBaseLocation(fileSplitJobProperties.getConfigFileStagingLocation())
                .build();
    }

@Bean("sparkOutputFileWriterDeploymentUtility")
public SparkDeploymentUtility sparkOutputFileWriterDeploymentUtility() {
    return SparkDeploymentUtility.builder()
            .sparkSubmitPath(sparkOutputFileWriterProperties.getSparkSubmitPath())
            .jarPath(sparkOutputFileWriterProperties.getJarPath())
            .mainClass(sparkOutputFileWriterProperties.getMainClass())
            .master(sparkOutputFileWriterProperties.getMaster())
            .logFileAppBaseLocation(sparkOutputFileWriterProperties.getLogFileAppBaseLocation())
            .sparkJobType(sparkOutputFileWriterProperties.getAppType())
            .mapper(mapper)
            .configFileAppBaseLocation(sparkOutputFileWriterProperties.getConfigFileStagingLocation())
            .build();
}

}


/*
@Configuration
public class SparkDeploymentConfiguration {

    private final ObjectMapper mapper;
    private final FileSplitJobProperties fileSplitJobProperties;
    private final SparkOutputFileWriterProperties sparkOutputFileWriterProperties;

    public SparkDeploymentConfiguration(final ObjectMapper mapper,
                                        final FileSplitJobProperties fileSplitJobProperties,
                                        final SparkOutputFileWriterProperties sparkOutputFileWriterProperties) {
        this.mapper = mapper;
        this.fileSplitJobProperties = fileSplitJobProperties;
        this.sparkOutputFileWriterProperties = sparkOutputFileWriterProperties;
    }

    @Bean
    public SparkDeploymentUtility fileSplitJobDeploymentUtility() {
        return createSparkDeploymentUtility(fileSplitJobProperties);
    }

    @Bean
    public SparkDeploymentUtility sparkOutputFileWriterDeploymentUtility() {
        return createSparkDeploymentUtility(sparkOutputFileWriterProperties);
    }

    private SparkDeploymentUtility createSparkDeploymentUtility(SparkJobProperties properties) {
        return new SparkDeploymentUtility(
                properties.getSparkSubmitPath(),
                properties.getJarPath(),
                properties.getMainClass(),
                properties.getMaster(),
                mapper,
                properties.getConfigFileStagingLocation(),
                properties.getLogFileAppBaseLocation(),
                properties.getAppType()
        );
    }
}
*/
