package com.ecommerce.catalog.infrastructure.web;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application Liveness Check Tests")
class ApplicationLivenessCheckTest {

    private ApplicationLivenessCheck livenessCheck;

    @BeforeEach
    void setUp() {
        livenessCheck = new ApplicationLivenessCheck();
    }

    @Test
    @DisplayName("Debe retornar respuesta UP del health check")
    void shouldReturnUpHealthCheckResponse() {
        // When
        HealthCheckResponse response = livenessCheck.call();

        // Then
        assertNotNull(response, "La respuesta del health check no debe ser null");
        assertEquals("product-catalog-liveness", response.getName(), "El nombre debe coincidir");
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus(), "El estado debe ser UP");
    }

    @Test
    @DisplayName("Debe incluir timestamp en los datos")
    void shouldIncludeTimestampInData() {
        // Given
        LocalDateTime beforeCall = LocalDateTime.now();

        // When
        HealthCheckResponse response = livenessCheck.call();

        // Then
        assertTrue(response.getData().isPresent(), "Debe tener datos");
        assertTrue(response.getData().get().containsKey("timestamp"), "Debe contener timestamp");

        String timestampStr = response.getData().get().get("timestamp").toString();
        assertNotNull(timestampStr, "El timestamp no debe ser null");
        assertFalse(timestampStr.isBlank(), "El timestamp no debe estar vacío");

        // Verificar que el timestamp es reciente (dentro de 1 segundo)
        LocalDateTime timestamp = LocalDateTime.parse(timestampStr);
        assertTrue(timestamp.isAfter(beforeCall.minusSeconds(1)), "El timestamp debe ser reciente");
    }

    @Test
    @DisplayName("Debe incluir información del servicio")
    void shouldIncludeServiceInformation() {
        // When
        HealthCheckResponse response = livenessCheck.call();

        // Then
        assertTrue(response.getData().isPresent(), "Debe tener datos");
        assertTrue(response.getData().get().containsKey("service"), "Debe contener información del servicio");
        assertEquals("product-catalog-api", response.getData().get().get("service"),
                "El nombre del servicio debe coincidir");
    }

    @Test
    @DisplayName("Debe ser consistente en múltiples llamadas")
    void shouldBeConsistentAcrossMultipleCalls() {
        // When
        HealthCheckResponse response1 = livenessCheck.call();
        HealthCheckResponse response2 = livenessCheck.call();

        // Then
        assertEquals(response1.getName(), response2.getName(), "El nombre debe ser consistente");
        assertEquals(response1.getStatus(), response2.getStatus(), "El estado debe ser consistente");

        // El servicio debe ser el mismo
        assertEquals(
                response1.getData().get().get("service"),
                response2.getData().get().get("service"),
                "La información del servicio debe ser consistente"
        );

        // Los timestamps deben ser diferentes (llamadas en momentos distintos)
        assertNotEquals(
                response1.getData().get().get("timestamp"),
                response2.getData().get().get("timestamp"),
                "Los timestamps deben ser diferentes en llamadas separadas"
        );
    }

    @Test
    @DisplayName("Debe tener exactamente dos datos")
    void shouldHaveExactlyTwoDataFields() {
        // When
        HealthCheckResponse response = livenessCheck.call();

        // Then
        assertTrue(response.getData().isPresent(), "Debe tener datos");
        assertEquals(2, response.getData().get().size(), "Debe tener exactamente 2 campos de datos");
        assertTrue(response.getData().get().containsKey("timestamp"), "Debe contener timestamp");
        assertTrue(response.getData().get().containsKey("service"), "Debe contener service");
    }

    @Test
    @DisplayName("Debe ejecutarse sin excepciones")
    void shouldExecuteWithoutExceptions() {
        // When & Then
        assertDoesNotThrow(() -> {
            HealthCheckResponse response = livenessCheck.call();
            assertNotNull(response);
        }, "La llamada al health check no debe lanzar excepciones");
    }
}
