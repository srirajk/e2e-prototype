package com.example.rph.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilteredRecordRiskModel {

    private String fileName;
    private String requestId;
    private ObjectNode data;
    private Long recordNumber;
    private Long fieldLength;
    private List<Risk> risks;
    private boolean hit;

}
