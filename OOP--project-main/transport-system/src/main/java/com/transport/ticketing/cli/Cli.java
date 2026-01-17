package com.transport.ticketing.cli;

import com.transport.ticketing.exception.DomainException;
import com.transport.ticketing.model.Role;
import com.transport.ticketing.model.Trip;
import com.transport.ticketing.model.TripRequest;
import com.transport.ticketing.model.TripStatus;
import com.transport.ticketing.model.Ticket;
import com.transport.ticketing.model.User;
import com.transport.ticketing.repository.*;
import com.transport.ticketing.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private final TicketService ticketService = new TicketService(ticketRepo, tripRepo, cashierRepo, distributorRepo, companyRepo);
    private final NotificationService notificationService = new NotificationService(notificationRepo);
    private final NotificationCoordinator notificationCoordinator = new NotificationCoordinator(notificationService, distributorRepo, cashierRepo, ticketRepo, companyRepo);
    private final ReportService reportService = new ReportService(companyRepo, distributorRepo, cashierRepo, tripRepo, ticketRepo);
    private final RatingService ratingService = new RatingService(companyRepo, distributorRepo, cashierRepo, tripRepo, requestRepo);

    {
        // Integrate notification coordinator with services
        tripService.setNotificationCoordinator(notificationCoordinator);
        ticketService.setNotificationCoordinator(notificationCoordinator);
    }

    private User admin;
    private User companyUser;
    private User distributorUser;
    private User cashierUser;
    private String companyId;
    private String distributorId;
    private String cashierId;

    public void run() {
        printWelcome();
        println("");
        while (true) {
            print("> ");
            String line = scanner.nextLine();
            if (line == null) break;
            String input = line.trim();
            if (input.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                println("\nБлагодарим ви за използването на системата! Довиждане!\n");
                break;
            }
            try {
                dispatch(input);
            } catch (DomainException ex) {
                println("\nГРЕШКА: " + ex.getMessage());
                println("   Въведете 'help' за списък с команди.\n");
            } catch (NumberFormatException ex) {
                println("\nГРЕШКА: Невалидна числена стойност!");
                println("   Проверете въведените данни и опитайте отново.\n");
            } catch (Exception ex) {
                println("\nНЕОЧАКВАНА ГРЕШКА: " + ex.getMessage());
                println("   Моля, опитайте отново или въведете 'help'.\n");
            }
        }
    }

    private void printWelcome() {
        println("\n" + "=".repeat(60));
        println("  Passenger Transport Information System");
        println("=".repeat(60));
        println("\nДобре дошли!");
        println("Това е система за управление на пътувания и билети.");
        println("\nВъведете 'seed' за зареждане на примерни данни.");
        println("Въведете 'help' за списък с налични команди.");
        println("Въведете 'exit' или 'quit' за изход.\n");
    }

    private void dispatch(String input) {
        String[] parts = input.split("\\s+");
        String cmd = parts[0].toLowerCase(Locale.ROOT);
        switch (cmd) {
            case "help" -> help();
            case "?" -> help();
            case "seed" -> seed();
            case "add-trip" -> addTrip(parts);
            case "request-trip" -> requestTrip(parts);
            case "approve-request" -> approveRequest(parts);
            case "sell-ticket" -> sellTicket(parts);
            case "report-trips" -> reportTrips(parts);
            case "report-tickets" -> reportTickets(parts);
            case "report-companies" -> reportCompanies(parts);
            case "report-distributors" -> reportDistributors(parts);
            case "report-cashiers" -> reportCashiers(parts);
            case "cancel-trip" -> cancelTrip(parts);
            case "update-company" -> updateCompany(parts);
            case "update-distributor" -> updateDistributor(parts);
            case "update-cashier" -> updateCashier(parts);
            case "rate-company" -> rateCompany(parts);
            case "rate-distributor" -> rateDistributor(parts);
            case "rate-cashier" -> rateCashier(parts);
            case "check-upcoming-trips" -> checkUpcomingTrips(parts);
            case "notifications" -> notifications();
            case "who" -> who();
            default -> {
                println("\nНепозната команда: '" + cmd + "'");
                println("   Въведете 'help' или '?' за списък с налични команди.\n");
            }
        }
    }

    private void help() {
        println("\n" + "=".repeat(60));
        println("  СПИСЪК С КОМАНДИ / COMMANDS LIST");
        println("=".repeat(60));
        
        println("\nОБЩИ КОМАНДИ:");
        println("  help              - Показва този списък");
        println("  who               - Показва текущите потребители и организации");
        println("  seed              - Рестартира системата с примерни данни");
        println("  exit / quit       - Излизане от системата");
        
        println("\nРАБОТА С ПЪТУВАНИЯ:");
        println("  add-trip <тип> <дестинация> <места> <лимит>");
        println("                   - Добавя ново пътуване");
        println("                   Пример: add-trip Express Sofia 50 5");
        println("  request-trip <id>");
        println("                   - Заявява билети за продажба");
        println("                   Пример: request-trip <tripId>");
        println("  approve-request <id> <yes/no>");
        println("                   - Одобрява/отказва заявка за билети");
        println("                   Пример: approve-request <requestId> yes");
        println("  cancel-trip <id>");
        println("                   - Отменя пътуване");
        println("                   Пример: cancel-trip <tripId>");
        
        println("\nРАБОТА С БИЛЕТИ:");
        println("  sell-ticket <tripId> <място> <име_купувач> [контакт]");
        println("                   - Продава билет");
        println("                   Пример: sell-ticket <tripId> 15 Иван Петров 0888123456");
        
        println("\nОТЧЕТИ:");
        println("  report-trips [от_дата] [до_дата]");
        println("                   - Отчет за пътувания");
        println("                   Пример: report-trips");
        println("  report-tickets <tripId> [от_дата] [до_дата]");
        println("                   - Отчет за билети за пътуване");
        println("                   Пример: report-tickets <tripId>");
        println("  report-companies [от_дата] [до_дата]");
        println("                   - Отчет за компании с налични пътувания");
        println("  report-distributors");
        println("                   - Отчет за дистрибутори");
        println("  report-cashiers [distributorId]");
        println("                   - Отчет за касиери");
        
        println("\nУПРАВЛЕНИЕ НА ПРОФИЛИ:");
        println("  update-company <id> [име] [комисионна] [контакт]");
        println("                   - Обновява данни на компания");
        println("                   Пример: update-company <id> Ново_име - - нов@email.com");
        println("                   (Използвайте '-' за да пропуснете поле)");
        println("  update-distributor <id> [име] [комисионна] [контакт]");
        println("                   - Обновява данни на дистрибутор");
        println("  update-cashier <id> [име] [комисионна] [контакт]");
        println("                   - Обновява данни на касиер");
        
        println("\nОЦЕНЯВАНЕ:");
        println("  rate-company <id> <рейтинг>");
        println("                   - Оценява компания (1.0 - 5.0)");
        println("                   Пример: rate-company <id> 4.5");
        println("  rate-distributor <id> <рейтинг>");
        println("                   - Оценява дистрибутор (1.0 - 5.0)");
        println("  rate-cashier <id> <рейтинг>");
        println("                   - Оценява касиер (1.0 - 5.0)");
        
        println("\nИЗВЕСТИЯ:");
        println("  notifications         - Показва всички известия");
        println("  check-upcoming-trips [часове]");
        println("                       - Проверява наближаващи пътувания с непродадени билети");
        println("                       Пример: check-upcoming-trips 24 (следващите 24 часа)");
        
        println("\n" + "-".repeat(60));
        println("СЪВЕТ: Въведете 'who' за да видите ID-тата на потребителите и организациите.");
        println("СЪВЕТ: Рейтингите са от 1.0 до 5.0.");
        println("СЪВЕТ: За дата формат: YYYY-MM-DDTHH:mm (пример: 2024-01-15T10:30)");
        println("=".repeat(60) + "\n");
    }

    private void seed() {
        println("\nИнициализиране на системата...");
        userRepo.findAll().forEach(u -> userRepo.deleteById(u.getId()));
        companyRepo.findAll().forEach(c -> companyRepo.deleteById(c.getId()));
        distributorRepo.findAll().forEach(d -> distributorRepo.deleteById(d.getId()));
        cashierRepo.findAll().forEach(c -> cashierRepo.deleteById(c.getId()));
        tripRepo.findAll().forEach(t -> tripRepo.deleteById(t.getId()));
        requestRepo.findAll().forEach(r -> requestRepo.deleteById(r.getId()));
        ticketRepo.findAll().forEach(t -> ticketRepo.deleteById(t.getId()));
        notificationRepo.findAll().forEach(n -> notificationRepo.deleteById(n.getId()));
        
        // Reset ID generators
        com.transport.ticketing.util.IdGenerator.reset();

        admin = userService.createUser("admin@example.com", "Admin", Role.ADMIN);
        companyUser = userService.createUser("company@example.com", "CompanyOwner", Role.COMPANY);
        distributorUser = userService.createUser("distributor@example.com", "DistributorOwner", Role.DISTRIBUTOR);
        cashierUser = userService.createUser("cashier@example.com", "CashierUser", Role.CASHIER);

        companyId = companyService.createCompany(admin, "CityTransport", 5.0, "contact@company", companyUser.getId()).getId();
        distributorId = distributorService.createDistributor(admin, companyId, distributorUser.getId(), "BestTickets", 2.0, "contact@dist").getId();
        cashierId = distributorService.createCashier(distributorUser, distributorId, cashierUser.getId(), "FrontDesk", 1.0, "cashier@dist").getId();

        println("Системата е инициализирана успешно!");
        println("   Заредени са примерни потребители и организации.\n");
    }

    private void addTrip(String[] parts) {
        if (parts.length < 5) {
            println("\nГРЕШКА: Недостатъчно параметри!");
            println("   Използване: add-trip <тип> <дестинация> <брой_места> <лимит_на_човек>");
            println("   Пример: add-trip Express Sofia 50 5\n");
            return;
        }
        String type = parts[1];
        String dest = parts[2];
        int seats;
        int limit;
        try {
            seats = Integer.parseInt(parts[3]);
            limit = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            println("\nГРЕШКА: Невалидни числени стойности!");
            println("   Брой места и лимит трябва да са цели числа.");
            println("   Пример: add-trip Express Sofia 50 5\n");
            return;
        }
        LocalDateTime dep = LocalDateTime.now().plusDays(2);
        LocalDateTime arr = dep.plusHours(2);
        Trip trip = tripService.addTrip(companyUser, companyId, type, dest, dep, arr, seats, limit, List.of("BUS"));
        println("\nПътуването е създадено успешно!");
        println("   ID: " + trip.getId());
        println("   Тип: " + type);
        println("   Дестинация: " + dest);
        println("   Места: " + seats);
        println("   Лимит на човек: " + limit + " билета\n");
    }

    private void requestTrip(String[] parts) {
        if (parts.length < 2) {
            println("Usage: request-trip <tripId>");
            return;
        }
        String tripId = parts[1];
        TripRequest req = tripService.requestTrip(distributorUser, distributorId, tripId);
        notificationCoordinator.onTripRequestSubmitted(req, companyUser.getId());
        println("\nЗаявката е изпратена успешно!");
        println("   ID на заявка: " + req.getId());
        println("   ID на пътуване: " + tripId);
        println("   Известие е изпратено до компанията.\n");
    }

    private void approveRequest(String[] parts) {
        if (parts.length < 3) {
            println("Usage: approve-request <requestId> <yes|no>");
            return;
        }
        String reqId = parts[1];
        boolean approve = "yes".equalsIgnoreCase(parts[2]);
        Trip trip = tripService.approveRequest(companyUser, reqId, approve);
        if (approve) {
            println("\nЗаявката е одобрена успешно!");
        } else {
            println("\nЗаявката е отхвърлена.");
        }
        println("   ID на заявка: " + reqId);
        println("   ID на пътуване: " + trip.getId() + "\n");
    }

    private void sellTicket(String[] parts) {
        if (parts.length < 4) {
            println("\nГРЕШКА: Недостатъчно параметри!");
            println("   Използване: sell-ticket <tripId> <място> <име_купувач> [контакт]");
            println("   Пример: sell-ticket <tripId> 15 Иван Петров 0888123456\n");
            return;
        }
        String tripId = parts[1];
        int seat;
        try {
            seat = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            println("\nГРЕШКА: Номерът на мястото трябва да е цяло число!");
            println("   Пример: sell-ticket <tripId> 15 Иван Петров\n");
            return;
        }
        String buyer = parts[3];
        String contact = parts.length >= 5 ? parts[4] : "н/д";
        Ticket ticket = ticketService.sellTicket(cashierUser, cashierId, tripId, seat, buyer, contact);
        println("\nБилетът е продаден успешно!");
        println("   ID на билет: " + ticket.getId());
        println("   Пътуване: " + tripId);
        println("   Място: " + seat);
        println("   Купувач: " + buyer);
        println("   Контакт: " + contact + "\n");
    }

    private void reportTrips(String[] parts) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        
        if (parts.length >= 2) {
            from = parseDateTime(parts[1]);
        }
        if (parts.length >= 3) {
            to = parseDateTime(parts[2]);
        }
        
        List<Trip> list = reportService.reportTrips(admin, from, to);
        println("\n" + "=".repeat(60));
        println("  ОТЧЕТ ЗА ПЪТУВАНИЯ");
        println("=".repeat(60));
        if (list.isEmpty()) {
            println("\n   Няма намерени пътувания.\n");
        } else {
            println("\n   Намерени " + list.size() + " пътувания:\n");
            for (int i = 0; i < list.size(); i++) {
                Trip t = list.get(i);
                println("   " + (i + 1) + ". ID: " + t.getId());
                println("      Дестинация: " + t.getDestination());
                println("      Заминаване: " + t.getDeparture());
                println("      Пристигане: " + t.getArrival());
                println("      Статус: " + t.getStatus());
                println("      Места: " + t.getSeatsTotal() + "\n");
            }
        }
    }

    private void reportTickets(String[] parts) {
        if (parts.length < 2) {
            println("Usage: report-tickets <tripId> [fromDate] [toDate]");
            return;
        }
        String tripId = parts[1];
        java.time.Instant from = null;
        java.time.Instant to = null;
        
        if (parts.length >= 3) {
            from = parseInstant(parts[2]);
        }
        if (parts.length >= 4) {
            to = parseInstant(parts[3]);
        }
        
        List<Ticket> list = reportService.reportTickets(admin, tripId, from, to);
        println("\n" + "=".repeat(60));
        println("  ОТЧЕТ ЗА БИЛЕТИ - ПЪТУВАНЕ: " + tripId);
        println("=".repeat(60));
        if (list.isEmpty()) {
            println("\n   Няма намерени билети за това пътуване.\n");
        } else {
            println("\n   Намерени " + list.size() + " билета:\n");
            for (int i = 0; i < list.size(); i++) {
                Ticket t = list.get(i);
                println("   " + (i + 1) + ". ID: " + t.getId());
                println("      Място: " + t.getSeatNumber());
                println("      Купувач: " + t.getBuyerName());
                println("      Контакт: " + t.getBuyerContact());
                println("      Статус: " + t.getStatus());
                println("      Продаден на: " + t.getSoldAt() + "\n");
            }
        }
    }

    private void notifications() {
        List<com.transport.ticketing.model.Notification> all = notificationRepo.findAll();
        println("\n" + "=".repeat(60));
        println("  ИЗВЕСТИЯ");
        println("=".repeat(60));
        if (all.isEmpty()) {
            println("\n   Няма нови известия.\n");
        } else {
            println("\n   Намерени " + all.size() + " известия:\n");
            for (int i = 0; i < all.size(); i++) {
                com.transport.ticketing.model.Notification n = all.get(i);
                String readStatus = n.getReadAt() == null ? "Непрочетено" : "Прочетено";
                println("   " + (i + 1) + ". " + readStatus);
                println("      Тип: " + n.getType());
                println("      Потребител: " + n.getUserId());
                println("      Съдържание: " + n.getPayload());
                println("      Дата: " + n.getCreatedAt() + "\n");
            }
        }
    }

    private void reportCompanies(String[] parts) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        
        if (parts.length >= 2) {
            from = parseDateTime(parts[1]);
        }
        if (parts.length >= 3) {
            to = parseDateTime(parts[2]);
        }
        
        List<com.transport.ticketing.model.Company> list = 
            reportService.reportCompaniesWithAvailableTrips(distributorUser, from, to);
        println("\n" + "=".repeat(60));
        println("  ОТЧЕТ: КОМПАНИИ С НАЛИЧНИ ПЪТУВАНИЯ");
        println("=".repeat(60));
        if (list.isEmpty()) {
            println("\n   Няма намерени компании с налични пътувания.\n");
        } else {
            println("\n   Намерени " + list.size() + " компании:\n");
            for (int i = 0; i < list.size(); i++) {
                com.transport.ticketing.model.Company c = list.get(i);
                println("   " + (i + 1) + ". ID: " + c.getId());
                println("      Име: " + c.getName());
                println("      Комисионна: " + c.getCommission() + "%");
                println("      Рейтинг: " + c.getRating() + " / 5.0");
                println("      Контакт: " + c.getContact() + "\n");
            }
        }
    }

    private void reportDistributors(String[] parts) {
        List<com.transport.ticketing.model.Distributor> list = 
            reportService.reportDistributors(admin);
        println("\n" + "=".repeat(60));
        println("  ОТЧЕТ: ДИСТРИБУТОРИ");
        println("=".repeat(60));
        if (list.isEmpty()) {
            println("\n   Няма намерени дистрибутори.\n");
        } else {
            println("\n   Намерени " + list.size() + " дистрибутори:\n");
            for (int i = 0; i < list.size(); i++) {
                com.transport.ticketing.model.Distributor d = list.get(i);
                println("   " + (i + 1) + ". ID: " + d.getId());
                println("      Име: " + d.getName());
                println("      Комисионна: " + d.getCommission() + "%");
                println("      Рейтинг: " + d.getRating() + " / 5.0");
                println("      Компания ID: " + d.getCompanyId());
                println("      Контакт: " + d.getContact() + "\n");
            }
        }
    }

    private void reportCashiers(String[] parts) {
        String distributorId = parts.length >= 2 ? parts[1] : this.distributorId;
        
        List<com.transport.ticketing.model.Cashier> list = 
            reportService.reportCashiers(distributorUser, distributorId);
        println("\n" + "=".repeat(60));
        println("  ОТЧЕТ: КАСИЕРИ (Дистрибутор: " + distributorId + ")");
        println("=".repeat(60));
        if (list.isEmpty()) {
            println("\n   Няма намерени касиери за този дистрибутор.\n");
        } else {
            println("\n   Намерени " + list.size() + " касиери:\n");
            for (int i = 0; i < list.size(); i++) {
                com.transport.ticketing.model.Cashier c = list.get(i);
                println("   " + (i + 1) + ". ID: " + c.getId());
                println("      Име: " + c.getName());
                println("      Комисионна: " + c.getCommission() + "%");
                println("      Рейтинг: " + c.getRating() + " / 5.0");
                println("      Контакт: " + c.getContact() + "\n");
            }
        }
    }

    private void cancelTrip(String[] parts) {
        if (parts.length < 2) {
            println("Usage: cancel-trip <tripId>");
            return;
        }
        String tripId = parts[1];
        Trip trip = tripService.cancelTrip(companyUser, tripId);
        println("\nПътуването е отменено успешно!");
        println("   ID: " + trip.getId());
        println("   Известия са изпратени до всички заинтересовани страни.\n");
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || "null".equalsIgnoreCase(dateStr)) {
            return null;
        }
        try {
            // Try ISO format first
            return LocalDateTime.parse(dateStr);
        } catch (DateTimeParseException e) {
            try {
                // Try simpler format: yyyy-MM-ddTHH:mm
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException e2) {
                println("Warning: Invalid date format '" + dateStr + "', ignoring filter.");
                return null;
            }
        }
    }

    private java.time.Instant parseInstant(String instantStr) {
        if (instantStr == null || instantStr.isEmpty() || "null".equalsIgnoreCase(instantStr)) {
            return null;
        }
        try {
            return java.time.Instant.parse(instantStr);
        } catch (DateTimeParseException e) {
            println("Warning: Invalid instant format '" + instantStr + "', ignoring filter.");
            return null;
        }
    }

    private void updateCompany(String[] parts) {
        if (parts.length < 2) {
            println("Usage: update-company <companyId> [name] [commission] [contact]");
            println("  Use '-' to skip a field, e.g., update-company <id> newName - - newContact");
            return;
        }
        String companyId = parts[1];
        String name = parts.length >= 3 && !"-".equals(parts[2]) ? parts[2] : null;
        Double commission = null;
        if (parts.length >= 4 && !"-".equals(parts[3])) {
            try {
                commission = Double.parseDouble(parts[3]);
            } catch (NumberFormatException e) {
                println("Error: Invalid commission value: " + parts[3]);
                return;
            }
        }
        String contact = parts.length >= 5 && !"-".equals(parts[4]) ? parts[4] : null;
        
        com.transport.ticketing.model.Company company = 
            companyService.updateCompany(companyUser, companyId, name, commission, contact);
        println("\nПрофилът на компанията е обновен успешно!");
        println("   ID: " + company.getId());
        println("   Име: " + company.getName());
        println("   Комисионна: " + company.getCommission() + "%");
        println("   Контакт: " + company.getContact() + "\n");
    }

    private void updateDistributor(String[] parts) {
        if (parts.length < 2) {
            println("Usage: update-distributor <distributorId> [name] [commission] [contact]");
            println("  Use '-' to skip a field");
            return;
        }
        String distributorId = parts[1];
        String name = parts.length >= 3 && !"-".equals(parts[2]) ? parts[2] : null;
        Double commission = null;
        if (parts.length >= 4 && !"-".equals(parts[3])) {
            try {
                commission = Double.parseDouble(parts[3]);
            } catch (NumberFormatException e) {
                println("Error: Invalid commission value: " + parts[3]);
                return;
            }
        }
        String contact = parts.length >= 5 && !"-".equals(parts[4]) ? parts[4] : null;
        
        com.transport.ticketing.model.Distributor distributor = 
            distributorService.updateDistributor(distributorUser, distributorId, name, commission, contact);
        println("\nПрофилът на дистрибутора е обновен успешно!");
        println("   ID: " + distributor.getId());
        println("   Име: " + distributor.getName());
        println("   Комисионна: " + distributor.getCommission() + "%");
        println("   Контакт: " + distributor.getContact() + "\n");
    }

    private void updateCashier(String[] parts) {
        if (parts.length < 2) {
            println("Usage: update-cashier <cashierId> [name] [commission] [contact]");
            println("  Use '-' to skip a field");
            return;
        }
        String cashierId = parts[1];
        String name = parts.length >= 3 && !"-".equals(parts[2]) ? parts[2] : null;
        Double commission = null;
        if (parts.length >= 4 && !"-".equals(parts[3])) {
            try {
                commission = Double.parseDouble(parts[3]);
            } catch (NumberFormatException e) {
                println("Error: Invalid commission value: " + parts[3]);
                return;
            }
        }
        String contact = parts.length >= 5 && !"-".equals(parts[4]) ? parts[4] : null;
        
        com.transport.ticketing.model.Cashier cashier = 
            distributorService.updateCashier(distributorUser, cashierId, name, commission, contact);
        println("\nПрофилът на касиера е обновен успешно!");
        println("   ID: " + cashier.getId());
        println("   Име: " + cashier.getName());
        println("   Комисионна: " + cashier.getCommission() + "%");
        println("   Контакт: " + cashier.getContact() + "\n");
    }

    private void rateCompany(String[] parts) {
        if (parts.length < 3) {
            println("Usage: rate-company <companyId> <rating>");
            println("  Rating must be between " + RatingService.getMinRating() + " and " + RatingService.getMaxRating());
            return;
        }
        String companyId = parts[1];
        double rating;
        try {
            rating = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            println("Error: Invalid rating value: " + parts[2]);
            println("  Rating must be a number between " + RatingService.getMinRating() + " and " + RatingService.getMaxRating());
            return;
        }
        
        com.transport.ticketing.model.Company company = 
            ratingService.rateCompany(distributorUser, companyId, rating);
        println("\nКомпанията е оценена успешно!");
        println("   ID: " + company.getId());
        println("   Име: " + company.getName());
        println("   Нов рейтинг: " + company.getRating() + " / 5.0\n");
    }

    private void rateDistributor(String[] parts) {
        if (parts.length < 3) {
            println("Usage: rate-distributor <distributorId> <rating>");
            println("  Rating must be between " + RatingService.getMinRating() + " and " + RatingService.getMaxRating());
            return;
        }
        String distributorId = parts[1];
        double rating;
        try {
            rating = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            println("Error: Invalid rating value: " + parts[2]);
            println("  Rating must be a number between " + RatingService.getMinRating() + " and " + RatingService.getMaxRating());
            return;
        }
        
        com.transport.ticketing.model.Distributor distributor = 
            ratingService.rateDistributor(companyUser, distributorId, rating);
        println("\nДистрибуторът е оценен успешно!");
        println("   ID: " + distributor.getId());
        println("   Име: " + distributor.getName());
        println("   Нов рейтинг: " + distributor.getRating() + " / 5.0\n");
    }

    private void rateCashier(String[] parts) {
        if (parts.length < 3) {
            println("Usage: rate-cashier <cashierId> <rating>");
            println("  Rating must be between " + RatingService.getMinRating() + " and " + RatingService.getMaxRating());
            return;
        }
        String cashierId = parts[1];
        double rating;
        try {
            rating = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            println("Error: Invalid rating value: " + parts[2]);
            println("  Rating must be a number between " + RatingService.getMinRating() + " and " + RatingService.getMaxRating());
            return;
        }
        
        com.transport.ticketing.model.Cashier cashier = 
            ratingService.rateCashier(distributorUser, cashierId, rating);
        println("\nКасиерът е оценен успешно!");
        println("   ID: " + cashier.getId());
        println("   Име: " + cashier.getName());
        println("   Нов рейтинг: " + cashier.getRating() + " / 5.0\n");
    }

    private void who() {
        println("\n" + "=".repeat(60));
        println("  ТЕКУЩИ ПОТРЕБИТЕЛИ И ОРГАНИЗАЦИИ");
        println("=".repeat(60));
        println("\nПОТРЕБИТЕЛИ:");
        println("  Администратор:  " + admin.getId() + " (" + admin.getEmail() + ")");
        println("  Компания:       " + companyUser.getId() + " (" + companyUser.getEmail() + ")");
        println("  Дистрибутор:    " + distributorUser.getId() + " (" + distributorUser.getEmail() + ")");
        println("  Касиер:         " + cashierUser.getId() + " (" + cashierUser.getEmail() + ")");
        println("\nОРГАНИЗАЦИИ:");
        println("  Компания ID:    " + companyId);
        println("  Дистрибутор ID: " + distributorId);
        println("  Касиер ID:      " + cashierId);
        println("\nСЪВЕТ: Използвайте тези ID-та в командите!\n");
    }

    private void checkUpcomingTrips(String[] parts) {
        int hoursAhead = 24; // По подразбиране проверяваме за следващите 24 часа
        if (parts.length >= 2) {
            try {
                hoursAhead = Integer.parseInt(parts[1]);
                if (hoursAhead <= 0) {
                    println("\nГРЕШКА: Броят часове трябва да е положително число!");
                    println("   Пример: check-upcoming-trips 24\n");
                    return;
                }
            } catch (NumberFormatException e) {
                println("\nГРЕШКА: Невалиден брой часове: " + parts[1]);
                println("   Пример: check-upcoming-trips 24\n");
                return;
            }
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkUntil = now.plusHours(hoursAhead);

        List<Trip> allTrips = tripRepo.findAll();
        List<Trip> upcomingTrips = allTrips.stream()
                .filter(trip -> trip.getStatus() == TripStatus.ACTIVE || trip.getStatus() == TripStatus.APPROVED)
                .filter(trip -> trip.getDeparture().isAfter(now) && trip.getDeparture().isBefore(checkUntil))
                .collect(java.util.stream.Collectors.toList());
        
        if (upcomingTrips.isEmpty()) {
            println("\nНяма наближаващи пътувания в следващите " + hoursAhead + " часа.\n");
            return;
        }
        
        println("\nПроверка за наближаващи пътувания (следващите " + hoursAhead + " часа)...");
        notificationCoordinator.notifyUpcomingWithUnsold(upcomingTrips, now);
        
        long notifiedCount = upcomingTrips.stream()
                .filter(trip -> {
                    long sold = ticketRepo.findByTripId(trip.getId()).size();
                    return sold < trip.getSeatsTotal();
                })
                .count();
        
        println("Проверени " + upcomingTrips.size() + " пътувания.");
        if (notifiedCount > 0) {
            println("Изпратени известия за " + notifiedCount + " пътувания с непродадени билети.\n");
        } else {
            println("Всички пътувания са напълно продадени.\n");
        }
    }

    private void println(String s) {
        System.out.println(s);
    }

    private void print(String s) {
        System.out.print(s);
    }
}
