package dineease;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Scanner;

public class MenuManager {
    private final ArrayList<MenuItem> menuItems = new ArrayList<>();

    // Initializes the sample food menu.
    public MenuManager() { loadDefaultMenu(); }

    // Loads 11 food, side dish, drink, and dessert options.
    private void loadDefaultMenu() {
        addItem(new MenuItem(101, "Chicken Adobo", "Main Dish", new BigDecimal("180.00")));
        addItem(new MenuItem(102, "Pork Sisig", "Main Dish", new BigDecimal("240.00")));
        addItem(new MenuItem(103, "Beef Tapa", "Main Dish", new BigDecimal("220.00")));
        addItem(new MenuItem(104, "Pancit Canton", "Main Dish", new BigDecimal("160.00")));
        addItem(new MenuItem(201, "Garlic Rice", "Side Dish", new BigDecimal("45.00")));
        addItem(new MenuItem(202, "French Fries", "Side Dish", new BigDecimal("85.00")));
        addItem(new MenuItem(301, "Iced Tea", "Beverage", new BigDecimal("55.00")));
        addItem(new MenuItem(302, "Bottled Water", "Beverage", new BigDecimal("30.00")));
        addItem(new MenuItem(303, "Mango Shake", "Beverage", new BigDecimal("95.00")));
        addItem(new MenuItem(401, "Leche Flan", "Dessert", new BigDecimal("80.00")));
        addItem(new MenuItem(402, "Halo-Halo", "Dessert", new BigDecimal("110.00")));
    }

    // Adds a menu item only when its ID is unique.
    public void addItem(MenuItem item) {
        if (item == null) throw new IllegalArgumentException("Menu item cannot be null.");
        if (getItemById(item.getItemId()) != null)
            throw new IllegalArgumentException("Menu item ID already exists: " + item.getItemId());
        menuItems.add(item);
    }

    // Finds a menu item by identifier.
    public MenuItem getItemById(int itemId) {
        for (MenuItem item : menuItems) if (item.getItemId() == itemId) return item;
        return null;
    }

    // Returns a shallow copy of the food list.
    public ArrayList<MenuItem> getAllItems() { return new ArrayList<>(menuItems); }

    // Prints all food names, IDs, categories, and prices.
    public void displayMenu() {
        System.out.println("\n==================================================================");
        System.out.println("                        DINEEASE FOOD MENU");
        System.out.println("==================================================================");
        System.out.printf("%-8s %-24s %-17s %12s%n", "ITEM ID", "FOOD NAME", "CATEGORY", "PRICE");
        System.out.println("------------------------------------------------------------------");
        if (menuItems.isEmpty()) System.out.println("No food items are currently available.");
        for (MenuItem item : menuItems) {
            System.out.printf("%-8d %-24s %-17s %12s%n", item.getItemId(),
                    item.getItemName(), item.getCategory(), item.getFormattedPrice());
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("Total Menu Items: " + menuItems.size());
        System.out.println("==================================================================");
    }

    // Opens the display-only menu.
    public void open(Scanner scanner) {
        displayMenu();
        System.out.println("\nPress Enter to return to Main Menu...");
        scanner.nextLine();
    }
}
