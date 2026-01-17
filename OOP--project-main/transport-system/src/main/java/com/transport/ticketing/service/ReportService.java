package com.transport.ticketing.service;

import com.transport.ticketing.exception.AccessDeniedException;
import com.transport.ticketing.model.Cashier;
import com.transport.ticketing.model.Company;
import com.transport.ticketing.model.Distributor;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.Ticket;
import com.transport.ticketing.model.Trip;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.CashierRepository;
import com.transport.ticketing.repository.CompanyRepository;
import com.transport.ticketing.repository.DistributorRepository;
import com.transport.ticketing.repository.TicketRepository;
import com.transport.ticketing.repository.TripRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportService {
    private final CompanyRepository companies;
    private final DistributorRepository distributors;
    private final CashierRepository cashiers;
    private final TripRepository trips;
    private final TicketRepository tickets;

    public ReportService(CompanyRepository companies,
                         DistributorRepository distributors,
                         CashierRepository cashiers,
                         TripRepository trips,
                         TicketRepository tickets) {
        this.companies = companies;
        this.distributors = distributors;
        this.cashiers = cashiers;
        this.trips = trips;
        this.tickets = tickets;
    }

    public List<Company> reportCompaniesWithAvailableTrips(User distributorActor, LocalDateTime from, LocalDateTime to) {
        SecurityGuard.requireRole(distributorActor, Role.DISTRIBUTOR);
        return trips.findActive().stream()
                .filter(trip -> within(trip.getDeparture(), from, to))
                .map(trip -> companies.findById(trip.getOrganizerCompanyId()))
                .flatMap(Optional::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Distributor> reportDistributors(User actor) {
        if (actor.getRole() == Role.ADMIN) {
            return distributors.findAll();
        }
        if (actor.getRole() == Role.COMPANY) {
            Company company = companies.findAll().stream()
                    .filter(c -> c.getOwnerUserId().equals(actor.getId()))
                    .findFirst()
                    .orElse(null);
            if (company == null) {
                return List.of();
            }
            final String companyId = company.getId();
            return distributors.findAll().stream()
                    .filter(d -> d.getCompanyId().equals(companyId))
                    .collect(Collectors.toList());
        }
        throw new AccessDeniedException("Not permitted");
    }

    public List<Cashier> reportCashiers(User actor, String distributorId) {
        if (actor.getRole() == Role.ADMIN) {
            return cashiers.findAll();
        }
        if (actor.getRole() == Role.DISTRIBUTOR) {
            return cashiers.findByDistributorId(distributorId);
        }
        throw new AccessDeniedException("Not permitted");
    }

    public List<Ticket> reportTickets(User actor, String tripId, Instant from, Instant to) {
        List<Ticket> scoped = tickets.findByTripId(tripId).stream()
                .filter(t -> within(t.getSoldAt(), from, to))
                .collect(Collectors.toList());

        Trip trip = trips.findById(tripId).orElse(null);
        if (trip == null) {
            return scoped;
        }
        return switch (actor.getRole()) {
            case COMPANY -> {
                Company company = companies.findById(trip.getOrganizerCompanyId()).orElse(null);
                if (company != null && company.getOwnerUserId().equals(actor.getId())) {
                    yield scoped;
                }
                throw new AccessDeniedException("Not permitted");
            }
            case DISTRIBUTOR, CASHIER, ADMIN -> scoped;
            default -> throw new AccessDeniedException("Not permitted");
        };
    }

    public List<Trip> reportTrips(User actor, LocalDateTime from, LocalDateTime to) {
        return trips.findAll().stream()
                .filter(trip -> within(trip.getDeparture(), from, to))
                .filter(trip -> isTripVisible(actor, trip))
                .collect(Collectors.toList());
    }

    private boolean isTripVisible(User actor, Trip trip) {
        return switch (actor.getRole()) {
            case ADMIN -> true;
            case COMPANY -> {
                Company company = companies.findById(trip.getOrganizerCompanyId()).orElse(null);
                yield company != null && company.getOwnerUserId().equals(actor.getId());
            }
            case DISTRIBUTOR, CASHIER -> true;
            default -> false;
        };
    }

    private boolean within(LocalDateTime time, LocalDateTime from, LocalDateTime to) {
        if (time == null) return false;
        if (from != null && time.isBefore(from)) return false;
        if (to != null && time.isAfter(to)) return false;
        return true;
    }

    private boolean within(Instant time, Instant from, Instant to) {
        if (time == null) return false;
        if (from != null && time.isBefore(from)) return false;
        if (to != null && time.isAfter(to)) return false;
        return true;
    }
}
