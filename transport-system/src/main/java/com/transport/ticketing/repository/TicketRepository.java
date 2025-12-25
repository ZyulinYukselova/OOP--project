package com.transport.ticketing.repository;

import com.transport.ticketing.model.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TicketRepository extends InMemoryCrudRepository<Ticket> {
    public Optional<Ticket> findByTripIdAndSeat(String tripId, int seatNumber) {
        return findAll().stream()
                .filter(t -> t.getTripId().equals(tripId) && t.getSeatNumber() == seatNumber)
                .findFirst();
    }

    public List<Ticket> findByTripId(String tripId) {
        return findAll().stream()
                .filter(t -> t.getTripId().equals(tripId))
                .collect(Collectors.toList());
    }

    public long countByTripAndBuyer(String tripId, String buyerName) {
        return findAll().stream()
                .filter(t -> t.getTripId().equals(tripId) && t.getBuyerName().equalsIgnoreCase(buyerName))
                .count();
    }
}
