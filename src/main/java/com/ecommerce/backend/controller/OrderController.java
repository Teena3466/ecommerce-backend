package com.ecommerce.backend.controller;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create single-product order
    @PostMapping
    public Order createOrder(
            @Valid @RequestBody com.ecommerce.backend.dto.OrderDTO orderDTO,
            Authentication authentication) {

        String email = authentication.getName();

        return orderService.createOrder(
                email,
                orderDTO.getProductId(),
                orderDTO.getQuantity()
        );
    }

    // Checkout cart
    @PostMapping("/checkout")
    public List<Order> checkoutCart(
            Authentication authentication) {

        String email = authentication.getName();

        return orderService.checkoutCart(email);
    }

    // Get orders by user
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(
            @PathVariable Long userId,
            Authentication authentication) {

        String email = authentication.getName();

        return orderService.getOrdersByUser(
                userId,
                email
        );
    }

    // Cancel order
    @DeleteMapping("/{orderId}")
    public String cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email = authentication.getName();

        orderService.cancelOrder(
                orderId,
                email
        );

        return "Order cancelled successfully";
    }

    // Update order status
    @PutMapping("/{orderId}/status")
    public Order updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return orderService.updateOrderStatus(
                orderId,
                status
        );
    }
}