package org.example.externalapiinvoker.service;

import lombok.extern.log4j.Log4j2;
import org.example.common.model.FileEvent;
import org.example.common.model.FileRequestLineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class KafkaProducerService {

    @Value("${kafka.file-record-events-processed-topic}")
    private String fileRecordLineEventsTopic;

    private KafkaTemplate<String, FileRequestLineEvent> kafkaTemplate;

    private RetryTemplate retryTemplate;


    public KafkaProducerService(@Autowired final KafkaTemplate<String, FileRequestLineEvent> kafkaTemplate,
                                @Autowired final RetryTemplate retryTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.retryTemplate = retryTemplate;
    }


    public void publishEvent(final FileRequestLineEvent fileRequestLineEvent) {
        retryTemplate.execute(context -> {
            final var key = fileRequestLineEvent.getRequestId()+"-"+ fileRequestLineEvent.getRecordNumber();
            CompletableFuture<SendResult<String, FileRequestLineEvent>> future = kafkaTemplate.send(fileRecordLineEventsTopic, key, fileRequestLineEvent);
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
