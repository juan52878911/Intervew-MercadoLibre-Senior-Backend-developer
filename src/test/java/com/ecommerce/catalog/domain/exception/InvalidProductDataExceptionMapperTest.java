package com.ecommerce.catalog.domain.exception;

import com.ecommerce.catalog.application.exception.InvalidProductDataExceptionMapper;
import com.ecommerce.catalog.infrastructure.web.dto.request.CauseResponseDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.ErrorResponseDto;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Invalid Product Data Exception Mapper Tests")
class InvalidProductDataExceptionMapperTest {

    private InvalidProductDataExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InvalidProductDataExceptionMapper();
    }

    @Test
    @DisplayName("Debe mapear excepción a respuesta 400")
    void shouldMapExceptionToBadRequestResponse() {
        // Given
        String errorMessage = "Invalid product data: price cannot be negative";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof ErrorResponseDto);

        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        assertEquals("Validation error", errorResponse.getMessage());
        assertEquals("validation_error", errorResponse.getError());
        assertEquals(400, errorResponse.getStatus());
        assertNotNull(errorResponse.getCause());
        assertFalse(errorResponse.getCause().isEmpty());
    }

    @Test
    @DisplayName("Debe crear causa correcta para datos inválidos")
    void shouldCreateCorrectCauseForInvalidData() {
        // Given
        String errorMessage = "Title field is required and cannot be empty";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        CauseResponseDto cause = errorResponse.getCause().get(0);

        // Then
        assertEquals("items", cause.getDepartment());
        assertEquals(400, cause.getCauseId());
        assertEquals("error", cause.getType());
        assertEquals("item.data.invalid", cause.getCode());
        assertEquals(List.of("product_data"), cause.getReferences());
        assertEquals(errorMessage, cause.getMessage());
    }

    @Test
    @DisplayName("Debe manejar validación con múltiples errores")
    void shouldHandleValidationWithMultipleErrors() {
        // Given
        String errorMessage = "Multiple validation errors: price is negative, title is empty, category is invalid";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("validation_error", errorResponse.getError());
        assertEquals(1, errorResponse.getCause().size());
        assertEquals(errorMessage, errorResponse.getCause().get(0).getMessage());
    }

    @Test
    @DisplayName("Debe manejar excepción con mensaje null")
    void shouldHandleExceptionWithNullMessage() {
        // Given
        InvalidProductDataException exception = new InvalidProductDataException(null);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("Validation error", errorResponse.getMessage());
        assertNull(errorResponse.getCause().get(0).getMessage());
    }
}