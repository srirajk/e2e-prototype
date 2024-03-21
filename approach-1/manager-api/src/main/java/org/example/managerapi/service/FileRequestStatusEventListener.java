package org.example.managerapi.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class FileRequestStatusEventListener {


    @Value("${kafka.file-request-status-updates-topic}")
    private String fileRequestStatusUpdatesTopic;

    private final FileEventDispatcherService fileEventDispatcherService;

    private final ObjectMapper mapper;

    public FileRequestStatusEventListener(FileEventDispatcherService fileEventDispatcherService, ObjectMapper mapper) {
        this.fileEventDispatcherService = fileEventDispatcherService;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = "#{__listener.fileRequestStatusUpdatesTopic}",
            containerFactory = "fileEventKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, String> fileEventRecord) throws JsonProcessingException {
        ObjectNode data = mapper.readValue(fileEventRecord.value(), ObjectNode.class);
        log.info("Received file status event: {}", data);
    }

    public String getFileRequestStatusUpdatesTopic() {
        return fileRequestStatusUpdatesTopic;
    }

}
