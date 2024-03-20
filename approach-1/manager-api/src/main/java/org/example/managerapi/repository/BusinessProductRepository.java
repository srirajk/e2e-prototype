package org.example.managerapi.repository;

import org.example.managerapi.model.BusinessProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessProductRepository extends JpaRepository<BusinessProductEntity, Integer>{
    BusinessProductEntity findByBusinessIdAndProductId(final String businessId, final String productId);
}
