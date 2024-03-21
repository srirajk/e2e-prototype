package org.example.managerapi.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.example.common.model.BusinessProduct;
import org.example.common.model.BusinessProductFileRequest;
import org.example.common.model.FileEvent;
import org.example.common.model.SparkFileSplitRequest;
import org.example.common.utility.SparkDeploymentUtility;
import org.example.common.utility.Utility;
import org.example.managerapi.model.BusinessProductEntity;
import org.example.managerapi.model.FileEventStatusEntity;
import org.example.managerapi.repository.BusinessProductRepository;
import org.example.managerapi.repository.FileEventStatusRepository;
import org.example.managerapi.utility.EntityConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.managerapi.utility.EntityConverter.convertToBusinessProduct;

@Service
@Log4j2
public class FileEventDispatcherService {

    private final BusinessProductRepository businessProductRepository;
    private final FileEventStatusRepository fileEventStatusRepository;
    private final ObjectMapper mapper;
    private final SparkDeploymentUtility sparkDeploymentUtility;

    @Value("${kafka.file-record-events-topic}")
    private String fileRecordEventsTopic;

    @Value("${kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    public FileEventDispatcherService(final BusinessProductRepository businessProductRepository,
                                      final FileEventStatusRepository fileEventStatusRepository,
                                      final ObjectMapper mapper,
                                     @Qualifier("fileSplitJobDeploymentUtility") final SparkDeploymentUtility fileSplitJobDeploymentUtility) {
        this.businessProductRepository = businessProductRepository;
        this.fileEventStatusRepository = fileEventStatusRepository;
        this.mapper = mapper;
        this.sparkDeploymentUtility = fileSplitJobDeploymentUtility;
    }

    public void dispatchFileEvent(final FileEvent fileEvent) {
        final FileEventStatusEntity existingEntity = fileEventStatusRepository.findByBusinessIdAndProductIdAndFilePath(fileEvent.businessId(),
                fileEvent.productId(),
                fileEvent.filePath());
        if (Objects.isNull(existingEntity)) {
            final FileEventStatusEntity entity = FileEventStatusEntity.builder()
                    .filePath(fileEvent.filePath())
                    .requestId(UUID.randomUUID().toString())
                    .status("INITIALIZED")
                    .businessId(fileEvent.businessId())
                    .productId(fileEvent.productId())
                    .currentStep("INITIALIZED")
                    .build();
            FileEventStatusEntity savedEntity = fileEventStatusRepository.save(entity);
            triggerSparkBatchJob(savedEntity);
        } else {
            log.info("IGNORED Received existing file event: {}", fileEvent);
        }
    }

    private void triggerSparkBatchJob(final FileEventStatusEntity fileEventStatusEntity) {
        final String requestId = fileEventStatusEntity.getRequestId();
        final BusinessProductEntity businessProductEntity = businessProductRepository.findByBusinessIdAndProductId(fileEventStatusEntity.getBusinessId(),
                fileEventStatusEntity.getProductId());
        final BusinessProductFileRequest businessProductFileRequest = EntityConverter.convertToBusinessProductFileRequest(fileEventStatusEntity);
        final BusinessProduct businessProduct = EntityConverter.convertToBusinessProduct(businessProductEntity);
        final SparkFileSplitRequest sparkFileSplitRequest = SparkFileSplitRequest.builder()
                .businessProductFileRequest(businessProductFileRequest)
                .businessProduct(businessProduct)
                .kafkaProducerProperties(Map.of("bootstrap-servers", kafkaBootstrapServers,
                        "topic", fileRecordEventsTopic))
                .exclusionMarker(1)
                .build();
        final ObjectNode config = mapper.convertValue(sparkFileSplitRequest, ObjectNode.class);
        sparkDeploymentUtility.deploySparkJob("FileSplitProcessor-" + requestId,
                2,
                1,
                1,
                config, Collections.emptyList());
    }



}
