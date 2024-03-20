package org.example.common.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileRequestLineEvent implements Cloneable, Serializable {

    private Map<String, Object> fileRequest;
    private String requestId;
    private String businessId;
    private String productId;
    private String filePath;
    private Long recordNumber;
    private int fieldLength;
    private String fileName;
    private boolean isValid;
    private String errorMessage;
    private List<MatchModel> hits;
    private boolean postFilterApplied;

    @Override
    public FileRequestLineEvent clone() {
        try {
            return (FileRequestLineEvent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }

}
