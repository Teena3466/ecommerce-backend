package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AddressDTO;
import com.ecommerce.backend.dto.AddressResponseDTO;
import com.ecommerce.backend.entity.Address;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.AddressRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    public AddressService(
            AddressRepository addressRepository,
            UserService userService) {

        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    // Add new address
    public AddressResponseDTO addAddress(
            String email,
            AddressDTO addressDTO) {

        User user = userService.findByEmail(email);

        Address address = new Address();

        address.setUserId(user.getId());
        address.setFullName(addressDTO.getFullName());
        address.setPhone(addressDTO.getPhone());
        address.setAddressLine(addressDTO.getAddressLine());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPincode(addressDTO.getPincode());

        Address savedAddress =
                addressRepository.save(address);

        return convertToResponse(savedAddress);
    }

    // Get all addresses of logged-in user
    public List<AddressResponseDTO> getAddresses(
            String email) {

        User user = userService.findByEmail(email);

        return addressRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get one address of logged-in user
    public AddressResponseDTO getAddress(
            String email,
            Long addressId) {

        User user = userService.findByEmail(email);

        Address address = addressRepository
                .findByIdAndUserId(
                        addressId,
                        user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found"));

        return convertToResponse(address);
    }

    // Update address
    public AddressResponseDTO updateAddress(
            String email,
            Long addressId,
            AddressDTO addressDTO) {

        User user = userService.findByEmail(email);

        Address address = addressRepository
                .findByIdAndUserId(
                        addressId,
                        user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found"));

        address.setFullName(addressDTO.getFullName());
        address.setPhone(addressDTO.getPhone());
        address.setAddressLine(addressDTO.getAddressLine());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setPincode(addressDTO.getPincode());

        Address updatedAddress =
                addressRepository.save(address);

        return convertToResponse(updatedAddress);
    }

    // Delete address
    public void deleteAddress(
            String email,
            Long addressId) {

        User user = userService.findByEmail(email);

        Address address = addressRepository
                .findByIdAndUserId(
                        addressId,
                        user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Address not found"));

        addressRepository.delete(address);
    }

    // Convert Address to Response DTO
    private AddressResponseDTO convertToResponse(
            Address address) {

        return new AddressResponseDTO(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPincode()
        );
    }
}