package com.transport.ticketing;

/**
 * Entry point for the transport ticketing system (plain Java, no frameworks).
 * For now it just prints a greeting; we'll grow this into a CLI/service layer.
 */
public class TicketingApplication {

    public static void main(String[] args) {
        System.out.println("Ticketing system bootstrap (no framework).");
        new com.transport.ticketing.cli.Cli().run();
    }
}
