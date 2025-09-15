package com.ecommerce.catalog.application.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ACTIVE("active"),
    PAUSED("paused"),
    CLOSED("closed");

    private final String value;
}

