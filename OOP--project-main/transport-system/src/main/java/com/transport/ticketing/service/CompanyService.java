package com.transport.ticketing.service;

import com.transport.ticketing.exception.AccessDeniedException;
import com.transport.ticketing.exception.NotFoundException;
import com.transport.ticketing.exception.ValidationException;
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

    public Company updateCompany(User actor, String companyId, String name, Double commission, String contact) {
        SecurityGuard.requireRole(actor, Role.ADMIN, Role.COMPANY);
        Company company = companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("Company not found"));
        
        // Only company owner or admin can update
        if (actor.getRole() == Role.COMPANY && !company.getOwnerUserId().equals(actor.getId())) {
            throw new AccessDeniedException("Company not owned by actor");
        }
        
        if (name != null && !name.trim().isEmpty()) {
            company.setName(name.trim());
        }
        if (commission != null) {
            if (commission < 0) {
                throw new ValidationException("Commission cannot be negative");
            }
            company.setCommission(commission);
        }
        if (contact != null && !contact.trim().isEmpty()) {
            company.setContact(contact.trim());
        }
        
        return companies.save(company);
    }
}
