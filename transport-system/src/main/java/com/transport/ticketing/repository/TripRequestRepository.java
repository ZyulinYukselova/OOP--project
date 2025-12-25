package com.transport.ticketing.repository;

import com.transport.ticketing.model.TripRequest;

import java.util.List;
import java.util.stream.Collectors;

public class TripRequestRepository extends InMemoryCrudRepository<TripRequest> {
    public List<TripRequest> findByTripId(String tripId) {
        return findAll().stream()
                .filter(r -> r.getTripId().equals(tripId))
                .collect(Collectors.toList());
    }
}
