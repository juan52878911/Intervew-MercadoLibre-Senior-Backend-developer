package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResultDto {
    private int totalProcessed;
    private int successful;
    private int failed;
    private String message;
}
