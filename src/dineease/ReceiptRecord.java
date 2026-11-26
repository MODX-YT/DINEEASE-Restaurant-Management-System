package dineease;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReceiptRecord {
    private final int orderId;
    private final String customerName;
    private final LocalDateTime paidAt;
    private final BigDecimal total;
    private final String receiptText;

    // Preserves a completed payment and its original receipt text.
    public ReceiptRecord(int orderId, String customerName, LocalDateTime paidAt,
                         BigDecimal total, String receiptText) {
        if (orderId <= 0 || customerName == null || customerName.trim().isEmpty()
                || paidAt == null || total == null || total.signum() < 0
                || receiptText == null)
            throw new IllegalArgumentException("Invalid receipt record.");
        this.orderId = orderId;
        this.customerName = customerName;
        this.paidAt = paidAt;
        this.total = total;
        this.receiptText = receiptText;
    }
    // Returns ID.
    public int getOrderId() { return orderId; }
    // Returns customer.
    public String getCustomerName() { return customerName; }
    // Returns time paid.
    public LocalDateTime getPaidAt() { return paidAt; }
    // Returns billed total.
    public BigDecimal getTotal() { return total; }
    // Returns original receipt.
    public String getReceiptText() { return receiptText; }
}
