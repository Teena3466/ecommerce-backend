package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.LoginRequestDTO;
import com.ecommerce.backend.dto.LoginResponseDTO;
import com.ecommerce.backend.dto.UserDTO;
import com.ecommerce.backend.dto.UserResponseDTO;
import com.ecommerce.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Register User
    @PostMapping("/register")
    public UserResponseDTO registerUser(
            @Valid @RequestBody UserDTO userDTO) {

        return userService.registerUser(userDTO);
    }

    // Login User
    @PostMapping("/login")
    public LoginResponseDTO loginUser(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        return userService.loginUser(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
    }

    // Get User By ID
    @GetMapping("/{id}")
    public UserResponseDTO getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);
    }

    // Get All Users
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {

        return userService.getAllUsers();
    }

    // Update User
    @PutMapping("/{id}")
    public UserResponseDTO updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {

        return userService.updateUser(id, userDTO);
    }

    // Delete User
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "User deleted successfully";
    }
}