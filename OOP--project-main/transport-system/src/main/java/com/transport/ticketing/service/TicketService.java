package com.transport.ticketing.service;

import com.transport.ticketing.exception.AccessDeniedException;
import com.transport.ticketing.exception.NotFoundException;
import com.transport.ticketing.exception.ValidationException;
import com.transport.ticketing.model.Cashier;
import com.transport.ticketing.model.Company;
import com.transport.ticketing.model.Distributor;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.Ticket;
import com.transport.ticketing.model.TicketStatus;
import com.transport.ticketing.model.Trip;
import com.transport.ticketing.model.TripStatus;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.CashierRepository;
import com.transport.ticketing.repository.CompanyRepository;
import com.transport.ticketing.repository.DistributorRepository;
import com.transport.ticketing.repository.TicketRepository;
import com.transport.ticketing.repository.TripRepository;

public class TicketService {
    private final TicketRepository tickets;
    private final TripRepository trips;
    private final CashierRepository cashiers;
    private final DistributorRepository distributors;
    private final CompanyRepository companies;
    private NotificationCoordinator notificationCoordinator;

    public TicketService(TicketRepository tickets,
                         TripRepository trips,
                         CashierRepository cashiers,
                         DistributorRepository distributors,
                         CompanyRepository companies) {
        this.tickets = tickets;
        this.trips = trips;
        this.cashiers = cashiers;
        this.distributors = distributors;
        this.companies = companies;
    }

    public void setNotificationCoordinator(NotificationCoordinator notificationCoordinator) {
        this.notificationCoordinator = notificationCoordinator;
    }

    public Ticket sellTicket(User cashierActor, String cashierId, String tripId,
                             int seatNumber, String buyerName, String buyerContact) {
        SecurityGuard.requireRole(cashierActor, Role.CASHIER);
        Cashier cashier = cashiers.findById(cashierId)
                .orElseThrow(() -> new NotFoundException("Cashier not found"));
        if (!cashier.getUserId().equals(cashierActor.getId())) {
            throw new AccessDeniedException("Cashier not owned by actor");
        }
        Trip trip = trips.findById(tripId).orElseThrow(() -> new NotFoundException("Trip not found"));
        if (trip.getStatus() != TripStatus.ACTIVE && trip.getStatus() != TripStatus.APPROVED) {
            throw new ValidationException("Trip not sellable in status " + trip.getStatus());
        }
        Distributor distributor = distributors.findById(cashier.getDistributorId())
                .orElseThrow(() -> new NotFoundException("Distributor not found"));
        if (!trip.isDistributorApproved(distributor.getId())) {
            throw new AccessDeniedException("Distributor not approved for this trip");
        }
        if (seatNumber <= 0 || seatNumber > trip.getSeatsTotal()) {
            throw new ValidationException("Seat number out of range");
        }
        tickets.findByTripIdAndSeat(trip.getId(), seatNumber).ifPresent(t -> {
            throw new ValidationException("Seat already sold");
        });
        long buyerCount = tickets.countByTripAndBuyer(trip.getId(), buyerName);
        if (buyerCount >= trip.getPerPersonLimit()) {
            throw new ValidationException("Buyer reached per-person limit");
        }
        Ticket ticket = new Ticket(trip.getId(), seatNumber, cashier.getId(), buyerName, buyerContact);
        ticket.setStatus(TicketStatus.CONFIRMED);
        Ticket savedTicket = tickets.save(ticket);

        if (notificationCoordinator != null) {
            Company company = companies.findById(trip.getOrganizerCompanyId()).orElse(null);
            if (company != null) {
                notificationCoordinator.sendTicketsSoldSummary(trip, company.getOwnerUserId());
            }
        }
        
        return savedTicket;
    }
}
