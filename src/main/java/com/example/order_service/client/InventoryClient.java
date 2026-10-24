package com.example.order_service.client;

import com.example.order_service.model.enums.StatusReservedItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "inventory-service", url = "${services.inventory-service.url}")
public interface InventoryClient {

    @PostMapping("/inventory/reserve")
    StatusReservedItem reserveProduct(@RequestParam UUID productId,
                                      @RequestParam Integer quantity);

    @PostMapping("/inventory/release")
    void releaseProduct(@RequestParam UUID productId,
                        @RequestParam Integer quantity);
}