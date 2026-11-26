package dineease;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class Billing {
    protected final Order order;

    // Initializes billing for a nonempty order.
    public Billing(Order order) {
        if (order == null || order.isEmpty())
            throw new IllegalArgumentException("Billing requires a non-empty order.");
        this.order = order;
    }

    // Calculates the subclass-specific total.
    public abstract BigDecimal calculateTotal();
    // Identifies the subclass billing type.
    public abstract String getBillingType();
    // Provides the subclass's receipt note.
    public abstract String getBillingNote();

    // Validates cash tendered and computes customer change.
    public BigDecimal calculateChange(BigDecimal cash) {
        if (cash == null || cash.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Invalid cash amount.");
        BigDecimal total = calculateTotal();
        if (cash.compareTo(total) < 0)
            throw new IllegalArgumentException("Insufficient payment.");
        return cash.subtract(total);
    }

    // Builds an itemized receipt with its original payment timestamp.
    public String buildReceipt(BigDecimal cash, LocalDateTime paidAt) {
        if (paidAt == null) throw new IllegalArgumentException("Payment timestamp is required.");
        BigDecimal change = calculateChange(cash);
        StringBuilder text = new StringBuilder();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        text.append("\n============================================================\n");
        text.append("                         DINEEASE\n");
        text.append("                    OFFICIAL RECEIPT\n");
        text.append("============================================================\n");
        text.append("Order ID : ").append(order.getOrderId()).append("\n");
        text.append("Customer : ").append(order.getCustomerName()).append("\n");
        text.append("Type     : ").append(getBillingType()).append("\n");
        if (order.getTableId() != null)
            text.append("Table    : ").append(order.getTableId()).append("\n");
        text.append("Date     : ").append(paidAt.format(format)).append("\n");
        text.append("------------------------------------------------------------\n");
        text.append(String.format(Locale.US, "%-6s %-20s %5s %10s %10s%n",
                "ID", "ITEM", "QTY", "PRICE", "TOTAL"));
        text.append("------------------------------------------------------------\n");
        for (OrderItem item : order.getItems()) {
            text.append(String.format(Locale.US, "%-6d %-20s %5d %10s %10s%n",
                    item.getMenuItem().getItemId(), item.getMenuItem().getItemName(),
                    item.getQuantity(), item.getMenuItem().getPrice().toPlainString(),
                    item.getLineTotal().toPlainString()));
        }
        text.append("------------------------------------------------------------\n");
        text.append("Subtotal : PHP ").append(order.calculateSubtotal().toPlainString()).append("\n");
        text.append("TOTAL    : PHP ").append(calculateTotal().toPlainString()).append("\n");
        text.append("CASH     : PHP ").append(cash.toPlainString()).append("\n");
        text.append("CHANGE   : PHP ").append(change.toPlainString()).append("\n");
        text.append("------------------------------------------------------------\n");
        text.append(getBillingNote()).append("\n");
        text.append("                  PAYMENT SUCCESSFUL\n");
        text.append("              THANK YOU FOR DINING WITH US!\n");
        text.append("============================================================\n");
        return text.toString();
    }

    // Prints a receipt at the current time.
    public void printReceipt(BigDecimal cash) {
        System.out.print(buildReceipt(cash, LocalDateTime.now()));
    }
}
