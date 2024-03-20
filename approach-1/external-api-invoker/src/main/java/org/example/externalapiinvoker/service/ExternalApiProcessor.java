package org.example.externalapiinvoker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.example.common.model.FileRequestLineEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ExternalApiProcessor {

    private final ObjectMapper objectMapper;
    private final KafkaProducerService kafkaProducerService;



    public ExternalApiProcessor(final ObjectMapper objectMapper,
                                final KafkaProducerService kafkaProducerService) {
        this.objectMapper = objectMapper;
        this.kafkaProducerService = kafkaProducerService;
    }



    public FileRequestLineEvent processRequestLine(final FileRequestLineEvent fileRequestLineEvent) {
        log.info("Started Processing request line event {}", fileRequestLineEvent);
        final FileRequestLineEvent responseEvent = fileRequestLineEvent.clone();
        final List<ObjectNode> hits = executeExternalApi(fileRequestLineEvent);
        responseEvent.setHits(hits);
        kafkaProducerService.publishEvent(responseEvent);
        log.info("Published response event to kafka topic {} for request id {}", responseEvent.getRequestId(), responseEvent);
        return responseEvent;
    }

    private List<ObjectNode> executeExternalApi(final FileRequestLineEvent fileRequestLineEvent) {
        ObjectNode hit = objectMapper.createObjectNode();
        hit.put("key", "value");
        return List.of(hit);
    }
}
