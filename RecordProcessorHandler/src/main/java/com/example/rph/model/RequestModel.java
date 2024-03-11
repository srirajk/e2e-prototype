package com.example.rph.model;

import com.example.rph.utility.Utility;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestModel {
    private String fileName;
    private String requestId;
    private ObjectNode data;
    private Long recordNumber;
    private Long fieldLength;
    private Long recordCount;
}

