package com.modoria.domain.user.service.impl;

import com.modoria.domain.user.dto.request.AddressRequest;
import com.modoria.domain.user.dto.response.AddressResponse;
import com.modoria.domain.user.entity.Address;
import com.modoria.domain.user.entity.User;
import com.modoria.domain.user.mapper.AddressMapper;
import com.modoria.domain.user.repository.AddressRepository;
import com.modoria.domain.user.repository.UserRepository;
import com.modoria.domain.user.service.AddressService;
import com.modoria.infrastructure.exceptions.resource.ResourceNotFoundException;
import com.modoria.infrastructure.exceptions.auth.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressResponse create(AddressRequest request) {
        User user = getCurrentUser();

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        // If this is the first address, make it default automatically
        if (addressRepository.findByUserId(user.getId()).isEmpty()) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(request.getIsDefault())) {
            // If new address is set as default, unset others
            unsetOtherDefaults(user.getId());
        }

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getById(Long id) {
        Address address = getAddressIfAuthorized(id);
        return addressMapper.toResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getCurrentUserAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse update(Long id, AddressRequest request) {
        Address address = getAddressIfAuthorized(id);

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            unsetOtherDefaults(address.getUser().getId());
        }

        addressMapper.updateEntityFromRequest(request, address);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public void delete(Long id) {
        Address address = getAddressIfAuthorized(id);
        addressRepository.delete(address);
    }

    @Override
    public AddressResponse setDefault(Long id) {
        Address address = getAddressIfAuthorized(id);

        if (!Boolean.TRUE.equals(address.getIsDefault())) {
            unsetOtherDefaults(address.getUser().getId());
            address.setIsDefault(true);
            addressRepository.save(address);
        }

        return addressMapper.toResponse(address);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Address getAddressIfAuthorized(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        User currentUser = getCurrentUser();
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to access this address");
        }

        return address;
    }

    private void unsetOtherDefaults(Long userId) {
        List<Address> defaults = addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(List::of)
                .orElse(List.of());

        for (Address addr : defaults) {
            addr.setIsDefault(false);
            addressRepository.save(addr);
        }
    }
}
