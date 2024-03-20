package org.example.managerapi.config;


import lombok.extern.log4j.Log4j2;
import org.example.common.model.FileEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
@Log4j2
public class KafkaConfig {

    private KafkaProperties kafkaProperties;

    private ConsumerFactory<String, FileEvent> consumerFactory;

    public KafkaConfig(final KafkaProperties kafkaProperties, final ConsumerFactory<String, FileEvent> consumerFactory) {
        this.kafkaProperties = kafkaProperties;
        this.consumerFactory = consumerFactory;
    }



    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FileEvent> fileEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FileEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        //factory.setBatchListener(true); // Enable batch listening
        //factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        //factory.getContainerProperties().setIdleBetweenPolls(1000);
        return factory;
    }

}
