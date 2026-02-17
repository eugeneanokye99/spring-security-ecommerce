package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.AddressMapperStruct;
import com.shopjoy.dto.request.CreateAddressRequest;
import com.shopjoy.dto.request.UpdateAddressRequest;
import com.shopjoy.dto.response.AddressResponse;
import com.shopjoy.entity.Address;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.repository.AddressRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.AddressService;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Address service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AddressServiceImpl implements AddressService {
    
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapperStruct addressMapper;
    
    @Override
    @Transactional()
    public AddressResponse createAddress(CreateAddressRequest request) {
        Address address = addressMapper.toAddress(request);
        address.setUser(userRepository.getReferenceById(request.getUserId()));
        address.setCreatedAt(LocalDateTime.now());
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(savedAddress);
    }
    
    @Override
    public AddressResponse getAddressById(Integer addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return addressMapper.toAddressResponse(address);
    }
    
    @Override
    public List<AddressResponse> getAddressesByUser(Integer userId) {

        List<Address> addresses = addressRepository.findByUser_Id(userId);
        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    public AddressResponse updateAddress(Integer addressId, UpdateAddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        addressMapper.updateAddressFromRequest(request, address);
        
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(updatedAddress);
    }
    
    @Override
    @Transactional()
    public void deleteAddress(Integer addressId) {
        if (!addressRepository.existsById(addressId)) {
            throw new ResourceNotFoundException("Address", "id", addressId);
        }
        
        addressRepository.deleteById(addressId);
    }
    
    @Override
    @Transactional()
    public AddressResponse setDefaultAddress(Integer addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        addressRepository.findByUser_IdAndIsDefaultTrue(address.getUser().getId())
                .ifPresent(currentDefault -> {
                    currentDefault.setDefault(false);
                    addressRepository.save(currentDefault);
                });

        address.setDefault(true);
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(updatedAddress);
    }
    
    @Override
    public AddressResponse getDefaultAddress(Integer userId) {
        Address address = addressRepository.findByUser_IdAndIsDefaultTrue(userId)
                .orElse(null);
        return address != null ? addressMapper.toAddressResponse(address) : null;
    }
}
