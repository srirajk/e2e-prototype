package org.example.managerapi.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileEventStatus {

    private String businessId;
    private String productId;
    private String filePath;
    private String requestId;
    private String status;
    private String currentStep;

}
