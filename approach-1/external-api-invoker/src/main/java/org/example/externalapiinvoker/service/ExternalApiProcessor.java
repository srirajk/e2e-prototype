package org.example.externalapiinvoker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.example.common.model.FileRequestLineEvent;
import org.example.common.model.MatchModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

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
        final List<MatchModel> hits = executeExternalApi(fileRequestLineEvent);
        responseEvent.setHits(hits);
        kafkaProducerService.publishEvent(responseEvent);
        log.info("Published response event to kafka topic {} for request id {}", responseEvent.getRequestId(), responseEvent);
        return responseEvent;
    }

    private List<MatchModel> executeExternalApi(final FileRequestLineEvent fileRequestLineEvent) {
        Long recordNumber = fileRequestLineEvent.getRecordNumber();
        List<MatchModel> matches = List.of();
        if (recordNumber % 2 == 0) {
            Random rand = new Random();
            for (int i = 0; i < 3; i++) {
                MatchModel model = new MatchModel();
                model.setDescription("Description of the match" + rand.nextInt(100)); // Generate a random description
                model.setMatchId(String.valueOf(rand.nextInt(100))); // Generate a random match ID
                model.setMatchableItemId(String.valueOf(rand.nextInt(100))); // Generate a random matchable item ID
                matches.add(model);
            }
        }
        return matches;
    }
}
