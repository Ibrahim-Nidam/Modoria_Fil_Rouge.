package com.modoria.domain.user.controller;

import com.modoria.domain.user.dto.request.AddressRequest;
import com.modoria.domain.user.dto.response.AddressResponse;
import com.modoria.domain.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Address", description = "User Address Book management")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Create a new address")
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all addresses for current user")
    public ResponseEntity<List<AddressResponse>> getCurrentUserAddresses() {
        return ResponseEntity.ok(addressService.getCurrentUserAddresses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<AddressResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        addressService.delete(id);
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<AddressResponse> setDefault(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefault(id));
    }
}
