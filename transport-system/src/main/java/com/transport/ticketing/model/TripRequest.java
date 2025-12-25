package com.transport.ticketing.model;

public class TripRequest extends BaseEntity {
    private final String tripId;
    private final String distributorId;
    private RequestStatus status;

    public TripRequest(String tripId, String distributorId) {
        this.tripId = tripId;
        this.distributorId = distributorId;
        this.status = RequestStatus.REQUESTED;
    }

    public String getTripId() {
        return tripId;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
