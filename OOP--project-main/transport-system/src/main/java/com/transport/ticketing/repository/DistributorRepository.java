package com.transport.ticketing.repository;

import com.transport.ticketing.model.Distributor;

import java.util.List;
import java.util.stream.Collectors;

public class DistributorRepository extends InMemoryCrudRepository<Distributor> {
    public List<Distributor> findByCompanyId(String companyId) {
        return findAll().stream()
                .filter(d -> d.getCompanyId().equals(companyId))
                .collect(Collectors.toList());
    }
}
