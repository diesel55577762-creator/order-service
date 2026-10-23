package com.example.order_service;

import com.example.order_service.client.InventoryClient;
import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.model.dto.OrderDto;
import com.example.order_service.model.dto.OrderItemDto;
import com.example.order_service.model.dto.ProductDto;
import com.example.order_service.model.entity.Customer;
import com.example.order_service.model.enums.OutboxStatus;
import com.example.order_service.model.enums.Status;
import com.example.order_service.model.enums.StatusReservedItem;
import com.example.order_service.repository.CustomerRepository;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.OutboxEventRepository;
import com.example.order_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private ProductServiceClient productServiceClient;

    @MockitoBean
    private InventoryClient inventoryClient;

    // Очистка базы перед каждым тестом!
    @BeforeEach
    void cleanDatabase() {
        outboxEventRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        Customer customer = createAndSaveCustomer();

        UUID productId = UUID.randomUUID();
        ProductDto productDto = ProductDto.builder()
                .id(productId)
                .name("Телефон")
                .price(BigDecimal.valueOf(999.99))
                .build();

        when(productServiceClient.getProductInfoRequest(any()))
                .thenReturn(List.of(productDto));
        when(inventoryClient.reserveProduct(any(), any()))
                .thenReturn(StatusReservedItem.SUCCESS);

        OrderItemDto item = OrderItemDto.builder()
                .productId(productId)
                .quantity(2)
                .build();

        OrderDto dto = OrderDto.builder()
                .customerId(customer.getId())
                .orderItems(List.of(item))
                .build();

        OrderDto order = orderService.create(dto);

        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(Status.NEW);
        assertThat(order.getTotalAmount()).isPositive();

        var saved = orderRepository.findById(order.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    void shouldFailWhenInsufficientStock() {
        Customer customer = createAndSaveCustomer();

        UUID productId = UUID.randomUUID();
        ProductDto productDto = ProductDto.builder()
                .id(productId)
                .name("Телефон")
                .price(BigDecimal.valueOf(999.99))
                .build();

        when(productServiceClient.getProductInfoRequest(any()))
                .thenReturn(List.of(productDto));
        when(inventoryClient.reserveProduct(any(), any()))
                .thenReturn(StatusReservedItem.INSUFFICIENT_STOCK);

        OrderItemDto item = OrderItemDto.builder()
                .productId(productId)
                .quantity(100)
                .build();

        OrderDto dto = OrderDto.builder()
                .customerId(customer.getId())
                .orderItems(List.of(item))
                .build();

        boolean exceptionThrown = false;
        try {
            orderService.create(dto);
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }

        assertThat(exceptionThrown).isTrue();
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    void shouldSendOrderCreatedEventToKafka() {
        Customer customer = createAndSaveCustomer();

        UUID productId = UUID.randomUUID();
        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setName("Телефон");
        productDto.setPrice(BigDecimal.valueOf(999.99));

        when(productServiceClient.getProductInfoRequest(any()))
                .thenReturn(List.of(productDto));
        when(inventoryClient.reserveProduct(any(), any()))
                .thenReturn(StatusReservedItem.SUCCESS);

        OrderItemDto item = OrderItemDto.builder()
                .productId(productId)
                .quantity(2)
                .build();
        OrderDto dto = OrderDto.builder()
                .customerId(customer.getId())
                .orderItems(List.of(item))
                .build();

        OrderDto created = orderService.create(dto);

        // Проверяем, что запись в Outbox создалась
        var outbox = outboxEventRepository.findByAggregateId(created.getId());
        assertThat(outbox).isPresent();
        assertThat(outbox.get().getStatus()).isEqualTo(OutboxStatus.NEW);

        // Ждем, пока Outbox-publisher отправит сообщение в Kafka (до 15 секунд)
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var updated = outboxEventRepository.findByAggregateId(created.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getStatus()).isEqualTo(OutboxStatus.SENT);
        });
    }

    @Test
    void shouldCreateOrderOnlyOnce() {
        Customer customer = createAndSaveCustomer();

        UUID productId = UUID.randomUUID();
        ProductDto productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setName("Телефон");
        productDto.setPrice(BigDecimal.valueOf(999.99));

        when(productServiceClient.getProductInfoRequest(any()))
                .thenReturn(List.of(productDto));
        when(inventoryClient.reserveProduct(any(), any()))
                .thenReturn(StatusReservedItem.SUCCESS);

        UUID orderId = UUID.randomUUID();
        OrderItemDto item = OrderItemDto.builder()
                .productId(productId)
                .quantity(2)
                .build();
        OrderDto dto = OrderDto.builder()
                .id(orderId) // Передаем ID для идемпотентности
                .customerId(customer.getId())
                .orderItems(List.of(item))
                .build();

        orderService.create(dto);
        orderService.create(dto); // Второй вызов не должен создать дубль

        long count = orderRepository.findAll().size();
        assertThat(count).isEqualTo(1);
    }

    private Customer createAndSaveCustomer() {
        Customer customer = new Customer();
        customer.setName("Иван Петров");
        customer.setEmail("ivan@test.com");
        customer.setCompanyName("ООО Тест");
        return customerRepository.save(customer);
    }
}