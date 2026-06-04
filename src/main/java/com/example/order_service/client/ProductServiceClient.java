package com.example.order_service.client;

import com.example.order_service.model.dto.ProductDto;
import com.example.order_service.model.dto.ProductInfoRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductServiceClient {

    @Operation(summary = "Получение списка продуктов по id товара")
    @GetMapping("/products/by-ids")
    List<ProductDto> getProductInfoRequest(@RequestBody ProductInfoRequest dto);
}
