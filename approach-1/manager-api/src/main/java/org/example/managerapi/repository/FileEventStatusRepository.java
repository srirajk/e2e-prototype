package org.example.managerapi.repository;

import org.example.managerapi.model.FileEventStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileEventStatusRepository extends JpaRepository<FileEventStatusEntity, Integer> {

    FileEventStatusEntity findByBusinessIdAndProductIdAndFilePath(final String businessId, final String productId, final String filePath);
    FileEventStatusEntity findByBusinessIdAndProductIdAndRequestId(final String businessId, final String productId, final String requestId);
    FileEventStatusEntity findByRequestId(final String requestId);
}
