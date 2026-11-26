package dineease;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Properties;

public final class DataStore {
    private DataStore() { }

    public static class State {
        public final ArrayList<RestaurantTable> tables = new ArrayList<>();
        public final ArrayList<Reservation> reservations = new ArrayList<>();
        public final ArrayList<ReceiptRecord> receipts = new ArrayList<>();
        public Order draft;
        public int draftReservationId;
        public int nextOrderId = 1;
    }

    // Resolves the normal save path or an isolated test file.
    private static Path getFile() {
        String override = System.getProperty("dineease.dataFile");
        if (override != null && !override.trim().isEmpty()) return Paths.get(override);
        return Paths.get("data", "dineease.properties");
    }

    // Reads a required property from disk.
    private static String get(Properties p, String key) throws IOException {
        String value = p.getProperty(key);
        if (value == null) throw new IOException("Missing data: " + key);
        return value;
    }

    // Reads an integer property with validation.
    private static int getInt(Properties p, String key) throws IOException {
        try { return Integer.parseInt(get(p, key)); }
        catch (NumberFormatException e) { throw new IOException("Invalid number: " + key, e); }
    }

    // Reads and limits the number of records to load.
    private static int getCount(Properties p, String key) throws IOException {
        int count = getInt(p, key);
        if (count < 0 || count > 10000) throw new IOException("Invalid record count: " + key);
        return count;
    }

    // Reads a strictly formatted true/false value.
    private static boolean getBool(Properties p, String key) throws IOException {
        String value = get(p, key);
        if ("true".equals(value)) return true;
        if ("false".equals(value)) return false;
        throw new IOException("Invalid boolean: " + key);
    }

    // Loads persisted restaurant tables, bookings, drafts, and receipts.
    public static State load() throws IOException {
        Path file = getFile();
        if (!Files.exists(file)) return null;
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        if (!"1".equals(get(p, "version")))
            throw new IOException("Unsupported data file version.");
        try {
            State state = new State();
            int tableCount = getCount(p, "tables.count");
            for (int i = 0; i < tableCount; i++) {
                String key = "tables." + i + ".";
                RestaurantTable table = new RestaurantTable(
                        getInt(p, key + "id"), getInt(p, key + "capacity"));
                table.setOccupied(getBool(p, key + "occupied"));
                state.tables.add(table);
            }
            int reservationCount = getCount(p, "reservations.count");
            for (int i = 0; i < reservationCount; i++) {
                String key = "reservations." + i + ".";
                Reservation reservation = new Reservation(
                        getInt(p, key + "id"), get(p, key + "name"),
                        getInt(p, key + "table"), getInt(p, key + "guests"),
                        LocalDateTime.parse(get(p, key + "start")));
                if (getBool(p, key + "cancelled")) reservation.cancel();
                state.reservations.add(reservation);
            }
            int receiptCount = getCount(p, "receipts.count");
            for (int i = 0; i < receiptCount; i++) {
                String key = "receipts." + i + ".";
                ReceiptRecord receipt = new ReceiptRecord(
                        getInt(p, key + "id"), get(p, key + "customer"),
                        LocalDateTime.parse(get(p, key + "paidAt")),
                        new BigDecimal(get(p, key + "total")), get(p, key + "text"));
                state.receipts.add(receipt);
            }
            state.nextOrderId = getInt(p, "orders.nextId");
            state.draftReservationId = getInt(p, "draft.reservationId");
            if (getBool(p, "draft.present")) {
                Order.OrderType type = Order.OrderType.valueOf(get(p, "draft.type"));
                Integer tableId = type == Order.OrderType.DINE_IN
                        ? getInt(p, "draft.table") : null;
                Order draft = new Order(getInt(p, "draft.id"), get(p, "draft.customer"),
                        type, tableId);
                int itemCount = getCount(p, "draft.items.count");
                for (int i = 0; i < itemCount; i++) {
                    String key = "draft.items." + i + ".";
                    MenuItem item = new MenuItem(
                            getInt(p, key + "id"), get(p, key + "name"),
                            get(p, key + "category"), new BigDecimal(get(p, key + "price")));
                    draft.addItem(item, getInt(p, key + "quantity"));
                }
                state.draft = draft;
            }
            return state;
        } catch (RuntimeException e) {
            throw new IOException("Invalid saved restaurant data.", e);
        }
    }

