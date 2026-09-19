package com.ecommerce.backend.dto;

import com.ecommerce.backend.entity.PaymentMethod;
import com.ecommerce.backend.entity.PaymentStatus;

public class PaymentResponseDTO {

    private Long id;
    private Long orderId;
    private double amount;
    private PaymentStatus status;
    private PaymentMethod method;

    public PaymentResponseDTO() {
    }

    public PaymentResponseDTO(
            Long id,
            Long orderId,
            double amount,
            PaymentStatus status,
            PaymentMethod method) {

        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.method = method;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
}