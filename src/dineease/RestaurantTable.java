package dineease;

public class RestaurantTable {
    private final int tableId;
    private final int capacity;
    private boolean occupied;

    // Creates a table with a unique ID and positive capacity.
    public RestaurantTable(int tableId, int capacity) {
        if (tableId <= 0 || capacity <= 0) {
            throw new IllegalArgumentException("Table ID and capacity must be positive.");
        }
        this.tableId = tableId;
        this.capacity = capacity;
    }

    // Returns the table identifier.
    public int getTableId() { return tableId; }

    // Returns the seating capacity.
    public int getCapacity() { return capacity; }

    // Returns whether the table is occupied right now.
    public boolean isOccupied() { return occupied; }

    // Updates physical occupancy, independently of future reservations.
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    // Formats the occupancy state.
    public String getStatus() { return occupied ? "OCCUPIED" : "AVAILABLE"; }
}
