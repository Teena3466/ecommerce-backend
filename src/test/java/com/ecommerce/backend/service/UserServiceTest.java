package com.ecommerce.backend.service;


import com.ecommerce.backend.dto.LoginResponseDTO;
import com.ecommerce.backend.dto.UserDTO;
import com.ecommerce.backend.dto.UserResponseDTO;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.EmailAlreadyExistsException;
import com.ecommerce.backend.exception.InvalidCredentialsException;
import com.ecommerce.backend.exception.UserNotFoundException;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;


    // Test 1: Register User
    @Test
    void registerUser_shouldSaveUserAndReturnResponse() {

        UserDTO userDTO = new UserDTO();

        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password123");


        User savedUser = new User();

        savedUser.setId(1L);
        savedUser.setName("Test User");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole("USER");


        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);


        UserResponseDTO response =
                userService.registerUser(userDTO);


        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("USER", response.getRole());
    }


    // Test 2: Register User - Duplicate Email
    @Test
    void registerUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        UserDTO userDTO = new UserDTO();

        userDTO.setName("Another User");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password123");


        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);


        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(userDTO)
        );
    }


    // Test 3: Login User - Successful Login
    @Test
    void loginUser_shouldReturnLoginResponseWhenCredentialsAreCorrect() {

        User user = new User();

        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");


        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));


        when(passwordEncoder.matches(
                "password123",
                "encodedPassword"))
                .thenReturn(true);


        when(jwtUtil.generateToken(
                "test@example.com",
                "USER"))
                .thenReturn("test-jwt-token");


        LoginResponseDTO response =
                userService.loginUser(
                        "test@example.com",
                        "password123");


        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("test-jwt-token", response.getToken());
    }
    // Test 4: Login User - Wrong Password
@Test
void loginUser_shouldThrowExceptionWhenPasswordIsWrong() {

    User user = new User();

    user.setId(1L);
    user.setName("Test User");
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
    user.setRole("USER");

    when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(
            "wrongPassword",
            "encodedPassword"))
            .thenReturn(false);

    assertThrows(
            InvalidCredentialsException.class,
            () -> userService.loginUser(
                    "test@example.com",
                    "wrongPassword")
    );
}
// Test 5: Login User - Email Not Found
@Test
void loginUser_shouldThrowExceptionWhenEmailDoesNotExist() {

    when(userRepository.findByEmail("unknown@example.com"))
            .thenReturn(Optional.empty());

    assertThrows(
            InvalidCredentialsException.class,
            () -> userService.loginUser(
                    "unknown@example.com",
                    "password123")
    );
}
// Test 6: Get User By ID
@Test
void getUserById_shouldReturnUserResponse() {

    User user = new User();

    user.setId(1L);
    user.setName("Test User");
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
    user.setRole("USER");

    when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

    UserResponseDTO response =
            userService.getUserById(1L);

    assertEquals(1L, response.getId());
    assertEquals("Test User", response.getName());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("USER", response.getRole());
}
// Test 7: Get User By ID - User Not Found
@Test
void getUserById_shouldThrowExceptionWhenUserNotFound() {

    when(userRepository.findById(99L))
            .thenReturn(Optional.empty());

    assertThrows(
            UserNotFoundException.class,
            () -> userService.getUserById(99L)
    );
}
}