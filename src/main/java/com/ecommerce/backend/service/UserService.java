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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // Register User
    public UserResponseDTO registerUser(UserDTO userDTO) {

        logger.info("Registering new user with email: {}",
                userDTO.getEmail());

        if (userRepository.existsByEmail(userDTO.getEmail())) {

            logger.warn("Registration failed. Email already exists: {}",
                    userDTO.getEmail());

            throw new EmailAlreadyExistsException(
                    "Email already registered");
        }

        User user = new User();

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());

        // Encrypt password before saving
        user.setPassword(
                passwordEncoder.encode(userDTO.getPassword())
        );

        // Default role for new users
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with id: {}",
                savedUser.getId());

        return new UserResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    // Login User
    public LoginResponseDTO loginUser(
            String email,
            String password) {

        logger.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {

                    logger.warn("Login failed. User not found: {}",
                            email);

                    return new InvalidCredentialsException(
                            "Invalid email or password");
                });

        // Check password
        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            logger.warn("Login failed. Invalid password for email: {}",
                    email);

            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        // Generate JWT with email + role
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        logger.info("Login successful for user id: {}",
                user.getId());

        return new LoginResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token
        );
    }

    // Find User By Email
    public User findByEmail(String email) {

        logger.info("Finding user by email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {

                    logger.warn("User not found with email: {}",
                            email);

                    return new UserNotFoundException(
                            "User not found");
                });
    }

    // Get User By ID
    public UserResponseDTO getUserById(Long id) {

        logger.info("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn("User not found with id: {}", id);

                    return new UserNotFoundException(
                            "User not found with id: " + id);
                });

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // Get All Users
    public List<UserResponseDTO> getAllUsers() {

        logger.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                ))
                .toList();
    }

    // Update User
    public UserResponseDTO updateUser(
            Long id,
            UserDTO userDTO) {

        logger.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn("Cannot update. User not found with id: {}",
                            id);

                    return new UserNotFoundException(
                            "User not found with id: " + id);
                });

        // Check if email is already used by another user
        if (!user.getEmail().equals(userDTO.getEmail())
                && userRepository.existsByEmail(userDTO.getEmail())) {

            logger.warn(
                    "Update failed. Email already registered: {}",
                    userDTO.getEmail());

            throw new EmailAlreadyExistsException(
                    "Email already registered");
        }

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());

        // Update password only if provided
        if (userDTO.getPassword() != null
                && !userDTO.getPassword().isBlank()) {

            logger.info("Updating password for user id: {}", id);

            user.setPassword(
                    passwordEncoder.encode(
                            userDTO.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        logger.info("User updated successfully with id: {}",
                updatedUser.getId());

        return new UserResponseDTO(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole()
        );
    }

    // Delete User
    public void deleteUser(Long id) {

        logger.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {

            logger.warn("Cannot delete. User not found with id: {}",
                    id);

            throw new UserNotFoundException(
                    "User not found with id: " + id);
        }

        userRepository.deleteById(id);

        logger.info("User deleted successfully with id: {}", id);
    }
}