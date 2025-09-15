package com.ecommerce.catalog.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CauseResponseDto {

    @JsonProperty("department")
    private String department;

    @JsonProperty("cause_id")
    private Integer causeId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("code")
    private String code;

    @JsonProperty("references")
    private List<String> references;

    @JsonProperty("message")
    private String message;
}