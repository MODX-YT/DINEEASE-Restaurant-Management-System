package dineease;

import java.time.LocalDateTime;

public class Reservation {
    private final int reservationId;
    private final String customerName;
    private final int tableId;
    private final int guests;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private boolean cancelled;

    // Creates a two-hour table reservation.
    public Reservation(int reservationId, String customerName,
                       int tableId, int guests, LocalDateTime startDateTime) {
        if (reservationId <= 0 || customerName == null || customerName.trim().isEmpty()
                || tableId <= 0 || guests <= 0 || startDateTime == null) {
            throw new IllegalArgumentException("Invalid reservation details.");
        }
        this.reservationId = reservationId;
        this.customerName = customerName.trim();
        this.tableId = tableId;
        this.guests = guests;
        this.startDateTime = startDateTime;
        this.endDateTime = startDateTime.plusHours(2);
    }

    // Returns the reservation ID.
    public int getReservationId() { return reservationId; }
    // Returns the customer name.
    public String getCustomerName() { return customerName; }
    // Returns the selected table.
    public int getTableId() { return tableId; }
    // Returns the party size.
    public int getGuests() { return guests; }
    // Returns booking start.
    public LocalDateTime getStartDateTime() { return startDateTime; }
    // Returns booking end.
    public LocalDateTime getEndDateTime() { return endDateTime; }
    // Returns cancellation state.
    public boolean isCancelled() { return cancelled; }
    // Cancels a booking without deleting its record.
    public void cancel() { cancelled = true; }

    // Detects an overlapping interval on the same table.
    public boolean conflictsWith(int requestedTableId, LocalDateTime newStart,
                                 LocalDateTime newEnd) {
        return !cancelled && tableId == requestedTableId
                && newStart.isBefore(endDateTime)
                && newEnd.isAfter(startDateTime);
    }
}
