package com.modoria.domain.user.service;

import com.modoria.domain.user.dto.request.AddressRequest;
import com.modoria.domain.user.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    AddressResponse create(AddressRequest request);

    AddressResponse getById(Long id);

    List<AddressResponse> getCurrentUserAddresses();

    AddressResponse update(Long id, AddressRequest request);

    void delete(Long id);

    AddressResponse setDefault(Long id);
}
