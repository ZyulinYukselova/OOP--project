package com.transport.ticketing.model;

import java.time.Instant;

public class Ticket extends BaseEntity {
    private final String tripId;
    private final int seatNumber;
    private final String cashierId;
    private String buyerName;
    private String buyerContact;
    private Instant soldAt;
    private TicketStatus status;

    public Ticket(String tripId, int seatNumber, String cashierId, String buyerName, String buyerContact) {
        this.tripId = tripId;
        this.seatNumber = seatNumber;
        this.cashierId = cashierId;
        this.buyerName = buyerName;
        this.buyerContact = buyerContact;
        this.status = TicketStatus.PENDING;
        this.soldAt = Instant.now();
    }

    public String getTripId() {
        return tripId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getCashierId() {
        return cashierId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerContact() {
        return buyerContact;
    }

    public void setBuyerContact(String buyerContact) {
        this.buyerContact = buyerContact;
    }

    public Instant getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(Instant soldAt) {
        this.soldAt = soldAt;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
