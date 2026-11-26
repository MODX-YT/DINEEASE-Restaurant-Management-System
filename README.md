# DINEEASE TEST — Java 8 Console Project (V1.7)

**Approved project title:** DINEEASE: Modular Restaurant Table Reservation and Food Ordering Management System

A console-based Object-Oriented Programming (OOP 101) project for table reservation, restaurant menu display, ordering, cash checkout, and local transaction history.

## Requirements

- Java Development Kit (JDK) **8 or newer** (`java` and `javac`)
- VS Code (recommended), or Windows Command Prompt / PowerShell
- No third-party Java dependencies or database server

## Quick start on Windows

1. Extract the ZIP and open the **DINEEASE TEST** folder in VS Code.
2. Double-click `run.bat`, or use the terminal commands below.
3. Choose an option from the main menu.

### Windows CMD / PowerShell commands

Run from the `DINEEASE TEST` root directory:

```cmd
mkdir out
javac -encoding UTF-8 -d out src\dineease\*.java
java -cp out dineease.Main
```

If `out` already exists, skip `mkdir out`.

### Main menu

```text
[1] View Tables & Reservation
[2] Display Menu
[3] Create Order & Checkout
[4] Exit
```

## Main features

1. **Tables & Reservations:** view tables, edit physical occupancy, add tables, create two-hour reservations, prevent overlapping bookings, and cancel reservations.
2. **Food Menu:** 11 sample menu items with IDs, categories and PHP prices.
3. **Orders & Cart:** dine-in and takeout, add/change/remove cart items, quantities 1–99, automatic subtotal.
4. **Checkout:** cash validation, exact change using `BigDecimal`, dine-in and takeout polymorphic billing, itemized receipt.
5. **Transaction History:** from option 3, enter `/history` as customer name; view previous receipts by order ID. You can also select `[7] View Transaction History` within an active draft.
6. **Local storage:** tables, reservations, receipts, and unfinished draft orders load and save in `data/dineease.properties`. Saves occur when returning to the main menu from reservations/orders and on normal exit.

## Run all automated tests

Double-click `test.bat` or run these commands in a terminal after compiling:

```cmd
java -cp out dineease.DINEEASETest
java -Ddineease.testMode=true -cp out dineease.DINEEASEIntegrationTest
java -cp out dineease.DINEEASEPersistenceTest
```

Expected results (verified when the ZIP was built with `javac --release 8`):

| Suite | Passed | Failed |
| --- | ---: | ---: |
| OOP / core logic | 25 | 0 |
| Interactive integration | 13 | 0 |
| Persistence | 13 | 0 |
| **Total** | **51** | **0** |

**Test data safety:** Integration tests automatically enable test mode, so they do not read or write your real restaurant data. Persistence tests use a temporary data file and remove it when complete.

## Files

All Java source files are in **`src/dineease/`** (one folder, one `package dineease;`).

- `Main.java`: startup, main menu and automatic load/save
- `RestaurantTable.java`, `TableManager.java`: table model and operations
- `Reservation.java`, `ReservationManager.java`: reservation model and scheduling
- `MenuItem.java`, `MenuManager.java`: menu model and display
- `Order.java`, `OrderItem.java`, `OrderManager.java`: cart, order and checkout workflow
- `Billing.java`, `DineInBilling.java`, `TakeoutBilling.java`: abstract billing and overrides
- `ReceiptRecord.java`, `DataStore.java`: receipt history and persistent properties storage
- `DINEEASETest.java`, `DINEEASEIntegrationTest.java`, `DINEEASEPersistenceTest.java`: automated tests

## Object-oriented principles

- **Encapsulation:** `private` fields and public accessors/methods across the table, reservation, menu, and order classes.
- **Inheritance:** `DineInBilling` and `TakeoutBilling` extend `Billing`.
- **Polymorphism:** a `Billing` reference invokes overrides of `calculateTotal()`, `getBillingType()`, and `getBillingNote()` at runtime.
- **Abstraction:** `Billing` is an abstract base class with abstract methods and shared payment/receipt logic.

See `docs/OOP_OVERVIEW.md` for a compact UML-style overview.

## Key assumptions / limitations

- A reservation lasts **2 hours**; dates use `yyyy-MM-dd HH:mm` in 24-hour time.
- An open dine-in order checks a two-hour window from the current time against scheduled reservations. The reservation holder must supply a matching reservation ID and name when that table is booked.
- A table is marked **OCCUPIED** after successful dine-in checkout. Staff can mark it available through Table Management after it is vacated.
- There is one active draft order at a time; completing/cancelling it allows another.
- The system uses cash-only payments; no taxes, service charges or discounts are assumed.
- The food menu contains sample prices and is not editable through the console.
- Saved data is plaintext local properties data, **not encrypted**. Do not store sensitive real customer or card information. Local customer names are excluded from Git via `.gitignore`.
- `OFFICIAL RECEIPT` is a **simulated educational receipt** and not a legally registered tax invoice.
- Changes are saved at module return / normal exit, not after every action; a forced close can lose unsaved changes.
- There is no user authentication, multi-user concurrency, network database, or external reservation system.

## GitHub

Commit the `src/`, `docs/`, `README.md`, `.gitignore`, and Windows `.bat` files. Do **not** commit `data/`, `out/`, or real customer records.
