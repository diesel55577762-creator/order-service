package com.example.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "inventory-service", url = "${services.inventory-service.url}")
public interface InventoryClient {

    @PostMapping("/inventoryItems/reserve")
    void reserveProduct(@RequestParam UUID productId,
                        @RequestParam Integer quantity);

    @PostMapping("/inventoryItems/release")
    void releaseProduct(@RequestParam UUID productId,
                        @RequestParam Integer quantity);
}