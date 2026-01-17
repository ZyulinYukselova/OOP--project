package com.transport.ticketing.repository;

import com.transport.ticketing.model.Trip;
import com.transport.ticketing.model.TripStatus;

import java.util.List;
import java.util.stream.Collectors;

public class TripRepository extends InMemoryCrudRepository<Trip> {
    public List<Trip> findByOrganizer(String companyId) {
        return findAll().stream()
                .filter(t -> t.getOrganizerCompanyId().equals(companyId))
                .collect(Collectors.toList());
    }

    public List<Trip> findActive() {
        return findAll().stream()
                .filter(t -> t.getStatus() == TripStatus.ACTIVE || t.getStatus() == TripStatus.APPROVED)
                .collect(Collectors.toList());
    }
}
