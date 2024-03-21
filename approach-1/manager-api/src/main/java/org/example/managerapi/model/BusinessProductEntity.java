package org.example.managerapi.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "business_product",
        uniqueConstraints = @UniqueConstraint(columnNames = {"businessId", "productId"}))
public class BusinessProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String businessId;
    private String productId;
    private Integer splitSize;
    private Integer fieldLength;
    private Integer configurationSplitSize;

}
