package org.example.managerapi.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.common.model.BusinessProduct;
import org.example.common.model.BusinessProductFileRequest;
import org.example.common.model.SparkMergeDataRequest;
import org.example.common.utility.SparkDeploymentUtility;
import org.example.managerapi.model.BusinessProductEntity;
import org.example.managerapi.model.FileEventStatusEntity;
import org.example.managerapi.repository.BusinessProductRepository;
import org.example.managerapi.repository.FileEventStatusRepository;
import org.example.managerapi.utility.EntityConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collections;
import java.util.Map;

@Service
@Log4j2
public class FileRequestStatusEventListener {


    @Getter
    @Value("${kafka.file-request-status-updates-topic}")
    private String fileRequestStatusUpdatesTopic;

    @Value("${file-records-delta-table-location}")
    private String deltaLakeTableLocation;

    @Value("${base-staging-area}")
    private String baseStagingArea;

    @Value("${kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;
    private final FileEventDispatcherService fileEventDispatcherService;

    private final SparkDeploymentUtility sparkOutputFileWriterDeploymentUtility;
    private final ObjectMapper mapper;
    private final BusinessProductRepository businessProductRepository;
    private final FileEventStatusRepository fileEventStatusRepository;

    public FileRequestStatusEventListener(FileEventDispatcherService fileEventDispatcherService, ObjectMapper mapper,
                                          @Qualifier("sparkOutputFileWriterDeploymentUtility") final SparkDeploymentUtility sparkOutputFileWriterDeploymentUtility,
                                          final BusinessProductRepository businessProductRepository,
                                          final FileEventStatusRepository fileEventStatusRepository) {
        this.fileEventDispatcherService = fileEventDispatcherService;
        this.sparkOutputFileWriterDeploymentUtility = sparkOutputFileWriterDeploymentUtility;
        this.businessProductRepository = businessProductRepository;
        this.fileEventStatusRepository = fileEventStatusRepository;
        this.mapper = mapper;
    }

    @KafkaListener(
            topics = "#{__listener.fileRequestStatusUpdatesTopic}",
            containerFactory = "fileEventKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, String> fileEventRecord) throws JsonProcessingException {
        ObjectNode data = mapper.readValue(fileEventRecord.value(), ObjectNode.class);
        final String businessId = data.get("businessId").asText();
        final String productId = data.get("productId").asText();
        final String requestId = data.get("requestId").asText();
        final long totalRecords = data.get("totalRecords").asLong();
        final long processedRecords = data.get("totalRecordsProcessed").asLong();
        final String source = data.get("source").asText();
        final BusinessProductEntity businessProductEntity = businessProductRepository.findByBusinessIdAndProductId(businessId,
                productId);
        FileEventStatusEntity fileEventStatusEntity = fileEventStatusRepository.findByBusinessIdAndProductIdAndRequestId(businessId, productId, requestId);
        // ReadAndSplitFileRequest, SparkStreamingStateNotifier, SparkOutputFileWriter
        if (totalRecords == processedRecords) {
            String outputDataLocation = baseStagingArea + File.separator + "outputs" + File.separator + businessId + File.separator + productId + File.separator + requestId;
            final BusinessProductFileRequest businessProductFileRequest = EntityConverter.convertToBusinessProductFileRequest(fileEventStatusEntity);
            final BusinessProduct businessProduct = EntityConverter.convertToBusinessProduct(businessProductEntity);
            if (StringUtils.equalsAnyIgnoreCase("SparkStreamingStateNotifier", source)) {
                final SparkMergeDataRequest sparkMergeDataRequest = SparkMergeDataRequest.builder()
                        .businessProduct(businessProduct)
                        .businessProductFileRequest(businessProductFileRequest)
                        .deltaTableLocation(deltaLakeTableLocation)
                        .outputDataLocation(outputDataLocation)
                        .totalRecords(processedRecords)
                        .kafkaProducerProperties(Map.of("bootstrap-servers", kafkaBootstrapServers))
                        .build();
                fileEventStatusEntity.setCurrentStep("SparkOutputFileWriter - INITIALIZED");
                final ObjectNode inputConfig = mapper.convertValue(sparkMergeDataRequest, ObjectNode.class);
                sparkOutputFileWriterDeploymentUtility.deploySparkJob("FileOutputWriter-" + requestId,
                        2,
                        1,
                        1,
                        inputConfig, Collections.emptyList());
            } else {
                fileEventStatusEntity.setStatus("COMPLETED");
                fileEventStatusEntity.setCurrentStep(String.format("BATCHFILE GENERATED - %s", outputDataLocation));
            }
        } else {
            fileEventStatusEntity.setStatus("IN_PROGRESS");
            if (StringUtils.equalsAnyIgnoreCase("SparkStreamingStateNotifier", source)) {
                fileEventStatusEntity.setCurrentStep(source + String.format(" - %d/%d", processedRecords, totalRecords));
            } else {
                fileEventStatusEntity.setCurrentStep(source);
            }
        }
        fileEventStatusRepository.save(fileEventStatusEntity);
        log.info("Received file status event: {}", data);
    }

}
