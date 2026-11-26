package dineease;

import java.math.BigDecimal;

public class OrderItem {
    private final MenuItem menuItem;
    private final int quantity;

    // Represents one food item and its quantity in an order.
    public OrderItem(MenuItem menuItem, int quantity) {
        if (menuItem == null) throw new IllegalArgumentException("Food item cannot be null.");
        if (quantity < 1 || quantity > 99)
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    // Returns food details.
    public MenuItem getMenuItem() { return menuItem; }
    // Returns item count.
    public int getQuantity() { return quantity; }
    // Returns quantity multiplied by price.
    public BigDecimal getLineTotal() {
        return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