    // Saves a complete properties snapshot via a temporary replacement file.
    public static void save(TableManager tables, ReservationManager reservations,
                            OrderManager orders) throws IOException {
        Properties p = new Properties();
        p.setProperty("version", "1");
        ArrayList<RestaurantTable> tableList = tables.getTablesSnapshot();
        p.setProperty("tables.count", String.valueOf(tableList.size()));
        for (int i = 0; i < tableList.size(); i++) {
            RestaurantTable table = tableList.get(i);
            String key = "tables." + i + ".";
            p.setProperty(key + "id", String.valueOf(table.getTableId()));
            p.setProperty(key + "capacity", String.valueOf(table.getCapacity()));
            p.setProperty(key + "occupied", String.valueOf(table.isOccupied()));
        }
        ArrayList<Reservation> bookingList = reservations.getReservationsSnapshot();
        p.setProperty("reservations.count", String.valueOf(bookingList.size()));
        for (int i = 0; i < bookingList.size(); i++) {
            Reservation r = bookingList.get(i);
            String key = "reservations." + i + ".";
            p.setProperty(key + "id", String.valueOf(r.getReservationId()));
            p.setProperty(key + "name", r.getCustomerName());
            p.setProperty(key + "table", String.valueOf(r.getTableId()));
            p.setProperty(key + "guests", String.valueOf(r.getGuests()));
            p.setProperty(key + "start", r.getStartDateTime().toString());
            p.setProperty(key + "cancelled", String.valueOf(r.isCancelled()));
        }
        ArrayList<ReceiptRecord> receipts = orders.getReceiptsSnapshot();
        p.setProperty("receipts.count", String.valueOf(receipts.size()));
        for (int i = 0; i < receipts.size(); i++) {
            ReceiptRecord r = receipts.get(i);
            String key = "receipts." + i + ".";
            p.setProperty(key + "id", String.valueOf(r.getOrderId()));
            p.setProperty(key + "customer", r.getCustomerName());
            p.setProperty(key + "paidAt", r.getPaidAt().toString());
            p.setProperty(key + "total", r.getTotal().toPlainString());
            p.setProperty(key + "text", r.getReceiptText());
        }
        p.setProperty("orders.nextId", String.valueOf(orders.getNextOrderId()));
        p.setProperty("draft.reservationId", String.valueOf(orders.getCurrentReservationId()));
        Order draft = orders.getCurrentDraft();
        p.setProperty("draft.present", String.valueOf(draft != null));
        if (draft != null) {
            p.setProperty("draft.id", String.valueOf(draft.getOrderId()));
            p.setProperty("draft.customer", draft.getCustomerName());
            p.setProperty("draft.type", draft.getOrderType().name());
            if (draft.getTableId() != null)
                p.setProperty("draft.table", String.valueOf(draft.getTableId()));
            p.setProperty("draft.items.count", String.valueOf(draft.getItems().size()));
            for (int i = 0; i < draft.getItems().size(); i++) {
                OrderItem entry = draft.getItems().get(i);
                MenuItem item = entry.getMenuItem();
                String key = "draft.items." + i + ".";
                p.setProperty(key + "id", String.valueOf(item.getItemId()));
                p.setProperty(key + "name", item.getItemName());
                p.setProperty(key + "category", item.getCategory());
                p.setProperty(key + "price", item.getPrice().toPlainString());
                p.setProperty(key + "quantity", String.valueOf(entry.getQuantity()));
            }
        }
        Path file = getFile();
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = file.resolveSibling(file.getFileName().toString() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            p.store(writer, "DINEEASE V1.7 DATA");
        }
        try {
            Files.move(temporary, file,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
