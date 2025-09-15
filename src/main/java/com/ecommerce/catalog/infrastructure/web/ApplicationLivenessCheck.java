package com.ecommerce.catalog.infrastructure.web;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import java.time.LocalDateTime;

@Liveness
@ApplicationScoped
public class ApplicationLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("product-catalog-liveness")
                .status(true)
                .withData("timestamp", LocalDateTime.now().toString())
                .withData("service", "product-catalog-api")
                .build();
    }

}