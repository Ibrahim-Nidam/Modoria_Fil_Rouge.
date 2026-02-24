package com.modoria.domain.user.repository;

import com.modoria.domain.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    // Helper to unset other defaults
    List<Address> findByUserIdAndIsDefaultTrueAndIdNot(Long userId, Long addressId);
}
