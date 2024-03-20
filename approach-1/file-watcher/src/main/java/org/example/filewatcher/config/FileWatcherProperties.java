package org.example.filewatcher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.watcher")
public record FileWatcherProperties(String directory, String filePattern,
                                    Long pollInterval, Long quietPeriod) {}
