package org.example.managerapi.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.common.model.FileEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class FileEventListener {


    @Value("${kafka.file-events-topic}")
    private String fileWatcherEventsTopicName;

    private final FileEventDispatcherService fileEventDispatcherService;

    public ObjectMapper mapper;

    public FileEventListener(FileEventDispatcherService fileEventDispatcherService, ObjectMapper mapper) {
        this.fileEventDispatcherService = fileEventDispatcherService;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = "#{__listener.fileWatcherEventsTopicName}",
            containerFactory = "fileEventKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, String> fileEventRecord) throws JsonProcessingException {
        final FileEvent fileEvent = mapper.readValue(fileEventRecord.value(), FileEvent.class);
        log.info("Received file event: {}", fileEvent);
        fileEventDispatcherService.dispatchFileEvent(fileEvent);
    }

    public String getFileWatcherEventsTopicName() {
        return fileWatcherEventsTopicName;
    }

}
