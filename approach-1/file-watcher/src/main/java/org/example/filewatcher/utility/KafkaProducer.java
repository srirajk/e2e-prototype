package org.example.filewatcher.utility;

import lombok.extern.log4j.Log4j2;
import org.example.common.model.FileEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Log4j2
public class KafkaProducer {

    private KafkaTemplate<String, FileEvent> kafkaTemplate;

    private RetryTemplate retryTemplate;

    @Value("${kafka.topic}")
    private String topic;

    public KafkaProducer(@Autowired final KafkaTemplate<String, FileEvent> kafkaTemplate,
                         @Autowired final RetryTemplate retryTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.retryTemplate = retryTemplate;
    }

    public void publishEvent(final FileEvent fileEvent) {
        retryTemplate.execute(context -> {
            CompletableFuture<SendResult<String, FileEvent>> future = kafkaTemplate.send(topic, fileEvent.eventType(), fileEvent);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Error publishing message to Kafka: " + ex.getMessage());
                    throw new RuntimeException(ex);
                } else {
                    log.info("Message published to Kafka: " + result.getRecordMetadata().offset());
                }
            });
            return true;
        });
    }

}
