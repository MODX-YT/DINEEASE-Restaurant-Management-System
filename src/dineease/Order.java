package dineease;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    public enum OrderType { DINE_IN, TAKEOUT }

    private final int orderId;
    private final String customerName;
    private final OrderType orderType;
    private final Integer tableId;
    private final ArrayList<OrderItem> items = new ArrayList<>();

    // Creates a validated dine-in or takeout order.
    public Order(int orderId, String customerName, OrderType orderType, Integer tableId) {
        if (orderId <= 0) throw new IllegalArgumentException("Order ID must be positive.");
        if (customerName == null || customerName.trim().isEmpty())
            throw new IllegalArgumentException("Customer name is required.");
        if (orderType == null) throw new IllegalArgumentException("Order type is required.");
        if (orderType == OrderType.DINE_IN && (tableId == null || tableId <= 0))
            throw new IllegalArgumentException("Dine-in orders require a table.");
        if (orderType == OrderType.TAKEOUT && tableId != null)
            throw new IllegalArgumentException("Takeout orders cannot have a table.");
        this.orderId = orderId;
        this.customerName = customerName.trim();
        this.orderType = orderType;
        this.tableId = tableId;
    }

    // Returns order ID.
    public int getOrderId() { return orderId; }
    // Returns customer name.
    public String getCustomerName() { return customerName; }
    // Returns dine-in or takeout type.
    public OrderType getOrderType() { return orderType; }
    // Returns dine-in table ID when applicable.
    public Integer getTableId() { return tableId; }
    // Returns a read-only snapshot of cart items.
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<OrderItem>(items));
    }
    // Returns whether no food has been selected.
    public boolean isEmpty() { return items.isEmpty(); }

    // Adds a food item, merging quantities for identical IDs.
    public void addItem(MenuItem menuItem, int quantity) {
        OrderItem newItem = new OrderItem(menuItem, quantity);
        for (int i = 0; i < items.size(); i++) {
            OrderItem existing = items.get(i);
            if (existing.getMenuItem().getItemId() == menuItem.getItemId()) {
                int updated = existing.getQuantity() + quantity;
                items.set(i, new OrderItem(existing.getMenuItem(), updated));
                return;
            }
        }
        items.add(newItem);
    }

    // Changes a cart line to a valid new quantity.
    public boolean changeQuantity(int itemId, int quantity) {
        if (quantity < 1 || quantity > 99)
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        for (int i = 0; i < items.size(); i++) {
            OrderItem existing = items.get(i);
            if (existing.getMenuItem().getItemId() == itemId) {
                items.set(i, new OrderItem(existing.getMenuItem(), quantity));
                return true;
            }
        }
        return false;
    }

    // Removes a food item by ID.
    public boolean removeItem(int itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getMenuItem().getItemId() == itemId) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }

    // Sums cart line totals without floating-point rounding errors.
    public BigDecimal calculateSubtotal() {
        BigDecimal subtotal = new BigDecimal("0.00");
        for (OrderItem item : items) subtotal = subtotal.add(item.getLineTotal());
        return subtotal;
    }
}
