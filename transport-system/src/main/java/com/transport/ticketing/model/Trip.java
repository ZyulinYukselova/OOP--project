package com.transport.ticketing.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Trip definition.
 */
public class Trip extends BaseEntity {
    private final String organizerCompanyId;
    private String type;
    private String destination;
    private LocalDateTime departure;
    private LocalDateTime arrival;
    private int seatsTotal;
    private int perPersonLimit;
    private TripStatus status;
    private final List<String> transportTypes = new ArrayList<>();
    private final List<String> approvedDistributorIds = new ArrayList<>();

    public Trip(String organizerCompanyId,
                String type,
                String destination,
                LocalDateTime departure,
                LocalDateTime arrival,
                int seatsTotal,
                int perPersonLimit,
                List<String> transportTypes) {
        this.organizerCompanyId = organizerCompanyId;
        this.type = type;
        this.destination = destination;
        this.departure = departure;
        this.arrival = arrival;
        this.seatsTotal = seatsTotal;
        this.perPersonLimit = perPersonLimit;
        this.status = TripStatus.DRAFT;
        if (transportTypes != null) {
            this.transportTypes.addAll(transportTypes);
        }
    }

    public String getOrganizerCompanyId() {
        return organizerCompanyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDeparture() {
        return departure;
    }

    public void setDeparture(LocalDateTime departure) {
        this.departure = departure;
    }

    public LocalDateTime getArrival() {
        return arrival;
    }

    public void setArrival(LocalDateTime arrival) {
        this.arrival = arrival;
    }

    public int getSeatsTotal() {
        return seatsTotal;
    }

    public void setSeatsTotal(int seatsTotal) {
        this.seatsTotal = seatsTotal;
    }

    public int getPerPersonLimit() {
        return perPersonLimit;
    }

    public void setPerPersonLimit(int perPersonLimit) {
        this.perPersonLimit = perPersonLimit;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public List<String> getTransportTypes() {
        return Collections.unmodifiableList(transportTypes);
    }

    public void addTransportType(String transportType) {
        this.transportTypes.add(transportType);
    }

    public List<String> getApprovedDistributorIds() {
        return Collections.unmodifiableList(approvedDistributorIds);
    }

    public void approveDistributor(String distributorId) {
        if (!approvedDistributorIds.contains(distributorId)) {
            approvedDistributorIds.add(distributorId);
        }
    }

    public boolean isDistributorApproved(String distributorId) {
        return approvedDistributorIds.contains(distributorId);
    }
}
