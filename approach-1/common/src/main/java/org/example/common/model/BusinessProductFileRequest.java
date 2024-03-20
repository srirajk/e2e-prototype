package org.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessProductFileRequest {

    private String businessId;
    private String productId;
    private String filePath;
    private String requestId;

}
