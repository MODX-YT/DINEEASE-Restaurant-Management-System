package dineease;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class DINEEASETest {
    private static int passed;
    private static int failed;

    // Prints each assertion's pass/fail outcome.
    private static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("[PASS] " + name); }
        else { failed++; System.out.println("[FAIL] " + name); }
    }

    // Compares money amounts exactly.
    private static boolean moneyEquals(BigDecimal actual, String expected) {
        return actual.compareTo(new BigDecimal(expected)) == 0;
    }

    // Tests menu content and duplicate item enforcement.
    private static void testMenu() {
        MenuManager menu = new MenuManager();
        check("Menu contains 11 items", menu.getAllItems().size() == 11);
        check("Chicken Adobo ID is valid", menu.getItemById(101) != null);
        check("Invalid item ID returns null", menu.getItemById(999) == null);
        check("Chicken Adobo costs PHP 180", moneyEquals(menu.getItemById(101).getPrice(), "180.00"));
        boolean rejected = false;
        try { menu.addItem(new MenuItem(101, "Duplicate Food", "Main Dish", new BigDecimal("100.00"))); }
        catch (IllegalArgumentException e) { rejected = true; }
        check("Duplicate menu ID is rejected", rejected);
    }

    // Tests cart prices, updates, and invalid quantity checks.
    private static void testCart() {
        MenuManager menu = new MenuManager();
        Order order = new Order(1, "Juan", Order.OrderType.TAKEOUT, null);
        order.addItem(menu.getItemById(101), 2);
        order.addItem(menu.getItemById(301), 1);
        check("Initial cart subtotal is PHP 415", moneyEquals(order.calculateSubtotal(), "415.00"));
        order.changeQuantity(101, 3);
        check("Updated cart subtotal is PHP 595", moneyEquals(order.calculateSubtotal(), "595.00"));
        order.removeItem(301);
        check("Removing Iced Tea leaves PHP 540", moneyEquals(order.calculateSubtotal(), "540.00"));
        boolean rejected = false;
        try { order.addItem(menu.getItemById(101), 100); }
        catch (IllegalArgumentException e) { rejected = true; }
        check("Quantity above 99 is rejected", rejected);
    }

    // Tests billing polymorphism and insufficient payment rejection.
    private static void testBilling() {
        MenuManager menu = new MenuManager();
        Order takeoutOrder = new Order(2, "Maria", Order.OrderType.TAKEOUT, null);
        takeoutOrder.addItem(menu.getItemById(101), 2);
        Billing takeout = new TakeoutBilling(takeoutOrder);
        check("Takeout billing type", takeout.getBillingType().equals("TAKEOUT"));
        check("Takeout total is PHP 360", moneyEquals(takeout.calculateTotal(), "360.00"));
        check("Takeout change is PHP 140", moneyEquals(takeout.calculateChange(new BigDecimal("500.00")), "140.00"));
        Order dineInOrder = new Order(3, "Pedro", Order.OrderType.DINE_IN, 3);
        dineInOrder.addItem(menu.getItemById(301), 2);
        Billing dineIn = new DineInBilling(dineInOrder);
        check("Dine-in billing type", dineIn.getBillingType().equals("DINE-IN"));
        check("Dine-in total is PHP 110", moneyEquals(dineIn.calculateTotal(), "110.00"));
        boolean rejected = false;
        try { dineIn.calculateChange(new BigDecimal("100.00")); }
        catch (IllegalArgumentException e) { rejected = true; }
        check("Insufficient payment is rejected", rejected);
    }

    // Tests booking collision rules and cancellation.
    private static void testReservationModel() {
        LocalDateTime slot = LocalDateTime.of(2027, 1, 10, 18, 0);
        Reservation reservation = new Reservation(1, "Juan", 1, 2, slot);
        check("Overlapping reservation detected", reservation.conflictsWith(1, slot.plusMinutes(30), slot.plusHours(1)));
        check("Different table does not conflict", !reservation.conflictsWith(2, slot.plusMinutes(30), slot.plusHours(1)));
        check("Adjacent booking is allowed", !reservation.conflictsWith(1, slot.plusHours(2), slot.plusHours(4)));
        reservation.cancel();
        check("Cancelled booking no longer conflicts", !reservation.conflictsWith(1, slot.plusMinutes(30), slot.plusHours(1)));
    }

    // Tests reservation creation, ownership, and valid dining windows.
    private static void testReservationIntegration() {
        TableManager tables = new TableManager();
        ReservationManager manager = new ReservationManager(tables);
        LocalDateTime slot = LocalDateTime.now().plusDays(1).withHour(18).withMinute(0)
                .withSecond(0).withNano(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String scripted = "2\nJuan Dela Cruz\n1\n2\n" + slot.format(formatter) + "\n0\n";
        Scanner scanner = new Scanner(scripted);
        manager.open(scanner);
        scanner.close();
        LocalDateTime arrival = slot.plusMinutes(10);
        LocalDateTime departure = slot.plusMinutes(90);
        check("Reservation is stored", manager.isTableReservedBetween(1, arrival, departure));
        check("Reservation owner can use table", manager.canUseTableForOrder(1, "Juan Dela Cruz", 1, arrival, departure));
        check("Wrong customer is rejected", !manager.canUseTableForOrder(1, "Other Customer", 1, arrival, departure));
        check("Wrong reservation ID is rejected", !manager.canUseTableForOrder(1, "Juan Dela Cruz", 999, arrival, departure));
        check("Booking cannot be used before start", !manager.canUseTableForOrder(1, "Juan Dela Cruz", 1,
                slot.minusMinutes(30), slot.plusMinutes(60)));
        check("Unreserved table remains available", manager.canUseTableForOrder(2, "New Customer", 0, arrival, departure));
    }

    // Runs the complete OOP and business-logic test suite.
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       DINEEASE V1.6 TEST SUITE");
        System.out.println("========================================");
        testMenu();
        testCart();
        testBilling();
        testReservationModel();
        testReservationIntegration();
        System.out.println("\n========================================");
        System.out.println("PASSED : " + passed);
        System.out.println("FAILED : " + failed);
        System.out.println("========================================");
        if (failed > 0) throw new AssertionError(failed + " test(s) failed.");
        System.out.println("ALL AUTOMATED TESTS PASSED!");
    }
}
