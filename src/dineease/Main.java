package dineease;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final TableManager tableManager = new TableManager();
    private final ReservationManager reservationManager = new ReservationManager(tableManager);
    private final MenuManager menuManager = new MenuManager();
    private final OrderManager orderManager =
            new OrderManager(menuManager, tableManager, reservationManager);
    private boolean running = true;
    private boolean persistenceEnabled = !Boolean.getBoolean("dineease.testMode");

    // Launches the restaurant management program.
    public static void main(String[] args) { new Main().start(); }

    // Loads saved records without overwriting corrupt data.
    private void loadData() {
        if (!persistenceEnabled) return;
        try {
            DataStore.State state = DataStore.load();
            if (state == null) {
                System.out.println("No saved records. Starting with default tables.");
                return;
            }
            tableManager.restoreTables(state.tables);
            reservationManager.restoreReservations(state.reservations);
            orderManager.restorePersistedState(state.draft, state.draftReservationId,
                    state.nextOrderId, state.receipts);
            System.out.println("SUCCESS: Saved DINEEASE records restored!");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("ERROR: Could not load saved data: " + e.getMessage());
            System.err.println("Auto-save disabled to protect existing data.");
            persistenceEnabled = false;
        }
    }

    // Writes current table, booking, order and transaction state to disk.
    private void saveData() {
        if (!persistenceEnabled) return;
        try {
            DataStore.save(tableManager, reservationManager, orderManager);
            System.out.println("DINEEASE records saved successfully.");
        } catch (IOException e) {
            System.err.println("ERROR: Data could not be saved: " + e.getMessage());
        }
    }

    // Runs the approved four-option console main menu.
    public void start() {
        loadData();
        while (running) {
            displayMainMenu();
            int choice = readChoice();
            switch (choice) {
                case 1: reservationManager.open(scanner); saveData(); break;
                case 2: menuManager.open(scanner); break;
                case 3: orderManager.open(scanner); saveData(); break;
                case 4:
                    saveData();
                    running = false;
                    System.out.println("\nThank you for using DINEEASE!");
                    break;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // Displays the four approved DINEEASE operations.
    private void displayMainMenu() {
        System.out.println("\n========================================");
        System.out.println("                DINEEASE");
        System.out.println("     RESTAURANT MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("[1] View Tables & Reservation");
        System.out.println("[2] Display Menu");
        System.out.println("[3] Create Order & Checkout");
        System.out.println("[4] Exit");
        System.out.println("========================================");
        System.out.print("Enter your choice: ");
    }

    // Converts user input to a menu number safely.
    private int readChoice() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}
