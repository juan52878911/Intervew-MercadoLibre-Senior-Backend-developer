package com.ecommerce.catalog.application.exception;

import com.ecommerce.catalog.domain.exception.InvalidProductDataException;
import com.ecommerce.catalog.infrastructure.web.dto.request.CauseResponseDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.ErrorResponseDto;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ProductNotFoundExceptionMapper implements ExceptionMapper<InvalidProductDataException> {
    @Override
    public Response toResponse(InvalidProductDataException e) {
        CauseResponseDto cause = CauseResponseDto.builder()
                .department("items")
                .causeId(404)
                .type("error")
                .code("item.not_found")
                .references(List.of("product_id"))
                .message(e.getMessage())
                .build();

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message("Product not found")
                .error("not_found_error")
                .status(404)
                .cause(List.of(cause))
                .build();

        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }
}