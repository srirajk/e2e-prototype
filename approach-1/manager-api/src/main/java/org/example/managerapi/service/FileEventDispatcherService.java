package org.example.managerapi.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.example.common.model.BusinessProduct;
import org.example.common.model.BusinessProductFileRequest;
import org.example.common.model.FileEvent;
import org.example.common.model.SparkFileSplitRequest;
import org.example.common.utility.SparkDeploymentUtility;
import org.example.managerapi.model.BusinessProductEntity;
import org.example.managerapi.model.FileEventStatusEntity;
import org.example.managerapi.repository.BusinessProductRepository;
import org.example.managerapi.repository.FileEventStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class FileEventDispatcherService {

    private BusinessProductRepository businessProductRepository;
    private FileEventStatusRepository fileEventStatusRepository;
    private final ObjectMapper mapper;
    private final SparkDeploymentUtility sparkDeploymentUtility;

    @Value("${kafka.file-record-events-topic}")
    private String fileRecordEventsTopic;

    @Value("${kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    public FileEventDispatcherService(final BusinessProductRepository businessProductRepository,
                                      final FileEventStatusRepository fileEventStatusRepository,
                                      final ObjectMapper mapper,
                                      final SparkDeploymentUtility sparkDeploymentUtility) {
        this.businessProductRepository = businessProductRepository;
        this.fileEventStatusRepository = fileEventStatusRepository;
        this.mapper = mapper;
        this.sparkDeploymentUtility = sparkDeploymentUtility;
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
        final BusinessProductFileRequest businessProductFileRequest = convertToBusinessProductFileRequest(fileEventStatusEntity);
        final BusinessProduct businessProduct = convertToBusinessProduct(businessProductEntity);
        final SparkFileSplitRequest sparkFileSplitRequest = SparkFileSplitRequest.builder()
                .businessProductFileRequest(businessProductFileRequest)
                .businessProduct(businessProduct)
                .kafkaProducerProperties(Map.of("bootstrap-servers", kafkaBootstrapServers,
                        "topic", fileRecordEventsTopic))
                .build();
        final ObjectNode config = mapper.convertValue(sparkFileSplitRequest, ObjectNode.class);
        sparkDeploymentUtility.deploySparkJob("FileSplitProcessor-" + requestId,
                1,
                1,
                1,
                config, Collections.emptyList());
    }

    private BusinessProductFileRequest convertToBusinessProductFileRequest(final FileEventStatusEntity fileEventStatusEntity) {
        return BusinessProductFileRequest.builder()
                .businessId(fileEventStatusEntity.getBusinessId())
                .productId(fileEventStatusEntity.getProductId())
                .filePath(fileEventStatusEntity.getFilePath())
                .requestId(fileEventStatusEntity.getRequestId())
                .build();
    }

    private BusinessProduct convertToBusinessProduct(final BusinessProductEntity businessProductEntity) {
        return BusinessProduct.builder()
                .businessId(businessProductEntity.getBusinessId())
                .productId(businessProductEntity.getProductId())
                .splitSize(businessProductEntity.getSplitSize())
                .fieldLength(businessProductEntity.getFieldLength())
                .build();
    }

}
