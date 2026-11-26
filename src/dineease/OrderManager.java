package dineease;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class OrderManager {
    private final MenuManager menuManager;
    private final TableManager tableManager;
    private final ReservationManager reservationManager;
    private final ArrayList<ReceiptRecord> receiptHistory = new ArrayList<>();
    private Order currentDraft;
    private int currentReservationId;
    private int nextOrderId = 1;

    // Connects orders, food items, tables, and reservations.
    public OrderManager(MenuManager menuManager, TableManager tableManager,
                        ReservationManager reservationManager) {
        this.menuManager = menuManager;
        this.tableManager = tableManager;
        this.reservationManager = reservationManager;
    }

    // Runs the cart, checkout, and transaction-history menu.
    public void open(Scanner scanner) {
        if (currentDraft == null) {
            createDraft(scanner);
            if (currentDraft == null) return;
        } else {
            System.out.println("\nResuming Order #" + currentDraft.getOrderId());
        }
        while (currentDraft != null) {
            System.out.println("\n========================================");
            System.out.println("       DINEEASE ORDER MANAGEMENT");
            System.out.println("========================================");
            System.out.println("Order ID : " + currentDraft.getOrderId());
            System.out.println("Customer : " + currentDraft.getCustomerName());
            System.out.println("Type     : " + currentDraft.getOrderType());
            if (currentDraft.getTableId() != null)
                System.out.println("Table    : " + currentDraft.getTableId());
            if (currentReservationId > 0)
                System.out.println("Reservation ID : " + currentReservationId);
            System.out.println("----------------------------------------");
            System.out.println("[1] Add Food to Cart");
            System.out.println("[2] View Cart");
            System.out.println("[3] Change Item Quantity");
            System.out.println("[4] Remove Item");
            System.out.println("[5] Cancel Draft Order");
            System.out.println("[6] Checkout & Print Receipt");
            System.out.println("[7] View Transaction History");
            System.out.println("[0] Back to Main Menu");
            System.out.println("========================================");
            int choice = readInt(scanner, "Enter choice: ");
            switch (choice) {
                case 1: addFood(scanner); break;
                case 2: displayCart(); break;
                case 3: updateQuantity(scanner); break;
                case 4: removeFood(scanner); break;
                case 5: if (cancelDraft(scanner)) return; break;
                case 6: if (checkout(scanner)) return; break;
                case 7: viewTransactionHistory(scanner); break;
                case 0: System.out.println("Draft saved for this session."); return;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Reads a valid whole-number selection.
    private int readInt(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid whole number.");
            }
        }
    }

    // Creates an order draft with optional reservation verification.
    private void createDraft(Scanner scanner) {
        System.out.println("\n========== CREATE NEW ORDER ==========");
        System.out.print("Customer Name (or /history): ");
        String customerName = scanner.nextLine().trim();
        if (customerName.equalsIgnoreCase("/history")) {
            viewTransactionHistory(scanner);
            return;
        }
        if (customerName.isEmpty()) {
            System.out.println("ERROR: Customer name cannot be empty."); return;
        }
        System.out.println("\n[1] Dine-In");
        System.out.println("[2] Takeout");
        System.out.println("[0] Cancel");
        int selection = readInt(scanner, "Order Type: ");
        if (selection == 0) return;
        Order.OrderType orderType;
        Integer tableId = null;
        int reservationId = 0;
        if (selection == 1) {
            orderType = Order.OrderType.DINE_IN;
            tableManager.showAllTables();
            tableId = readInt(scanner, "Enter Table ID: ");
            RestaurantTable table = tableManager.getTableById(tableId);
            if (table == null) {
                System.out.println("ERROR: Table does not exist."); return;
            }
            if (table.isOccupied()) {
                System.out.println("ERROR: Table is currently occupied."); return;
            }
            LocalDateTime now = LocalDateTime.now();
            if (reservationManager.isTableReservedBetween(tableId, now, now.plusHours(2))) {
                System.out.println("NOTICE: Table has a reservation within the next 2 hours.");
                System.out.println("Reserved customers may use the table during their booking time.");
                reservationId = readInt(scanner, "Enter Reservation ID (0 to cancel): ");
                if (reservationId <= 0) {
                    System.out.println("Order creation cancelled."); return;
                }
            }
            if (!validateDineInTable(tableId, customerName, reservationId)) return;
        } else if (selection == 2) {
            orderType = Order.OrderType.TAKEOUT;
        } else {
            System.out.println("Invalid order type."); return;
        }
        currentDraft = new Order(nextOrderId++, customerName, orderType, tableId);
        currentReservationId = reservationId;
        System.out.println("\nSUCCESS: Order draft created!");
        System.out.println("Order ID: " + currentDraft.getOrderId());
        if (reservationId > 0) System.out.println("Reservation verified: #" + reservationId);
    }

    // Checks current occupancy and scheduling conflicts for dine-in use.
    private boolean validateDineInTable(int tableId, String customerName, int reservationId) {
        RestaurantTable table = tableManager.getTableById(tableId);
        if (table == null) {
            System.out.println("ERROR: Table does not exist."); return false;
        }
        if (table.isOccupied()) {
            System.out.println("ERROR: Table is currently occupied."); return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!reservationManager.canUseTableForOrder(
                tableId, customerName, reservationId, now, now.plusHours(2))) {
            System.out.println("ERROR: Table reservation validation failed.");
            System.out.println("Check the reservation ID, customer name, booking time, and other bookings.");
            return false;
        }
        return true;
    }

    // Adds a validated menu item and quantity to the current order.
    private void addFood(Scanner scanner) {
        menuManager.displayMenu();
        int itemId = readInt(scanner, "Enter Food Item ID: ");
        MenuItem item = menuManager.getItemById(itemId);
        if (item == null) { System.out.println("ERROR: Food item not found."); return; }
        int quantity = readInt(scanner, "Enter Quantity (1-99): ");
        try {
            currentDraft.addItem(item, quantity);
            System.out.println("SUCCESS: Added to cart!");
            System.out.println("Food     : " + item.getItemName());
            System.out.println("Quantity : " + quantity);
            System.out.println("Subtotal : PHP " + currentDraft.calculateSubtotal().toPlainString());
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // Prints the cart and calculated subtotal.
    private void displayCart() {
        System.out.println("\n==================================================================");
        System.out.println("                      DINEEASE CART");
        System.out.println("==================================================================");
        if (currentDraft.isEmpty()) {
            System.out.println("Your cart is empty."); return;
        }
        System.out.println("Order ID : " + currentDraft.getOrderId());
        System.out.println("Customer : " + currentDraft.getCustomerName());
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-8s %-23s %5s %12s %12s%n",
                "ITEM ID", "FOOD NAME", "QTY", "UNIT PRICE", "LINE TOTAL");
        System.out.println("------------------------------------------------------------------");
        for (OrderItem item : currentDraft.getItems()) {
            System.out.printf("%-8d %-23s %5d %12s %12s%n",
                    item.getMenuItem().getItemId(), item.getMenuItem().getItemName(),
                    item.getQuantity(), item.getMenuItem().getPrice().toPlainString(),
                    item.getLineTotal().toPlainString());
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("SUBTOTAL: PHP " + currentDraft.calculateSubtotal().toPlainString());
        System.out.println("==================================================================");
        System.out.println("Status: DRAFT - NOT YET PAID");
    }

    // Updates the quantity of a food item already in the cart.
    private void updateQuantity(Scanner scanner) {
        if (currentDraft.isEmpty()) { System.out.println("Cart is empty."); return; }
        displayCart();
        int itemId = readInt(scanner, "Enter Item ID to update: ");
        int quantity = readInt(scanner, "Enter New Quantity (1-99): ");
        try {
            if (currentDraft.changeQuantity(itemId, quantity)) {
                System.out.println("SUCCESS: Quantity updated!");
                System.out.println("New Subtotal: PHP " + currentDraft.calculateSubtotal().toPlainString());
            } else System.out.println("ERROR: Item not found in cart.");
        } catch (IllegalArgumentException e) { System.out.println("ERROR: " + e.getMessage()); }
    }

    // Removes an existing food item from the cart.
    private void removeFood(Scanner scanner) {
        if (currentDraft.isEmpty()) { System.out.println("Cart is empty."); return; }
        displayCart();
        int itemId = readInt(scanner, "Enter Item ID to remove: ");
        if (currentDraft.removeItem(itemId)) {
            System.out.println("SUCCESS: Item removed from cart.");
            System.out.println("New Subtotal: PHP " + currentDraft.calculateSubtotal().toPlainString());
        } else System.out.println("ERROR: Item not found in cart.");
    }

    // Cancels the draft without cancelling a scheduled table reservation.
    private boolean cancelDraft(Scanner scanner) {
        System.out.print("Type YES to cancel this order: ");
        if (scanner.nextLine().trim().equalsIgnoreCase("YES")) {
            currentDraft = null;
            currentReservationId = 0;
            System.out.println("Draft order cancelled successfully.");
            return true;
        }
        System.out.println("Cancellation aborted.");
        return false;
    }

    // Calculates the bill, accepts cash, and saves a receipt.
    private boolean checkout(Scanner scanner) {
        if (currentDraft.isEmpty()) {
            System.out.println("ERROR: Cannot checkout an empty cart."); return false;
        }
        RestaurantTable selectedTable = null;
        if (currentDraft.getOrderType() == Order.OrderType.DINE_IN) {
            int tableId = currentDraft.getTableId();
            if (!validateDineInTable(tableId, currentDraft.getCustomerName(), currentReservationId)) {
                System.out.println("Checkout blocked. Draft preserved."); return false;
            }
            selectedTable = tableManager.getTableById(tableId);
        }
        // Runtime polymorphism: one parent reference, two concrete billing types.
        Billing billing = currentDraft.getOrderType() == Order.OrderType.DINE_IN
                ? new DineInBilling(currentDraft) : new TakeoutBilling(currentDraft);
        displayCart();
        BigDecimal total = billing.calculateTotal();
        System.out.println("\n========== DINEEASE CHECKOUT ==========");
        System.out.println("Billing Type : " + billing.getBillingType());
        System.out.println("Total Amount : PHP " + total.toPlainString());
        System.out.println("---------------------------------------");
        while (true) {
            System.out.print("Cash Tendered (C to cancel): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("C")) {
                System.out.println("Checkout cancelled. Draft preserved."); return false;
            }
            BigDecimal cash;
            try { cash = new BigDecimal(input); }
            catch (NumberFormatException e) {
                System.out.println("ERROR: Enter a valid cash amount."); continue;
            }
            if (cash.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("ERROR: Cash must be greater than zero."); continue;
            }
            if (cash.scale() > 2) {
                System.out.println("ERROR: Maximum of two decimal places."); continue;
            }
            if (cash.compareTo(total) < 0) {
                System.out.println("ERROR: Insufficient payment!");
                System.out.println("Amount Due: PHP " + total.subtract(cash).toPlainString());
                continue;
            }
            cash = cash.setScale(2);
            LocalDateTime paidAt = LocalDateTime.now();
            String receiptText = billing.buildReceipt(cash, paidAt);
            System.out.print(receiptText);
            receiptHistory.add(new ReceiptRecord(currentDraft.getOrderId(),
                    currentDraft.getCustomerName(), paidAt, billing.calculateTotal(), receiptText));
            if (selectedTable != null) {
                selectedTable.setOccupied(true);
                System.out.println("Table " + selectedTable.getTableId() + " is now OCCUPIED.");
            }
            currentDraft = null;
            currentReservationId = 0;
            System.out.println("\nCheckout completed successfully!");
            System.out.println("Returning to Main Menu...");
            return true;
        }
    }

    // Lists transactions and reprints a selected original receipt.
    private void viewTransactionHistory(Scanner scanner) {
        System.out.println("\n========== TRANSACTION HISTORY ==========");
        if (receiptHistory.isEmpty()) {
            System.out.println("No completed transactions found."); return;
        }
        for (ReceiptRecord record : receiptHistory) {
            System.out.println("-----------------------------------------");
            System.out.println("Order ID : " + record.getOrderId());
            System.out.println("Customer : " + record.getCustomerName());
            System.out.println("Date     : " + record.getPaidAt());
            System.out.println("Total    : PHP " + record.getTotal().toPlainString());
        }
        System.out.println("-----------------------------------------");
        int id = readInt(scanner, "Enter Order ID to view receipt (0 to back): ");
        if (id == 0) return;
        for (ReceiptRecord record : receiptHistory) {
            if (record.getOrderId() == id) {
                System.out.print(record.getReceiptText()); return;
            }
        }
        System.out.println("ERROR: Transaction not found.");
    }

    // Returns completed receipts for persistence.
    public ArrayList<ReceiptRecord> getReceiptsSnapshot() { return new ArrayList<>(receiptHistory); }
    // Returns a draft for persistence.
    public Order getCurrentDraft() { return currentDraft; }
    // Returns the draft's reservation identifier.
    public int getCurrentReservationId() { return currentReservationId; }
    // Returns the next unused order ID.
    public int getNextOrderId() { return nextOrderId; }

    // Restores persisted orders, receipts, and ID counter.
    public void restorePersistedState(Order savedDraft, int savedReservationId,
                                      int savedNextOrderId, ArrayList<ReceiptRecord> savedReceipts) {
        currentDraft = savedDraft;
        currentReservationId = savedDraft == null ? 0 : savedReservationId;
        receiptHistory.clear();
        if (savedReceipts != null) receiptHistory.addAll(savedReceipts);
        int nextId = Math.max(1, savedNextOrderId);
        if (savedDraft != null) nextId = Math.max(nextId, savedDraft.getOrderId() + 1);
        for (ReceiptRecord record : receiptHistory)
            nextId = Math.max(nextId, record.getOrderId() + 1);
        nextOrderId = nextId;
    }
}
