package com.transport.ticketing.service;

import com.transport.ticketing.exception.NotFoundException;
import com.transport.ticketing.model.Company;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.CompanyRepository;

public class CompanyService {
    private final CompanyRepository companies;

    public CompanyService(CompanyRepository companies) {
        this.companies = companies;
    }

    public Company createCompany(User adminActor, String name, double commission, String contact, String ownerUserId) {
        SecurityGuard.requireRole(adminActor, Role.ADMIN);
        Company company = new Company(ownerUserId, name, commission, contact);
        return companies.save(company);
    }

    public Company getCompany(String companyId) {
        return companies.findById(companyId).orElseThrow(() -> new NotFoundException("Company not found"));
    }
}
