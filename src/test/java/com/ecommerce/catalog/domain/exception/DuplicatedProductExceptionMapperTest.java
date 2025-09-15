package com.ecommerce.catalog.domain.exception;

import com.ecommerce.catalog.application.exception.DuplicatedProductExceptionMapper;
import com.ecommerce.catalog.infrastructure.web.dto.request.CauseResponseDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.ErrorResponseDto;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Duplicated Product Exception Mapper Tests")
class DuplicatedProductExceptionMapperTest {

    private DuplicatedProductExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DuplicatedProductExceptionMapper();
    }

    @Test
    @DisplayName("Debe mapear excepción a respuesta 409")
    void shouldMapExceptionToConflictResponse() {
        // Given
        String errorMessage = "Product with ID 'MLA123456' already exists";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof ErrorResponseDto);

        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        assertEquals("Duplicate product error", errorResponse.getMessage());
        assertEquals("duplicate_error", errorResponse.getError());
        assertEquals(409, errorResponse.getStatus());
        assertNotNull(errorResponse.getCause());
        assertFalse(errorResponse.getCause().isEmpty());
    }

    @Test
    @DisplayName("Debe crear causa correcta para producto duplicado")
    void shouldCreateCorrectCauseForDuplicateProduct() {
        // Given
        String errorMessage = "Cannot create product: SKU already exists in catalog";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        CauseResponseDto cause = errorResponse.getCause().get(0);

        // Then
        assertEquals("items", cause.getDepartment());
        assertEquals(409, cause.getCauseId());
        assertEquals("error", cause.getType());
        assertEquals("item.duplicate", cause.getCode());
        assertEquals(List.of("product_id"), cause.getReferences());
        assertEquals(errorMessage, cause.getMessage());
    }

    @Test
    @DisplayName("Debe manejar duplicado por diferentes campos")
    void shouldHandleDuplicateByDifferentFields() {
        // Given
        String errorMessage = "Product with title 'iPhone 15 Pro' already exists in the same category";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();

        // Then
        assertEquals(409, response.getStatus());
        assertEquals("duplicate_error", errorResponse.getError());
        assertEquals("Duplicate product error", errorResponse.getMessage());
        assertEquals(errorMessage, errorResponse.getCause().get(0).getMessage());
    }

    @Test
    @DisplayName("Debe manejar excepción de duplicado con mensaje vacío")
    void shouldHandleDuplicateExceptionWithEmptyMessage() {
        // Given
        InvalidProductDataException exception = new InvalidProductDataException("");

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();

        // Then
        assertEquals(409, response.getStatus());
        assertEquals("Duplicate product error", errorResponse.getMessage());
        assertEquals("", errorResponse.getCause().get(0).getMessage());
    }

    @Test
    @DisplayName("Debe manejar excepción de duplicado con mensaje null")
    void shouldHandleDuplicateExceptionWithNullMessage() {
        // Given
        InvalidProductDataException exception = new InvalidProductDataException(null);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();

        // Then
        assertEquals(409, response.getStatus());
        assertEquals("Duplicate product error", errorResponse.getMessage());
        assertNull(errorResponse.getCause().get(0).getMessage());
    }
}