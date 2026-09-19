package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AddressDTO;
import com.ecommerce.backend.dto.AddressResponseDTO;
import com.ecommerce.backend.service.AddressService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // Add address
    @PostMapping
    public AddressResponseDTO addAddress(
            @Valid @RequestBody AddressDTO addressDTO,
            Authentication authentication) {

        String email = authentication.getName();

        return addressService.addAddress(
                email,
                addressDTO
        );
    }

    // Get all addresses
    @GetMapping
    public List<AddressResponseDTO> getAddresses(
            Authentication authentication) {

        String email = authentication.getName();

        return addressService.getAddresses(email);
    }

    // Get one address
    @GetMapping("/{addressId}")
    public AddressResponseDTO getAddress(
            @PathVariable Long addressId,
            Authentication authentication) {

        String email = authentication.getName();

        return addressService.getAddress(
                email,
                addressId
        );
    }

    // Update address
    @PutMapping("/{addressId}")
    public AddressResponseDTO updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO,
            Authentication authentication) {

        String email = authentication.getName();

        return addressService.updateAddress(
                email,
                addressId,
                addressDTO
        );
    }

    // Delete address
    @DeleteMapping("/{addressId}")
    public String deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication) {

        String email = authentication.getName();

        addressService.deleteAddress(
                email,
                addressId
        );

        return "Address deleted successfully";
    }
}