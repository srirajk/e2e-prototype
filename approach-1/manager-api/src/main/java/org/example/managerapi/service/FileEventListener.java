package org.example.managerapi.service;


import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.common.model.FileEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class FileEventListener {


    @Value("${kafka.file-event-topic}")
    private String fileWatcherEventTopicName;

    private final FileEventDispatcherService fileEventDispatcherService;

    public FileEventListener(FileEventDispatcherService fileEventDispatcherService) {
        this.fileEventDispatcherService = fileEventDispatcherService;
    }

    @KafkaListener(
            topics = "#{__listener.fileWatcherEventTopicName}",
            containerFactory = "fileEventKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, FileEvent> fileEventRecord) {
        final FileEvent fileEvent = fileEventRecord.value();
        log.info("Received file event: {}", fileEvent);
        fileEventDispatcherService.dispatchFileEvent(fileEvent);
    }

    public String getFileWatcherEventTopicName() {
        return fileWatcherEventTopicName;
    }

}
