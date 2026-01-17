package com.transport.ticketing.model;

import com.transport.ticketing.util.IdGenerator;

public class TripRequest extends BaseEntity {
    private final String tripId;
    private final String distributorId;
    private RequestStatus status;

    public TripRequest(String tripId, String distributorId) {
        super(IdGenerator.nextRequestId());
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
