package com.shopjoy.repository;

import com.shopjoy.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUserId(int userId);
    
    @Query("SELECT a FROM Address a WHERE a.userId = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddress(@Param("userId") int userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
    void clearDefaultAddresses(@Param("userId") int userId);
}
