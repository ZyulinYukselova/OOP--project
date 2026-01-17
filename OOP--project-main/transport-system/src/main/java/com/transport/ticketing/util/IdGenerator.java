package com.transport.ticketing.util;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private static final AtomicLong tripCounter = new AtomicLong(1);
    private static final AtomicLong ticketCounter = new AtomicLong(1);
    private static final AtomicLong requestCounter = new AtomicLong(1);
    private static final AtomicLong notificationCounter = new AtomicLong(1);
    private static final AtomicLong companyCounter = new AtomicLong(1);
    private static final AtomicLong distributorCounter = new AtomicLong(1);
    private static final AtomicLong cashierCounter = new AtomicLong(1);
    private static final AtomicLong userCounter = new AtomicLong(1);

    public static String nextTripId() {
        return "trip-" + tripCounter.getAndIncrement();
    }

    public static String nextTicketId() {
        return "ticket-" + ticketCounter.getAndIncrement();
    }

    public static String nextRequestId() {
        return "req-" + requestCounter.getAndIncrement();
    }

    public static String nextNotificationId() {
        return "notif-" + notificationCounter.getAndIncrement();
    }

    public static String nextCompanyId() {
        return "comp-" + companyCounter.getAndIncrement();
    }

    public static String nextDistributorId() {
        return "dist-" + distributorCounter.getAndIncrement();
    }

    public static String nextCashierId() {
        return "cash-" + cashierCounter.getAndIncrement();
    }

    public static String nextUserId() {
        return "user-" + userCounter.getAndIncrement();
    }

    /**
     * Reset all counters (useful for testing/seed).
     */
    public static void reset() {
        tripCounter.set(1);
        ticketCounter.set(1);
        requestCounter.set(1);
        notificationCounter.set(1);
        companyCounter.set(1);
        distributorCounter.set(1);
        cashierCounter.set(1);
        userCounter.set(1);
    }
}
