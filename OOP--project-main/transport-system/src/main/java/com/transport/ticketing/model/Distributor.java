package com.transport.ticketing.model;

import com.transport.ticketing.util.IdGenerator;

public class Distributor extends BaseEntity {
    private final String companyId;
    private final String ownerUserId;
    private String name;
    private double commission;
    private double rating;
    private String contact;

    public Distributor(String companyId, String ownerUserId, String name, double commission, String contact) {
        super(IdGenerator.nextDistributorId());
        this.companyId = companyId;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.commission = commission;
        this.contact = contact;
        this.rating = 0.0;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
