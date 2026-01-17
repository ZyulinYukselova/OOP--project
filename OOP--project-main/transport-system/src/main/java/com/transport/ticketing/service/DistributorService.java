package com.transport.ticketing.service;

import com.transport.ticketing.exception.AccessDeniedException;
import com.transport.ticketing.exception.NotFoundException;
import com.transport.ticketing.exception.ValidationException;
import com.transport.ticketing.model.Cashier;
import com.transport.ticketing.model.Company;
import com.transport.ticketing.model.Distributor;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.CashierRepository;
import com.transport.ticketing.repository.CompanyRepository;
import com.transport.ticketing.repository.DistributorRepository;

public class DistributorService {
    private final DistributorRepository distributors;
    private final CashierRepository cashiers;
    private final CompanyRepository companies;

    public DistributorService(DistributorRepository distributors,
                              CashierRepository cashiers,
                              CompanyRepository companies) {
        this.distributors = distributors;
        this.cashiers = cashiers;
        this.companies = companies;
    }

    public Distributor createDistributor(User adminActor, String companyId, String ownerUserId,
                                         String name, double commission, String contact) {
        SecurityGuard.requireRole(adminActor, Role.ADMIN);
        Company company = companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        Distributor distributor = new Distributor(company.getId(), ownerUserId, name, commission, contact);
        return distributors.save(distributor);
    }

    public Cashier createCashier(User distributorActor, String distributorId,
                                 String cashierUserId, String name, double commission, String contact) {
        SecurityGuard.requireRole(distributorActor, Role.DISTRIBUTOR);
        Distributor distributor = distributors.findById(distributorId)
                .orElseThrow(() -> new NotFoundException("Distributor not found"));
        if (!distributor.getOwnerUserId().equals(distributorActor.getId())) {
            throw new NotFoundException("Distributor not owned by actor");
        }
        Cashier cashier = new Cashier(distributor.getId(), cashierUserId, name, commission, contact);
        return cashiers.save(cashier);
    }

    public Distributor getDistributor(String id) {
        return distributors.findById(id).orElseThrow(() -> new NotFoundException("Distributor not found"));
    }

    public Cashier getCashier(String id) {
        return cashiers.findById(id).orElseThrow(() -> new NotFoundException("Cashier not found"));
    }

    public Distributor updateDistributor(User actor, String distributorId, String name, Double commission, String contact) {
        SecurityGuard.requireRole(actor, Role.ADMIN, Role.COMPANY, Role.DISTRIBUTOR);
        Distributor distributor = distributors.findById(distributorId)
                .orElseThrow(() -> new NotFoundException("Distributor not found"));

        boolean isOwner = distributor.getOwnerUserId().equals(actor.getId());
        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isCompanyOwner = false;
        
        if (actor.getRole() == Role.COMPANY) {
            Company company = companies.findById(distributor.getCompanyId()).orElse(null);
            if (company != null && company.getOwnerUserId().equals(actor.getId())) {
                isCompanyOwner = true;
            }
        }
        
        if (!isOwner && !isAdmin && !isCompanyOwner) {
            throw new AccessDeniedException("Not permitted to update distributor");
        }
        
        updateProfileFields(distributor, name, commission, contact);
        return distributors.save(distributor);
    }

    public Cashier updateCashier(User actor, String cashierId, String name, Double commission, String contact) {
        SecurityGuard.requireRole(actor, Role.ADMIN, Role.DISTRIBUTOR);
        Cashier cashier = cashiers.findById(cashierId)
                .orElseThrow(() -> new NotFoundException("Cashier not found"));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isDistributorOwner = false;
        
        if (actor.getRole() == Role.DISTRIBUTOR) {
            Distributor distributor = distributors.findById(cashier.getDistributorId()).orElse(null);
            if (distributor != null && distributor.getOwnerUserId().equals(actor.getId())) {
                isDistributorOwner = true;
            }
        }
        
        if (!isAdmin && !isDistributorOwner) {
            throw new AccessDeniedException("Not permitted to update cashier");
        }
        
        updateProfileFields(cashier, name, commission, contact);
        return cashiers.save(cashier);
    }

    private void updateProfileFields(Distributor distributor, String name, Double commission, String contact) {
        if (name != null && !name.trim().isEmpty()) {
            distributor.setName(name.trim());
        }
        if (commission != null) {
            validateCommission(commission);
            distributor.setCommission(commission);
        }
        if (contact != null && !contact.trim().isEmpty()) {
            distributor.setContact(contact.trim());
        }
    }

    private void updateProfileFields(Cashier cashier, String name, Double commission, String contact) {
        if (name != null && !name.trim().isEmpty()) {
            cashier.setName(name.trim());
        }
        if (commission != null) {
            validateCommission(commission);
            cashier.setCommission(commission);
        }
        if (contact != null && !contact.trim().isEmpty()) {
            cashier.setContact(contact.trim());
        }
    }

    private void validateCommission(double commission) {
        if (commission < 0) {
            throw new ValidationException("Commission cannot be negative");
        }
    }
}
