package org.example.externalapiinvoker.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.common.model.FileEvent;
import org.example.common.model.FileRequestLineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

@Configuration
public class KafkaConfiguration {

    private KafkaProperties kafkaProperties;

    private ConsumerFactory<String, String> consumerFactory;

    public KafkaConfiguration(@Autowired final KafkaProperties kafkaProperties, @Autowired ConsumerFactory<String, String> consumerFactory) {
        this.kafkaProperties = kafkaProperties;
        this.consumerFactory = consumerFactory;
    }

    @Bean
    public ProducerFactory<String, FileRequestLineEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null));
    }

    @Bean
    public KafkaTemplate<String, FileRequestLineEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

/*
    @Bean
    public ConsumerFactory<String, FileRequestLineEvent> consumerFactory() {
        DefaultKafkaConsumerFactory defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(null));
        return defaultKafkaConsumerFactory;
    }
*/

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> fileRecordLineKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        //factory.setBatchListener(true); // Enable batch listening
        //factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        //factory.getContainerProperties().setIdleBetweenPolls(1000);
        return factory;
    }


}
