package dineease;

import java.util.ArrayList;
import java.util.Scanner;

public class TableManager {
    private final ArrayList<RestaurantTable> tables = new ArrayList<>();

    // Creates five restaurant tables for a new installation.
    public TableManager() {
        tables.add(new RestaurantTable(1, 2));
        tables.add(new RestaurantTable(2, 2));
        tables.add(new RestaurantTable(3, 4));
        tables.add(new RestaurantTable(4, 4));
        tables.add(new RestaurantTable(5, 6));
    }

    // Opens the table management sub-menu.
    public void open(Scanner scanner) {
        boolean inModule = true;
        while (inModule) {
            System.out.println("\n========================================");
            System.out.println("        DINEEASE TABLE MANAGEMENT");
            System.out.println("========================================");
            System.out.println("[1] View All Tables");
            System.out.println("[2] View Available Tables");
            System.out.println("[3] View Occupied Tables");
            System.out.println("[4] Add New Table");
            System.out.println("[5] Update Table Status");
            System.out.println("[0] Back to Main Menu");
            System.out.println("========================================");
            int choice = readInt(scanner, "Enter your choice: ");
            switch (choice) {
                case 1: displayTables(-1); break;
                case 2: displayTables(0); break;
                case 3: displayTables(1); break;
                case 4: addTable(scanner); break;
                case 5: updateTableStatus(scanner); break;
                case 0: inModule = false; break;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    // Reads whole numbers without crashing on invalid input.
    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    // Displays tables filtered by occupancy (-1 means all).
    private void displayTables(int filter) {
        System.out.println("\n========================================");
        System.out.println(filter == 0 ? "           AVAILABLE TABLES" :
                           filter == 1 ? "            OCCUPIED TABLES" : "             ALL TABLES");
        System.out.println("========================================");
        System.out.printf("%-10s %-12s %-12s%n", "TABLE ID", "CAPACITY", "STATUS");
        System.out.println("----------------------------------------");
        int count = 0;
        for (RestaurantTable table : tables) {
            int status = table.isOccupied() ? 1 : 0;
            if (filter != -1 && status != filter) continue;
            System.out.printf("%-10d %-12d %-12s%n", table.getTableId(),
                    table.getCapacity(), table.getStatus());
            count++;
        }
        if (count == 0) System.out.println("No matching tables found.");
        System.out.println("----------------------------------------");
        System.out.println("Displayed Tables: " + count);
        if (filter == -1) showSummary();
    }

    // Prints table counts by physical occupancy.
    private void showSummary() {
        int available = 0;
        int occupied = 0;
        for (RestaurantTable table : tables) {
            if (table.isOccupied()) occupied++; else available++;
        }
        System.out.println("Total Tables    : " + tables.size());
        System.out.println("Available Tables: " + available);
        System.out.println("Occupied Tables : " + occupied);
    }

    // Finds a table by its identifier.
    private RestaurantTable findTable(int tableId) {
        for (RestaurantTable table : tables) {
            if (table.getTableId() == tableId) return table;
        }
        return null;
    }

    // Adds a nonduplicate table.
    private void addTable(Scanner scanner) {
        System.out.println("\n========== ADD NEW TABLE ==========");
        int tableId = readInt(scanner, "Enter Table ID: ");
        if (tableId <= 0) {
            System.out.println("ERROR: Table ID must be positive."); return;
        }
        if (findTable(tableId) != null) {
            System.out.println("ERROR: Table ID already exists!"); return;
        }
        int capacity = readInt(scanner, "Enter Seating Capacity: ");
        if (capacity <= 0) {
            System.out.println("ERROR: Capacity must be greater than 0."); return;
        }
        RestaurantTable table = new RestaurantTable(tableId, capacity);
        tables.add(table);
        System.out.println("SUCCESS: New table added!");
        System.out.println("Table ID : " + table.getTableId());
        System.out.println("Capacity : " + table.getCapacity());
        System.out.println("Status   : " + table.getStatus());
    }

    // Updates occupancy for an existing table.
    private void updateTableStatus(Scanner scanner) {
        System.out.println("\n========== UPDATE TABLE STATUS ==========");
        displayTables(-1);
        int tableId = readInt(scanner, "Enter Table ID: ");
        RestaurantTable table = findTable(tableId);
        if (table == null) {
            System.out.println("ERROR: Table not found!"); return;
        }
        System.out.println("Selected Table: " + table.getTableId());
        System.out.println("Current Status: " + table.getStatus());
        System.out.println("[1] AVAILABLE");
        System.out.println("[2] OCCUPIED");
        System.out.println("[0] Cancel");
        int choice = readInt(scanner, "Select new status: ");
        if (choice == 0) {
            System.out.println("Status update cancelled."); return;
        }
        if (choice != 1 && choice != 2) {
            System.out.println("Invalid status option."); return;
        }
        table.setOccupied(choice == 2);
        System.out.println("SUCCESS: Table status updated!");
        System.out.println("Table ID  : " + table.getTableId());
        System.out.println("New Status: " + table.getStatus());
    }

    // Displays the complete table list for reservations and ordering.
    public void showAllTables() { displayTables(-1); }

    // Exposes lookup without exposing the mutable list.
    public RestaurantTable getTableById(int tableId) { return findTable(tableId); }

    // Returns a list snapshot for persistence.
    public ArrayList<RestaurantTable> getTablesSnapshot() { return new ArrayList<>(tables); }

    // Restores saved table records after validating IDs.
    public void restoreTables(ArrayList<RestaurantTable> savedTables) {
        if (savedTables == null || savedTables.isEmpty()) return;
        ArrayList<Integer> seen = new ArrayList<>();
        for (RestaurantTable table : savedTables) {
            if (table == null || seen.contains(table.getTableId())) {
                throw new IllegalArgumentException("Invalid or duplicate saved table ID.");
            }
            seen.add(table.getTableId());
        }
        tables.clear();
        tables.addAll(savedTables);
    }
}
