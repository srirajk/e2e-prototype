package org.example.externalapiinvoker.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.common.model.FileRequestLineEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Log4j2
public class FileRequestLineListener {

    @Value("${kafka.file-record-line-events-topic}")
    public String fileRecordLineEventsTopic;

    private final ExternalApiProcessor externalApiProcessor;

    private final ObjectMapper mapper;

    public FileRequestLineListener(final ExternalApiProcessor externalApiProcessor, final ObjectMapper mapper){
        this.externalApiProcessor = externalApiProcessor;
        this.mapper = mapper;
    }


    @KafkaListener(
            topics = "#{__listener.fileRecordLineEventsTopic}",
            containerFactory = "fileRecordLineKafkaListenerContainerFactory")
    public void commonListenerForMultipleTopics(ConsumerRecord<String, String> record) {
        Map<String, byte[]> currentRecordHeaders = StreamSupport.stream(record.headers().spliterator(), false)
                .collect(Collectors.toMap(header -> header.key(), header -> header.value()));
        final String requestId = currentRecordHeaders.containsKey("requestId") ? new String(currentRecordHeaders.get("requestId")) : "";
        if (StringUtils.isNotBlank(requestId)) {
            FileRequestLineEvent fileRequestLineEvent = null;
            try {
                fileRequestLineEvent = mapper.readValue(record.value(), FileRequestLineEvent.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            externalApiProcessor.processRequestLine(fileRequestLineEvent);
        } else {
            log.debug("ignore on offset :: {}", record.offset());
        }
    }



}
