package org.example.managerapi.service;


import org.example.common.model.BusinessProduct;
import org.example.managerapi.model.BusinessProductEntity;
import org.example.managerapi.model.FileEventStatus;
import org.example.managerapi.model.FileEventStatusEntity;
import org.example.managerapi.repository.BusinessProductRepository;
import org.example.managerapi.repository.FileEventStatusRepository;
import org.example.managerapi.utility.EntityConverter;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BusinessProductLookupService {

    private final BusinessProductRepository businessProductRepository;
    private final FileEventStatusRepository fileEventStatusRepository;


    public BusinessProductLookupService(BusinessProductRepository businessProductRepository, FileEventStatusRepository fileEventStatusRepository) {
        this.businessProductRepository = businessProductRepository;
        this.fileEventStatusRepository = fileEventStatusRepository;
    }

    public BusinessProduct getBusinessProductMetadata(final String businessId, final String productId) {
        BusinessProductEntity byBusinessIdAndProductId = this.businessProductRepository.findByBusinessIdAndProductId(businessId, productId);
        if (Objects.isNull(byBusinessIdAndProductId)) {
            throw new RuntimeException("Business Product not found");
        } else {
            return EntityConverter.convertToBusinessProduct(byBusinessIdAndProductId);
        }
    }


    public FileEventStatus getFileEventStatus(final String requestId) {
        FileEventStatusEntity fileEventStatusEntity = this.fileEventStatusRepository.findByRequestId(requestId);
        if (Objects.isNull(fileEventStatusEntity)) {
            throw new RuntimeException("File Event Status not found");
        } else {
            return EntityConverter.convertToFileEventStatus(fileEventStatusEntity);
        }
    }

}
