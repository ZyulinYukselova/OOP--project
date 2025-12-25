package com.transport.ticketing.service;

import com.transport.ticketing.exception.NotFoundException;
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
}
