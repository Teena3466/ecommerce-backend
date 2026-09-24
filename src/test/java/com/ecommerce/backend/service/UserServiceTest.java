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
// Test 8: Get All Users
@Test
void getAllUsers_shouldReturnAllUsers() {

    User user1 = new User();

    user1.setId(1L);
    user1.setName("Test User 1");
    user1.setEmail("user1@example.com");
    user1.setRole("USER");


    User user2 = new User();

    user2.setId(2L);
    user2.setName("Test User 2");
    user2.setEmail("user2@example.com");
    user2.setRole("USER");


    when(userRepository.findAll())
            .thenReturn(java.util.List.of(user1, user2));


    java.util.List<UserResponseDTO> response =
            userService.getAllUsers();


    assertEquals(2, response.size());

    assertEquals(1L, response.get(0).getId());
    assertEquals("Test User 1", response.get(0).getName());
    assertEquals("user1@example.com", response.get(0).getEmail());

    assertEquals(2L, response.get(1).getId());
    assertEquals("Test User 2", response.get(1).getName());
    assertEquals("user2@example.com", response.get(1).getEmail());
}
// Test 9: Update User
@Test
void updateUser_shouldUpdateAndReturnResponse() {

    UserDTO userDTO = new UserDTO();

    userDTO.setName("Updated User");
    userDTO.setEmail("updated@example.com");
    userDTO.setPassword("newpassword123");


    User existingUser = new User();

    existingUser.setId(1L);
    existingUser.setName("Old User");
    existingUser.setEmail("old@example.com");
    existingUser.setPassword("oldEncodedPassword");
    existingUser.setRole("USER");


    User updatedUser = new User();

    updatedUser.setId(1L);
    updatedUser.setName("Updated User");
    updatedUser.setEmail("updated@example.com");
    updatedUser.setPassword("newEncodedPassword");
    updatedUser.setRole("USER");


    when(userRepository.findById(1L))
            .thenReturn(Optional.of(existingUser));

    when(userRepository.existsByEmail("updated@example.com"))
            .thenReturn(false);

    when(passwordEncoder.encode("newpassword123"))
            .thenReturn("newEncodedPassword");

    when(userRepository.save(any(User.class)))
            .thenReturn(updatedUser);


    UserResponseDTO response =
            userService.updateUser(1L, userDTO);


    assertEquals(1L, response.getId());
    assertEquals("Updated User", response.getName());
    assertEquals("updated@example.com", response.getEmail());
    assertEquals("USER", response.getRole());
}
// Test 10: Update User - Duplicate Email
@Test
void updateUser_shouldThrowExceptionWhenEmailAlreadyExists() {

    UserDTO userDTO = new UserDTO();

    userDTO.setName("Updated User");
    userDTO.setEmail("existing@example.com");
    userDTO.setPassword("newpassword123");


    User existingUser = new User();

    existingUser.setId(1L);
    existingUser.setName("Old User");
    existingUser.setEmail("old@example.com");
    existingUser.setPassword("oldEncodedPassword");
    existingUser.setRole("USER");


    when(userRepository.findById(1L))
            .thenReturn(Optional.of(existingUser));

    when(userRepository.existsByEmail("existing@example.com"))
            .thenReturn(true);


    assertThrows(
            EmailAlreadyExistsException.class,
            () -> userService.updateUser(1L, userDTO)
    );
}
// Test 11: Delete User
@Test
void deleteUser_shouldDeleteUser() {

    when(userRepository.existsById(1L))
            .thenReturn(true);

    userService.deleteUser(1L);

    org.mockito.Mockito.verify(
            userRepository
    ).deleteById(1L);
}
// Test 12: Delete User - User Not Found
@Test
void deleteUser_shouldThrowExceptionWhenUserNotFound() {

    when(userRepository.existsById(99L))
            .thenReturn(false);

    assertThrows(
            UserNotFoundException.class,
            () -> userService.deleteUser(99L)
    );
}

}