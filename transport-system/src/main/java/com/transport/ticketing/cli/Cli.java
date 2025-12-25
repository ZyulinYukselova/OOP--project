package com.transport.ticketing.cli;

import com.transport.ticketing.exception.DomainException;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.Trip;
import com.transport.ticketing.model.TripRequest;
import com.transport.ticketing.model.Ticket;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.*;
import com.transport.ticketing.service.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Cli {
    private final Scanner scanner = new Scanner(System.in);

    private final UserRepository userRepo = new UserRepository();
    private final CompanyRepository companyRepo = new CompanyRepository();
    private final DistributorRepository distributorRepo = new DistributorRepository();
    private final CashierRepository cashierRepo = new CashierRepository();
    private final TripRepository tripRepo = new TripRepository();
    private final TripRequestRepository requestRepo = new TripRequestRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final NotificationRepository notificationRepo = new NotificationRepository();

    private final UserService userService = new UserService(userRepo);
    private final CompanyService companyService = new CompanyService(companyRepo);
    private final DistributorService distributorService = new DistributorService(distributorRepo, cashierRepo, companyRepo);
    private final TripService tripService = new TripService(tripRepo, requestRepo, companyRepo, distributorRepo);
    private final TicketService ticketService = new TicketService(ticketRepo, tripRepo, cashierRepo, distributorRepo);
    private final NotificationService notificationService = new NotificationService(notificationRepo);
    private final NotificationCoordinator notificationCoordinator = new NotificationCoordinator(notificationService, distributorRepo, cashierRepo, ticketRepo);
    private final ReportService reportService = new ReportService(companyRepo, distributorRepo, cashierRepo, tripRepo, ticketRepo);

    private User admin;
    private User companyUser;
    private User distributorUser;
    private User cashierUser;
    private String companyId;
    private String distributorId;
    private String cashierId;

    public void run() {
        seed();
        println("Type 'help' for commands. Type 'exit' to quit.");
        while (true) {
            print("> ");
            String line = scanner.nextLine();
            if (line == null) break;
            String input = line.trim();
            if (input.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(input)) {
                println("Bye.");
                break;
            }
            try {
                dispatch(input);
            } catch (DomainException ex) {
                println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                println("Unexpected: " + ex.getMessage());
            }
        }
    }

    private void dispatch(String input) {
        String[] parts = input.split("\\s+");
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        switch (cmd) {
            case "help" -> help();
            case "seed" -> seed();
            case "add-trip" -> addTrip(parts);
            case "request-trip" -> requestTrip(parts);
            case "approve-request" -> approveRequest(parts);
            case "sell-ticket" -> sellTicket(parts);
            case "report-trips" -> reportTrips();
            case "report-tickets" -> reportTickets(parts);
            case "notifications" -> notifications();
            case "who" -> who();
            default -> println("Unknown command. Type 'help'.");
        }
    }

    private void help() {
        println("Commands:");
        println("  help");
        println("  seed");
        println("  add-trip <type> <dest> <seats> <limit>");
        println("  request-trip <tripId>");
        println("  approve-request <requestId> <yes|no>");
        println("  sell-ticket <tripId> <seat> <buyerName> [contact]");
        println("  report-trips");
        println("  report-tickets <tripId>");
        println("  notifications");
        println("  who");
        println("  exit");
    }

    private void seed() {
        userRepo.findAll().forEach(u -> userRepo.deleteById(u.getId()));
        companyRepo.findAll().forEach(c -> companyRepo.deleteById(c.getId()));
        distributorRepo.findAll().forEach(d -> distributorRepo.deleteById(d.getId()));
        cashierRepo.findAll().forEach(c -> cashierRepo.deleteById(c.getId()));
        tripRepo.findAll().forEach(t -> tripRepo.deleteById(t.getId()));
        requestRepo.findAll().forEach(r -> requestRepo.deleteById(r.getId()));
        ticketRepo.findAll().forEach(t -> ticketRepo.deleteById(t.getId()));
        notificationRepo.findAll().forEach(n -> notificationRepo.deleteById(n.getId()));

        admin = userService.createUser("admin@example.com", "Admin", Role.ADMIN);
        companyUser = userService.createUser("company@example.com", "CompanyOwner", Role.COMPANY);
        distributorUser = userService.createUser("distributor@example.com", "DistributorOwner", Role.DISTRIBUTOR);
        cashierUser = userService.createUser("cashier@example.com", "CashierUser", Role.CASHIER);

        companyId = companyService.createCompany(admin, "CityTransport", 5.0, "contact@company", companyUser.getId()).getId();
        distributorId = distributorService.createDistributor(admin, companyId, distributorUser.getId(), "BestTickets", 2.0, "contact@dist").getId();
        cashierId = distributorService.createCashier(distributorUser, distributorId, cashierUser.getId(), "FrontDesk", 1.0, "cashier@dist").getId();

        println("Seeded demo users/orgs.");
        who();
    }

    private void addTrip(String[] parts) {
        if (parts.length < 5) {
            println("Usage: add-trip <type> <dest> <seats> <limit>");
            return;
        }
        String type = parts[1];
        String dest = parts[2];
        int seats = Integer.parseInt(parts[3]);
        int limit = Integer.parseInt(parts[4]);
        LocalDateTime dep = LocalDateTime.now().plusDays(2);
        LocalDateTime arr = dep.plusHours(2);
        Trip trip = tripService.addTrip(companyUser, companyId, type, dest, dep, arr, seats, limit, List.of("BUS"));
        println("Trip id=" + trip.getId());
    }

    private void requestTrip(String[] parts) {
        if (parts.length < 2) {
            println("Usage: request-trip <tripId>");
            return;
        }
        String tripId = parts[1];
        TripRequest req = tripService.requestTrip(distributorUser, distributorId, tripId);
        notificationCoordinator.onTripRequestSubmitted(req, companyUser.getId());
        println("Request id=" + req.getId());
    }

    private void approveRequest(String[] parts) {
        if (parts.length < 3) {
            println("Usage: approve-request <requestId> <yes|no>");
            return;
        }
        String reqId = parts[1];
        boolean approve = "yes".equalsIgnoreCase(parts[2]);
        Trip trip = tripService.approveRequest(companyUser, reqId, approve);
        println("Request " + (approve ? "approved" : "rejected") + " for trip " + trip.getId());
    }

    private void sellTicket(String[] parts) {
        if (parts.length < 4) {
            println("Usage: sell-ticket <tripId> <seat> <buyerName> [contact]");
            return;
        }
        String tripId = parts[1];
        int seat = Integer.parseInt(parts[2]);
        String buyer = parts[3];
        String contact = parts.length >= 5 ? parts[4] : "n/a";
        Ticket ticket = ticketService.sellTicket(cashierUser, cashierId, tripId, seat, buyer, contact);
        println("Ticket id=" + ticket.getId());
    }

    private void reportTrips() {
        List<Trip> list = reportService.reportTrips(admin, null, null);
        println("Trips:");
        list.forEach(t -> println("  " + t.getId() + " dest=" + t.getDestination() + " status=" + t.getStatus()));
    }

    private void reportTickets(String[] parts) {
        if (parts.length < 2) {
            println("Usage: report-tickets <tripId>");
            return;
        }
        String tripId = parts[1];
        List<Ticket> list = reportService.reportTickets(admin, tripId, null, null);
        println("Tickets:");
        list.forEach(t -> println("  " + t.getId() + " seat=" + t.getSeatNumber() + " buyer=" + t.getBuyerName()));
    }

    private void notifications() {
        println("Notifications:");
        notificationRepo.findAll().forEach(n ->
                println("  user=" + n.getUserId() + " type=" + n.getType() + " payload=" + n.getPayload()));
    }

    private void who() {
        println("Users:");
        println("  admin        " + admin.getId());
        println("  companyUser  " + companyUser.getId());
        println("  distributor  " + distributorUser.getId());
        println("  cashier      " + cashierUser.getId());
        println("Orgs:");
        println("  companyId    " + companyId);
        println("  distributorId " + distributorId);
        println("  cashierId    " + cashierId);
    }

    private void println(String s) {
        System.out.println(s);
    }

    private void print(String s) {
        System.out.print(s);
    }
}
