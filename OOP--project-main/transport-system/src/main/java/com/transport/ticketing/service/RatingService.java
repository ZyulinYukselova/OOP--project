package com.transport.ticketing.service;

import com.transport.ticketing.exception.AccessDeniedException;
import com.transport.ticketing.exception.NotFoundException;
import com.transport.ticketing.exception.ValidationException;
import com.transport.ticketing.model.*;
import com.transport.ticketing.repository.*;

public class RatingService {
    private final CompanyRepository companies;
    private final DistributorRepository distributors;
    private final CashierRepository cashiers;
    private final TripRepository trips;
    private final TripRequestRepository tripRequests;
    private static final double MIN_RATING = 1.0;
    private static final double MAX_RATING = 5.0;

    public RatingService(CompanyRepository companies,
                        DistributorRepository distributors,
                        CashierRepository cashiers,
                        TripRepository trips,
                        TripRequestRepository tripRequests) {
        this.companies = companies;
        this.distributors = distributors;
        this.cashiers = cashiers;
        this.trips = trips;
        this.tripRequests = tripRequests;
    }


    public Company rateCompany(User raterActor, String companyId, double rating) {
        SecurityGuard.requireRole(raterActor, Role.DISTRIBUTOR, Role.ADMIN);
        
        Company company = companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        
        validateRating(rating);

        if (raterActor.getRole() == Role.ADMIN) {
            company.setRating(rating);
            return companies.save(company);
        }

        if (raterActor.getRole() == Role.DISTRIBUTOR) {
            Distributor distributor = distributors.findAll().stream()
                    .filter(d -> d.getOwnerUserId().equals(raterActor.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Distributor not found for user"));
            
            boolean hasApprovedTrips = trips.findAll().stream()
                    .anyMatch(trip -> trip.getOrganizerCompanyId().equals(companyId) &&
                            trip.isDistributorApproved(distributor.getId()));
            
            if (!hasApprovedTrips) {
                throw new AccessDeniedException("Can only rate companies you have worked with (approved trips)");
            }
            
            company.setRating(rating);
            return companies.save(company);
        }
        
        throw new AccessDeniedException("Not permitted to rate company");
    }

    public Distributor rateDistributor(User raterActor, String distributorId, double rating) {
        SecurityGuard.requireRole(raterActor, Role.COMPANY, Role.ADMIN);
        
        Distributor distributor = distributors.findById(distributorId)
                .orElseThrow(() -> new NotFoundException("Distributor not found"));
        
        validateRating(rating);
        

        if (raterActor.getRole() == Role.ADMIN) {
            distributor.setRating(rating);
            return distributors.save(distributor);
        }

        if (raterActor.getRole() == Role.COMPANY) {
            Company company = companies.findAll().stream()
                    .filter(c -> c.getOwnerUserId().equals(raterActor.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Company not found for user"));
            
            boolean hasApprovedRequests = tripRequests.findAll().stream()
                    .anyMatch(request -> request.getDistributorId().equals(distributorId) &&
                            request.getStatus() == RequestStatus.APPROVED &&
                            trips.findById(request.getTripId())
                                    .map(trip -> trip.getOrganizerCompanyId().equals(company.getId()))
                                    .orElse(false));
            
            if (!hasApprovedRequests) {
                throw new AccessDeniedException("Can only rate distributors whose requests you have approved");
            }
            
            distributor.setRating(rating);
            return distributors.save(distributor);
        }
        
        throw new AccessDeniedException("Not permitted to rate distributor");
    }

    public Cashier rateCashier(User raterActor, String cashierId, double rating) {
        SecurityGuard.requireRole(raterActor, Role.DISTRIBUTOR, Role.ADMIN);
        
        Cashier cashier = cashiers.findById(cashierId)
                .orElseThrow(() -> new NotFoundException("Cashier not found"));
        
        validateRating(rating);

        if (raterActor.getRole() == Role.ADMIN) {
            cashier.setRating(rating);
            return cashiers.save(cashier);
        }

        if (raterActor.getRole() == Role.DISTRIBUTOR) {
            Distributor distributor = distributors.findById(cashier.getDistributorId())
                    .orElseThrow(() -> new NotFoundException("Distributor not found"));
            
            if (!distributor.getOwnerUserId().equals(raterActor.getId())) {
                throw new AccessDeniedException("Can only rate your own cashiers");
            }
            
            cashier.setRating(rating);
            return cashiers.save(cashier);
        }
        
        throw new AccessDeniedException("Not permitted to rate cashier");
    }

    private void validateRating(double rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new ValidationException(
                    String.format("Rating must be between %.1f and %.1f", MIN_RATING, MAX_RATING));
        }
    }

    public static double getMinRating() {
        return MIN_RATING;
    }

    public static double getMaxRating() {
        return MAX_RATING;
    }
}
