package org.example.filewatcher.config;

import lombok.extern.log4j.Log4j2;
import org.example.filewatcher.utility.FileWatcher;
import org.example.filewatcher.utility.KafkaProducer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(FileWatcherProperties.class)
@Log4j2
public class FileWatcherConfiguration {

    private final FileWatcherProperties fileWatcherProperties;
    private final KafkaProducer kafkaProducer;

    public FileWatcherConfiguration(FileWatcherProperties fileWatcherProperties, final KafkaProducer kafkaProducer) {
        this.fileWatcherProperties = fileWatcherProperties;
        this.kafkaProducer = kafkaProducer;
    }

    @Bean
    FileWatcher fileSystemWatcher() {
        return new FileWatcher(
                fileWatcherProperties.directory(),
                Duration.ofMinutes(fileWatcherProperties.quietPeriod()).toMillis(),
                Duration.ofMinutes(fileWatcherProperties.pollInterval()).toMillis(),
                fileWatcherProperties.filePattern(), kafkaProducer);
    }

}
