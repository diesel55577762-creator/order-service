package com.example.order_service.model.dto;

import com.example.order_service.model.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProductDto {
    private UUID id;

    @NotBlank(message = "name not found")
    private String name;

    @NotBlank(message = "description not found")
    private String description;

    @NotNull(message = "categoryId not found")
    private UUID categoryId;

    @NotNull(message = "price not found")
    private BigDecimal price;

    @NotNull(message = "status not found")
    private ProductStatus status;

}
