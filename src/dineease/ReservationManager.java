package dineease;

import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ReservationManager {
    private final ArrayList<Reservation> reservations = new ArrayList<>();
    private final TableManager tableManager;
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                             .withResolverStyle(java.time.format.ResolverStyle.STRICT);
    private int nextReservationId = 1;

    // Connects reservations to restaurant tables.
    public ReservationManager(TableManager tableManager) {
        this.tableManager = tableManager;
    }

    // Runs the table and reservation menu.
    public void open(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println("     DINEEASE TABLES & RESERVATIONS");
            System.out.println("========================================");
            System.out.println("[1] View All Tables");
            System.out.println("[2] Reserve a Table");
            System.out.println("[3] View Reservations");
            System.out.println("[4] Cancel Reservation");
            System.out.println("[5] Manage Tables");
            System.out.println("[0] Back to Main Menu");
            System.out.println("========================================");
            int choice = readInt(scanner, "Enter choice: ");
            switch (choice) {
                case 1: tableManager.showAllTables(); break;
                case 2: createReservation(scanner); break;
                case 3: viewReservations(); break;
                case 4: cancelReservation(scanner); break;
                case 5: tableManager.open(scanner); break;
                case 0: running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // Reads a whole number safely.
    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Please enter a valid number."); }
        }
    }

    // Creates a reservation after checking capacity and overlapping bookings.
    private void createReservation(Scanner scanner) {
        System.out.println("\n========== NEW RESERVATION ==========");
        tableManager.showAllTables();
        System.out.print("Customer Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) { System.out.println("ERROR: Name cannot be empty."); return; }
        int tableId = readInt(scanner, "Table ID: ");
        RestaurantTable table = tableManager.getTableById(tableId);
        if (table == null) { System.out.println("ERROR: Table does not exist."); return; }
        int guests = readInt(scanner, "Number of Guests: ");
        if (guests <= 0 || guests > table.getCapacity()) {
            System.out.println("ERROR: Invalid guest count. Capacity: " + table.getCapacity());
            return;
        }
        System.out.print("Date and Time (yyyy-MM-dd HH:mm): ");
        LocalDateTime start;
        try { start = LocalDateTime.parse(scanner.nextLine().trim(), formatter); }
        catch (DateTimeParseException e) {
            System.out.println("ERROR: Invalid date or time."); return;
        }
        if (start.isBefore(LocalDateTime.now())) {
            System.out.println("ERROR: Cannot book a past date/time."); return;
        }
        LocalDateTime end = start.plusHours(2);
        if (isTableReservedBetween(tableId, start, end)) {
            System.out.println("ERROR: DOUBLE BOOKING DETECTED!");
            System.out.println("Table is already reserved for an overlapping time slot.");
            return;
        }
        Reservation reservation = new Reservation(nextReservationId++, name, tableId, guests, start);
        reservations.add(reservation);
        System.out.println("\n========================================");
        System.out.println("       RESERVATION SUCCESSFUL!");
        System.out.println("========================================");
        System.out.println("Reservation ID: " + reservation.getReservationId());
        System.out.println("Customer      : " + name);
        System.out.println("Table         : " + tableId);
        System.out.println("Guests        : " + guests);
        System.out.println("Date & Time   : " + start.format(formatter));
        System.out.println("Until         : " + end.format(formatter));
        System.out.println("========================================");
    }

    // Prints reservation records, including cancelled ones.
    private void viewReservations() {
        System.out.println("\n========== RESERVATION RECORDS ==========");
        if (reservations.isEmpty()) { System.out.println("No reservations found."); return; }
        for (Reservation reservation : reservations) {
            System.out.println("----------------------------------------");
            System.out.println("ID       : " + reservation.getReservationId());
            System.out.println("Customer : " + reservation.getCustomerName());
            System.out.println("Table    : " + reservation.getTableId());
            System.out.println("Guests   : " + reservation.getGuests());
            System.out.println("Schedule : " + reservation.getStartDateTime().format(formatter));
            System.out.println("Status   : " + (reservation.isCancelled() ? "CANCELLED" : "ACTIVE"));
        }
        System.out.println("----------------------------------------");
    }

    // Cancels an existing reservation by ID.
    private void cancelReservation(Scanner scanner) {
        viewReservations();
        if (reservations.isEmpty()) return;
        int id = readInt(scanner, "Enter Reservation ID: ");
        for (Reservation reservation : reservations) {
            if (reservation.getReservationId() == id) {
                if (reservation.isCancelled()) System.out.println("Reservation is already cancelled.");
                else { reservation.cancel(); System.out.println("Reservation cancelled successfully!"); }
                return;
            }
        }
        System.out.println("ERROR: Reservation not found.");
    }

    // Checks whether a time interval conflicts with a booking.
    public boolean isTableReservedBetween(int tableId, LocalDateTime start, LocalDateTime end) {
        for (Reservation reservation : reservations) {
            if (reservation.conflictsWith(tableId, start, end)) return true;
        }
        return false;
    }

    // Verifies customer ownership for a table over a dining interval.
    public boolean canUseTableForOrder(int tableId, String customerName, int reservationId,
                                       LocalDateTime start, LocalDateTime end) {
        if (customerName == null || customerName.trim().isEmpty() || start == null || end == null
                || !end.isAfter(start)) return false;
        boolean matchedReservation = false;
        for (Reservation reservation : reservations) {
            if (!reservation.conflictsWith(tableId, start, end)) continue;
            if (reservationId <= 0 || reservation.getReservationId() != reservationId
                    || !reservation.getCustomerName().trim().equalsIgnoreCase(customerName.trim())
                    || start.isBefore(reservation.getStartDateTime())
                    || !start.isBefore(reservation.getEndDateTime())) return false;
            matchedReservation = true;
        }
        return reservationId > 0 ? matchedReservation : true;
    }

    // Returns a list snapshot for persistence.
    public ArrayList<Reservation> getReservationsSnapshot() { return new ArrayList<>(reservations); }

    // Restores reservations and advances the next reservation ID.
    public void restoreReservations(ArrayList<Reservation> saved) {
        if (saved == null) return;
        reservations.clear();
        reservations.addAll(saved);
        nextReservationId = 1;
        for (Reservation r : reservations) {
            nextReservationId = Math.max(nextReservationId, r.getReservationId() + 1);
        }
    }
}
