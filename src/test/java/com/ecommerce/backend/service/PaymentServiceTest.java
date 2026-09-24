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

import java.util.List;
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


    // =========================================================
    // Test 1: Successful Payment
    // =========================================================
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


    // =========================================================
    // Test 2: Order Not Found
    // =========================================================
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


    // =========================================================
    // Test 3: Unauthorized Payment
    // =========================================================
    @Test
    void makePayment_shouldThrowExceptionWhenUserPaysForAnotherUsersOrder() {

        User loggedInUser = new User();
        loggedInUser.setId(1L);
        loggedInUser.setEmail("test@example.com");
        loggedInUser.setRole("USER");

        User orderOwner = new User();
        orderOwner.setId(2L);
        orderOwner.setEmail("owner@example.com");
        orderOwner.setRole("USER");

        Order order = new Order();
        order.setId(1L);
        order.setUser(orderOwner);
        order.setTotalPrice(100000);
        order.setStatus(OrderStatus.PLACED);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setOrderId(1L);
        paymentDTO.setMethod(PaymentMethod.UPI);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(loggedInUser);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        "test@example.com",
                        paymentDTO
                )
        );
    }


    // =========================================================
    // Test 4: Payment for Delivered Order
    // =========================================================
    @Test
    void makePayment_shouldThrowExceptionWhenOrderIsNotPlaced() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole("USER");

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalPrice(100000);
        order.setStatus(OrderStatus.DELIVERED);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setOrderId(1L);
        paymentDTO.setMethod(PaymentMethod.UPI);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        "test@example.com",
                        paymentDTO
                )
        );
    }


    // =========================================================
    // Test 5: Payment Already Exists
    // =========================================================
    @Test
    void makePayment_shouldThrowExceptionWhenPaymentAlreadyExists() {

        User user = new User();
        user.setId(1L);
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

        Payment existingPayment = new Payment();
        existingPayment.setId(1L);
        existingPayment.setOrderId(1L);
        existingPayment.setUserId(1L);
        existingPayment.setAmount(100000);
        existingPayment.setMethod(PaymentMethod.UPI);
        existingPayment.setStatus(PaymentStatus.SUCCESS);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrderId(1L))
                .thenReturn(Optional.of(existingPayment));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.makePayment(
                        "test@example.com",
                        paymentDTO
                )
        );
    }


    // =========================================================
    // Test 6: Get My Payments
    // =========================================================
    @Test
    void getMyPayments_shouldReturnUserPayments() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole("USER");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setUserId(1L);
        payment.setOrderId(1L);
        payment.setAmount(100000);
        payment.setMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESS);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(paymentRepository.findByUserId(1L))
                .thenReturn(List.of(payment));

        List<PaymentResponseDTO> response =
                paymentService.getMyPayments("test@example.com");

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(1L, response.get(0).getOrderId());
        assertEquals(100000, response.get(0).getAmount());
        assertEquals(PaymentStatus.SUCCESS, response.get(0).getStatus());
        assertEquals(PaymentMethod.UPI, response.get(0).getMethod());
    }


    // =========================================================
    // Test 7: Get My Payments - No Payments
    // =========================================================
    @Test
    void getMyPayments_shouldReturnEmptyListWhenNoPaymentsExist() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole("USER");

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(paymentRepository.findByUserId(1L))
                .thenReturn(List.of());

        List<PaymentResponseDTO> response =
                paymentService.getMyPayments("test@example.com");

        assertEquals(0, response.size());
    }


    // =========================================================
    // Test 8: Get Payment Successfully
    // =========================================================
    @Test
    void getPayment_shouldReturnPaymentSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole("USER");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setUserId(1L);
        payment.setOrderId(1L);
        payment.setAmount(100000);
        payment.setMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESS);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        PaymentResponseDTO response =
                paymentService.getPayment(
                        "test@example.com",
                        1L
                );

        assertEquals(1L, response.getId());
        assertEquals(1L, response.getOrderId());
        assertEquals(100000, response.getAmount());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(PaymentMethod.UPI, response.getMethod());
    }


    // =========================================================
    // Test 9: Payment Not Found
    // =========================================================
    @Test
    void getPayment_shouldThrowExceptionWhenPaymentNotFound() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole("USER");

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(paymentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.getPayment(
                        "test@example.com",
                        99L
                )
        );
    }


    // =========================================================
    // Test 10: Unauthorized Payment Access
    // =========================================================
    @Test
    void getPayment_shouldThrowExceptionWhenUserAccessesAnotherUsersPayment() {

        User loggedInUser = new User();
        loggedInUser.setId(1L);
        loggedInUser.setEmail("test@example.com");
        loggedInUser.setRole("USER");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setUserId(2L);
        payment.setOrderId(1L);
        payment.setAmount(100000);
        payment.setMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESS);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(loggedInUser);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.getPayment(
                        "test@example.com",
                        1L
                )
        );
    }
}