package dineease;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DINEEASEPersistenceTest {
    private static int passed;
    private static int failed;

    // Logs pass/fail result of a persisted-data check.
    private static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("[PASS] " + name); }
        else { failed++; System.out.println("[FAIL] " + name); }
    }

    // Tests a full save/load round trip in an isolated temp folder.
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("dineease_test_");
        Path testFile = directory.resolve("test.properties");
        String previous = System.getProperty("dineease.dataFile");
        System.setProperty("dineease.dataFile", testFile.toString());
        try {
            TableManager tables = new TableManager();
            tables.getTableById(3).setOccupied(true);
            ReservationManager reservations = new ReservationManager(tables);
            LocalDateTime date = LocalDateTime.of(2027, 1, 10, 18, 0);
            ArrayList<Reservation> bookings = new ArrayList<>();
            bookings.add(new Reservation(1, "Juan", 1, 2, date));
            Reservation cancelled = new Reservation(2, "Maria", 2, 2, date);
            cancelled.cancel();
            bookings.add(cancelled);
            reservations.restoreReservations(bookings);
            MenuManager menu = new MenuManager();
            OrderManager orders = new OrderManager(menu, tables, reservations);
            Order draft = new Order(10, "Juan", Order.OrderType.TAKEOUT, null);
            draft.addItem(menu.getItemById(101), 2);
            ArrayList<ReceiptRecord> history = new ArrayList<>();
            history.add(new ReceiptRecord(9, "Maria", date, new BigDecimal("55.00"),
                    "SAVED TEST RECEIPT\nCHANGE   : PHP 45.00\n"));
            orders.restorePersistedState(draft, 0, 11, history);
            DataStore.save(tables, reservations, orders);
            DataStore.State loaded = DataStore.load();
            check("Saved data file exists", Files.exists(testFile));
            check("Saved state loads", loaded != null);
            if (loaded == null) throw new AssertionError("No saved state returned.");
            TableManager restoredTables = new TableManager();
            restoredTables.restoreTables(loaded.tables);
            ReservationManager restoredReservations = new ReservationManager(restoredTables);
            restoredReservations.restoreReservations(loaded.reservations);
            OrderManager restoredOrders = new OrderManager(menu, restoredTables, restoredReservations);
            restoredOrders.restorePersistedState(loaded.draft, loaded.draftReservationId,
                    loaded.nextOrderId, loaded.receipts);
            check("Five restaurant tables restored", loaded.tables.size() == 5);
            check("Occupied table status restored", restoredTables.getTableById(3).isOccupied());
            check("Both reservation records restored", loaded.reservations.size() == 2);
            check("Active reservation is available", restoredReservations.isTableReservedBetween(1, date, date.plusHours(1)));
            check("Cancelled reservation stays cancelled", loaded.reservations.get(1).isCancelled());
            check("Reservation ownership validation restored", !restoredReservations.canUseTableForOrder(
                    1, "Other Customer", 0, date, date.plusHours(1)));
            check("Unfinished order draft restored", restoredOrders.getCurrentDraft() != null
                    && restoredOrders.getCurrentDraft().getOrderId() == 10);
            check("Draft subtotal is PHP 360", restoredOrders.getCurrentDraft().calculateSubtotal()
                    .compareTo(new BigDecimal("360.00")) == 0);
            check("Next order ID preserved", restoredOrders.getNextOrderId() == 11);
            check("Transaction history restored", restoredOrders.getReceiptsSnapshot().size() == 1);
            check("Original receipt text preserved", restoredOrders.getReceiptsSnapshot().get(0)
                    .getReceiptText().contains("CHANGE   : PHP 45.00"));
            System.out.println("\n========================================");
            System.out.println("PASSED : " + passed);
            System.out.println("FAILED : " + failed);
            System.out.println("========================================");
            if (failed > 0) throw new AssertionError(failed + " persistence tests failed.");
            System.out.println("ALL PERSISTENCE TESTS PASSED!");
        } finally {
            if (previous == null) System.clearProperty("dineease.dataFile");
            else System.setProperty("dineease.dataFile", previous);
            Files.deleteIfExists(testFile);
            Files.deleteIfExists(directory);
        }
    }
}
