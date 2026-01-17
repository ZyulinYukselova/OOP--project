package com.transport.ticketing;


public class TicketingApplication {

    public static void main(String[] args) {
        System.out.println("Ticketing system bootstrap (no framework).");
        new com.transport.ticketing.cli.Cli().run();
    }
}
