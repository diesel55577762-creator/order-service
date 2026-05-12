package com.example.order_service.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class CustomerDto {
    private UUID id;

    @NotNull(message = "Имя не может быть пустым")
    private String name;

    @NotNull(message = "Email обязателен")
    private String email;

    @NotNull(message = "Компания обязательна")
    private String companyName;

    private Instant createdAt;
}

