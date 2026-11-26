# DINEEASE OOP / Module Overview

```text
Main
 |-- TableManager ------ RestaurantTable
 |-- ReservationManager - Reservation ---> tableId
 |-- MenuManager ------- MenuItem
 |-- OrderManager ------ Order ------- OrderItem ---> MenuItem
 |                         | 
 |                         `--> Billing (abstract)
 |                                |-- DineInBilling
 |                                `-- TakeoutBilling
 |-- DataStore --------- Properties file
                            `-- ReceiptRecord (saved receipt text)
```

## OOP requirements

| Concept | Example |
| --- | --- |
| Classes / objects | `new RestaurantTable(1, 2)`, `new Order(...)` |
| Constructors | Table, reservation, menu, and order constructors validate data |
| Encapsulation | Private fields and controlled methods (`getCapacity`, `setOccupied`, `addItem`) |
| Inheritance | `DineInBilling extends Billing`; `TakeoutBilling extends Billing` |
| Polymorphism | `Billing billing = ...; billing.calculateTotal()` dispatches to subclass |
| Abstraction | Abstract `Billing` defines the contract for totals, type, and receipt notes |
| Data handling | `ArrayList` for tables, bookings, menu and cart; `BigDecimal` for money |
| Exception handling | Invalid numeric inputs, bad dates, invalid quantities and insufficient cash |

## Core business rules

- Table IDs and menu IDs must be unique.
- Reservations cannot overlap on the same table (two-hour slots).
- A dine-in order uses an available and unoccupied table.
- If a table has a booking, the matching reservation ID and customer name are required during the booking interval.
- A receipt is recorded only after cash tendered is sufficient.
- Successful dine-in checkout marks its table occupied.
- `DataStore` loads state at launch and saves it on returning from a mutating module or exiting.

## Future improvements

- Persist immediately after every successful change.
- Allow multiple simultaneous draft orders and staff logins.
- Add a proper reservation check-in/check-out lifecycle and dine-in reservation-to-order association.
- Add editable menu, reports, and database support if required by the instructor.
