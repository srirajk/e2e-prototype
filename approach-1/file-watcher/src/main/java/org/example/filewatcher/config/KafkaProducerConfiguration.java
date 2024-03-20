package org.example.filewatcher.config;

import org.example.common.model.FileEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfiguration {

    private KafkaProperties kafkaProperties;

    public KafkaProducerConfiguration(@Autowired final KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, FileEvent> producerFactory() {
       return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null));
    }

    @Bean
    public KafkaTemplate<String, FileEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


}
