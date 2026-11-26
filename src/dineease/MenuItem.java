package dineease;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MenuItem {
    private final int itemId;
    private final String itemName;
    private final String category;
    private final BigDecimal price;

    // Creates a menu item with validated attributes.
    public MenuItem(int itemId, String itemName, String category, BigDecimal price) {
        if (itemId <= 0) throw new IllegalArgumentException("Item ID must be positive.");
        if (itemName == null || itemName.trim().isEmpty())
            throw new IllegalArgumentException("Item name cannot be empty.");
        if (category == null || category.trim().isEmpty())
            throw new IllegalArgumentException("Category cannot be empty.");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Price must be greater than zero.");
        this.itemId = itemId;
        this.itemName = itemName.trim();
        this.category = category.trim();
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    // Returns item identifier.
    public int getItemId() { return itemId; }
    // Returns food name.
    public String getItemName() { return itemName; }
    // Returns category.
    public String getCategory() { return category; }
    // Returns exact menu price.
    public BigDecimal getPrice() { return price; }
    // Returns formatted Philippine peso price.
    public String getFormattedPrice() { return "PHP " + price.toPlainString(); }
}
