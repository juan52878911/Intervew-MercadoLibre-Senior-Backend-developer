package com.ecommerce.catalog.domain.exception;

import com.ecommerce.catalog.application.exception.ProductNotFoundExceptionMapper;
import com.ecommerce.catalog.infrastructure.web.dto.request.CauseResponseDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.ErrorResponseDto;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product Not Found Exception Mapper Tests")
class ProductNotFoundExceptionMapperTest {

    private ProductNotFoundExceptionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductNotFoundExceptionMapper();
    }

    @Test
    @DisplayName("Debe mapear excepción a respuesta 404")
    void shouldMapExceptionToNotFoundResponse() {
        // Given
        String errorMessage = "Product with ID 'MLA123456' not found";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);

        // Then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof ErrorResponseDto);

        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        assertEquals("Product not found", errorResponse.getMessage());
        assertEquals("not_found_error", errorResponse.getError());
        assertEquals(404, errorResponse.getStatus());
        assertNotNull(errorResponse.getCause());
        assertFalse(errorResponse.getCause().isEmpty());
    }

    @Test
    @DisplayName("Debe crear causa correcta en la respuesta")
    void shouldCreateCorrectCauseInResponse() {
        // Given
        String errorMessage = "Product not found in catalog";
        InvalidProductDataException exception = new InvalidProductDataException(errorMessage);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        CauseResponseDto cause = errorResponse.getCause().get(0);

        // Then
        assertEquals("items", cause.getDepartment());
        assertEquals(404, cause.getCauseId());
        assertEquals("error", cause.getType());
        assertEquals("item.not_found", cause.getCode());
        assertEquals(List.of("product_id"), cause.getReferences());
        assertEquals(errorMessage, cause.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje de excepción null")
    void shouldHandleNullExceptionMessage() {
        // Given
        InvalidProductDataException exception = new InvalidProductDataException(null);

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        CauseResponseDto cause = errorResponse.getCause().get(0);

        // Then
        assertEquals(404, response.getStatus());
        assertNull(cause.getMessage());
        assertEquals("Product not found", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Debe manejar mensaje de excepción vacío")
    void shouldHandleEmptyExceptionMessage() {
        // Given
        InvalidProductDataException exception = new InvalidProductDataException("");

        // When
        Response response = mapper.toResponse(exception);
        ErrorResponseDto errorResponse = (ErrorResponseDto) response.getEntity();
        CauseResponseDto cause = errorResponse.getCause().get(0);

        // Then
        assertEquals(404, response.getStatus());
        assertEquals("", cause.getMessage());
        assertEquals("Product not found", errorResponse.getMessage());
    }
}