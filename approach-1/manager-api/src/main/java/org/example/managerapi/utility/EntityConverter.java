package org.example.managerapi.utility;

import org.example.common.model.BusinessProduct;
import org.example.common.model.BusinessProductFileRequest;
import org.example.managerapi.model.BusinessProductEntity;
import org.example.managerapi.model.FileEventStatus;
import org.example.managerapi.model.FileEventStatusEntity;

public class EntityConverter {

    public static BusinessProductFileRequest convertToBusinessProductFileRequest(final FileEventStatusEntity fileEventStatusEntity) {
        return BusinessProductFileRequest.builder()
                .businessId(fileEventStatusEntity.getBusinessId())
                .productId(fileEventStatusEntity.getProductId())
                .filePath(fileEventStatusEntity.getFilePath())
                .requestId(fileEventStatusEntity.getRequestId())
                .build();
    }

    public static BusinessProduct convertToBusinessProduct(final BusinessProductEntity businessProductEntity) {
        return BusinessProduct.builder()
                .businessId(businessProductEntity.getBusinessId())
                .productId(businessProductEntity.getProductId())
                .splitSize(businessProductEntity.getSplitSize())
                .fieldLength(businessProductEntity.getFieldLength())
                .configurationSplitSize(businessProductEntity.getConfigurationSplitSize())
                .build();
    }

    public static FileEventStatus convertToFileEventStatus(final FileEventStatusEntity fileEventStatusEntity) {
        return FileEventStatus.builder()
                .businessId(fileEventStatusEntity.getBusinessId())
                .productId(fileEventStatusEntity.getProductId())
                .filePath(fileEventStatusEntity.getFilePath())
                .requestId(fileEventStatusEntity.getRequestId())
                .status(fileEventStatusEntity.getStatus())
                .currentStep(fileEventStatusEntity.getCurrentStep())
                .build();
    }

}
