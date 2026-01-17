package com.transport.ticketing.service;

import com.transport.ticketing.exception.AccessDeniedException;
import com.transport.ticketing.exception.NotFoundException;
import com.transport.ticketing.exception.ValidationException;
import com.transport.ticketing.model.Company;
import com.transport.ticketing.model.Distributor;
import com.transport.ticketing.model.RequestStatus;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.Trip;
import com.transport.ticketing.model.TripRequest;
import com.transport.ticketing.model.TripStatus;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.CompanyRepository;
import com.transport.ticketing.repository.DistributorRepository;
import com.transport.ticketing.repository.TripRepository;
import com.transport.ticketing.repository.TripRequestRepository;
import com.transport.ticketing.service.NotificationCoordinator;

import java.time.LocalDateTime;
import java.util.List;

public class TripService {
    private final TripRepository trips;
    private final TripRequestRepository requests;
    private final CompanyRepository companies;
    private final DistributorRepository distributors;
    private NotificationCoordinator notificationCoordinator;

    public TripService(TripRepository trips,
                       TripRequestRepository requests,
                       CompanyRepository companies,
                       DistributorRepository distributors) {
        this.trips = trips;
        this.requests = requests;
        this.companies = companies;
        this.distributors = distributors;
    }

    public void setNotificationCoordinator(NotificationCoordinator notificationCoordinator) {
        this.notificationCoordinator = notificationCoordinator;
    }

    public Trip addTrip(User companyActor, String companyId, String type, String destination,
                        LocalDateTime departure, LocalDateTime arrival,
                        int seatsTotal, int perPersonLimit, List<String> transportTypes) {
        SecurityGuard.requireRole(companyActor, Role.COMPANY);
        Company company = companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        if (!company.getOwnerUserId().equals(companyActor.getId())) {
            throw new AccessDeniedException("Company not owned by actor");
        }
        if (seatsTotal <= 0 || perPersonLimit <= 0) {
            throw new ValidationException("Seats and per-person limit must be positive");
        }
        Trip trip = new Trip(company.getId(), type, destination, departure, arrival, seatsTotal, perPersonLimit, transportTypes);
        trip.setStatus(TripStatus.ACTIVE);
        return trips.save(trip);
    }

    public TripRequest requestTrip(User distributorActor, String distributorId, String tripId) {
        SecurityGuard.requireRole(distributorActor, Role.DISTRIBUTOR);
        Distributor distributor = distributors.findById(distributorId)
                .orElseThrow(() -> new NotFoundException("Distributor not found"));
        if (!distributor.getOwnerUserId().equals(distributorActor.getId())) {
            throw new AccessDeniedException("Distributor not owned by actor");
        }
        Trip trip = trips.findById(tripId).orElseThrow(() -> new NotFoundException("Trip not found"));
        if (trip.getStatus() == TripStatus.CANCELLED || trip.getStatus() == TripStatus.COMPLETED) {
            throw new ValidationException("Cannot request cancelled or completed trips");
        }
        TripRequest request = new TripRequest(trip.getId(), distributor.getId());
        return requests.save(request);
    }

    public Trip approveRequest(User companyActor, String requestId, boolean approve) {
        SecurityGuard.requireRole(companyActor, Role.COMPANY);
        TripRequest request = requests.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        Trip trip = trips.findById(request.getTripId())
                .orElseThrow(() -> new NotFoundException("Trip not found"));
        Company company = companies.findById(trip.getOrganizerCompanyId())
                .orElseThrow(() -> new NotFoundException("Company not found"));
        if (!company.getOwnerUserId().equals(companyActor.getId())) {
            throw new AccessDeniedException("Company not owned by actor");
        }
        if (approve) {
            request.setStatus(RequestStatus.APPROVED);
            trip.approveDistributor(request.getDistributorId());
            if (trip.getStatus() == TripStatus.REQUESTED) {
                trip.setStatus(TripStatus.APPROVED);
            }
        } else {
            request.setStatus(RequestStatus.REJECTED);
        }
        requests.save(request);
        return trips.save(trip);
    }

    public Trip cancelTrip(User actor, String tripId) {
        Trip trip = trips.findById(tripId).orElseThrow(() -> new NotFoundException("Trip not found"));
        Company company = companies.findById(trip.getOrganizerCompanyId())
                .orElseThrow(() -> new NotFoundException("Company not found"));
        boolean companyOwner = company.getOwnerUserId().equals(actor.getId()) && actor.getRole() == Role.COMPANY;
        boolean admin = actor.getRole() == Role.ADMIN;
        if (!companyOwner && !admin) {
            throw new AccessDeniedException("Not permitted to cancel");
        }
        trip.setStatus(TripStatus.CANCELLED);
        Trip savedTrip = trips.save(trip);
        
       
        if (notificationCoordinator != null) {
            notificationCoordinator.onTripCancelled(savedTrip);
        }
        
        return savedTrip;
    }

    public Trip getTrip(String id) {
        return trips.findById(id).orElseThrow(() -> new NotFoundException("Trip not found"));
    }
}
