package com.transport.ticketing.service;

import com.transport.ticketing.model.Company;
import com.transport.ticketing.model.Trip;
import com.transport.ticketing.model.TripRequest;
import com.transport.ticketing.model.NotificationType;
import com.transport.ticketing.repository.CashierRepository;
import com.transport.ticketing.repository.CompanyRepository;
import com.transport.ticketing.repository.DistributorRepository;
import com.transport.ticketing.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationCoordinator {
    private final NotificationService notifications;
    private final DistributorRepository distributors;
    private final CashierRepository cashiers;
    private final TicketRepository tickets;
    private final CompanyRepository companies;

    public NotificationCoordinator(NotificationService notifications,
                                   DistributorRepository distributors,
                                   CashierRepository cashiers,
                                   TicketRepository tickets,
                                   CompanyRepository companies) {
        this.notifications = notifications;
        this.distributors = distributors;
        this.cashiers = cashiers;
        this.tickets = tickets;
        this.companies = companies;
    }

    public void onTripRequestSubmitted(TripRequest request, String companyOwnerUserId) {
        notifications.notify(companyOwnerUserId, NotificationType.TRIP_REQUESTED,
                "Trip request submitted by distributor " + request.getDistributorId());
    }

    public void onTripCancelled(Trip trip) {
        List<String> distributorIds = trip.getApprovedDistributorIds();
        for (String distributorId : distributorIds) {
            distributors.findById(distributorId).ifPresent(distributor -> {
                notifications.notify(distributor.getOwnerUserId(), NotificationType.TRIP_CANCELLED,
                        "Trip " + trip.getId() + " cancelled");
                cashiers.findByDistributorId(distributor.getId()).forEach(cashier ->
                        notifications.notify(cashier.getUserId(), NotificationType.TRIP_CANCELLED,
                                "Trip " + trip.getId() + " cancelled"));
            });
        }
    }

    public void sendTicketsSoldSummary(Trip trip, String companyOwnerUserId) {
        long sold = tickets.findByTripId(trip.getId()).size();
        notifications.notify(companyOwnerUserId, NotificationType.TICKETS_SOLD_SUMMARY,
                "Trip " + trip.getId() + " sold " + sold + " tickets");
    }

    public void notifyUpcomingWithUnsold(List<Trip> upcomingTrips, LocalDateTime now) {
        for (Trip trip : upcomingTrips) {
            long sold = tickets.findByTripId(trip.getId()).size();
            if (sold < trip.getSeatsTotal()) {
                // Notify company owner
                companies.findById(trip.getOrganizerCompanyId()).ifPresent(company ->
                        notifications.notify(company.getOwnerUserId(), NotificationType.UPCOMING_TRIP_UNSOLD,
                                "Trip " + trip.getId() + " departing soon has unsold tickets"));
                
                // Notify distributors
                for (String distributorId : trip.getApprovedDistributorIds()) {
                    distributors.findById(distributorId).ifPresent(distributor ->
                            notifications.notify(distributor.getOwnerUserId(), NotificationType.UPCOMING_TRIP_UNSOLD,
                                    "Trip " + trip.getId() + " departing soon has unsold tickets"));
                }
            }
        }
    }
}
