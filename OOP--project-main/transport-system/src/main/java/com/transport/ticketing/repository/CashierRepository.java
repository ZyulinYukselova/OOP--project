package com.transport.ticketing.repository;

import com.transport.ticketing.model.Cashier;

import java.util.List;
import java.util.stream.Collectors;

public class CashierRepository extends InMemoryCrudRepository<Cashier> {
    public List<Cashier> findByDistributorId(String distributorId) {
        return findAll().stream()
                .filter(c -> c.getDistributorId().equals(distributorId))
                .collect(Collectors.toList());
    }
}
