package com.transport.ticketing.model;

import com.transport.ticketing.util.IdGenerator;

public class Cashier extends BaseEntity {
    private final String distributorId;
    private final String userId;
    private String name;
    private double commission;
    private double rating;
    private String contact;

    public Cashier(String distributorId, String userId, String name, double commission, String contact) {
        super(IdGenerator.nextCashierId());
        this.distributorId = distributorId;
        this.userId = userId;
        this.name = name;
        this.commission = commission;
        this.contact = contact;
        this.rating = 0.0;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public String getUserId() {
        return userId;
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
