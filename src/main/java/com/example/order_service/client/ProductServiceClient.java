package com.example.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;


import java.util.UUID;

@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductServiceClient {


    @GetMapping("/{id}")
    ProductDto getProduct(@PathVariable("id") UUID id);
}
