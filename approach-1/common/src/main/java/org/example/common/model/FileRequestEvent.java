package org.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileRequestEvent {

    private Map<String, Object> fileRequest;
    private String requestId;
    private String businessId;
    private String productId;
    private String filePath;
    private Long recordNumber;
    private boolean isValid;
    private String errorMessage;

}
