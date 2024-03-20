package org.example.managerapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "file_event_status",
        uniqueConstraints = @UniqueConstraint(columnNames = {"businessId", "productId", "filePath", "requestId"}))
public class FileEventStatusEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String businessId;
    private String productId;
    @Column(unique = true)
    private String filePath;
    private String requestId;
    private String status;
    private String currentStep;

}
