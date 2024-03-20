package org.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessProduct implements Serializable {

    private String businessId;
    private String productId;
    private Integer splitSize;
    private Integer fieldLength;

}
