package com.dr.DavorRetreats.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dr.DavorRetreats.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByUsername(String username);
}
