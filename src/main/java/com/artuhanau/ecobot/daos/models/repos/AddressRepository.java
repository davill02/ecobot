package com.artuhanau.ecobot.daos.models.repos;

import java.util.Optional;

import com.artuhanau.ecobot.daos.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>
{
    Optional<Address> findAddressByAddress(String addressName);
}
