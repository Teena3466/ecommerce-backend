package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.PaymentDTO;
import com.ecommerce.backend.dto.PaymentResponseDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.Payment;
import com.ecommerce.backend.entity.PaymentMethod;
import com.ecommerce.backend.entity.PaymentStatus;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.PaymentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentService paymentService;

    // Test 1: Successful Payment
    @Test
    void makePayment_shouldCreateSuccessfulPayment() {

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole("USER");

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalPrice(100000);
        order.setStatus(OrderStatus.PLACED);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setOrderId(1L);
        paymentDTO.setMethod(PaymentMethod.UPI);

        Payment savedPayment = new Payment();
        savedPayment.setId(1L);
        savedPayment.setUserId(1L);
        savedPayment.setOrderId(1L);
        savedPayment.setAmount(100000);
        savedPayment.setMethod(PaymentMethod.UPI);
        savedPayment.setStatus(PaymentStatus.SUCCESS);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        PaymentResponseDTO response =
                paymentService.makePayment(
                        "test@example.com",
                        paymentDTO
                );

        assertEquals(1L, response.getId());
        assertEquals(1L, response.getOrderId());
        assertEquals(100000, response.getAmount());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(PaymentMethod.UPI, response.getMethod());
    }
    // Test 2: Order Not Found
@Test
void makePayment_shouldThrowExceptionWhenOrderNotFound() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    PaymentDTO paymentDTO = new PaymentDTO();
    paymentDTO.setOrderId(99L);
    paymentDTO.setMethod(PaymentMethod.UPI);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(orderRepository.findById(99L))
            .thenReturn(Optional.empty());

    assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.makePayment(
                    "test@example.com",
                    paymentDTO
            )
    );
}
}