package org.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessProduct {

    private String businessId;
    private String productId;
    private Integer splitSize;
    private Integer fieldLength;

}
