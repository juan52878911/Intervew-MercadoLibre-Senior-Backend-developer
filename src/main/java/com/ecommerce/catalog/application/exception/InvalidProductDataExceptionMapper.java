package com.ecommerce.catalog.application.exception;

import com.ecommerce.catalog.domain.exception.InvalidProductDataException;
import com.ecommerce.catalog.infrastructure.web.dto.request.CauseResponseDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.ErrorResponseDto;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class InvalidProductDataExceptionMapper implements ExceptionMapper<InvalidProductDataException> {
    @Override
    public Response toResponse(InvalidProductDataException e) {
        CauseResponseDto cause = CauseResponseDto.builder()
                .department("items")
                .causeId(400)
                .type("error")
                .code("item.data.invalid")
                .references(List.of("product_data"))
                .message(e.getMessage())
                .build();

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .message("Validation error")
                .error("validation_error")
                .status(400)
                .cause(List.of(cause))
                .build();

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
