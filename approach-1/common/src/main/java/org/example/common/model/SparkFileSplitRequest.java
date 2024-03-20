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
public class SparkFileSplitRequest implements SparkConfigRequest {

   private BusinessProduct businessProduct;
   private BusinessProductFileRequest businessProductFileRequest;
   private Map<String, Object> kafkaProducerProperties;

}
