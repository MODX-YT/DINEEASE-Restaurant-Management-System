package dineease;

import java.math.BigDecimal;

public class TakeoutBilling extends Billing {
    // Enforces takeout order type.
    public TakeoutBilling(Order order) {
        super(order);
        if (order.getOrderType() != Order.OrderType.TAKEOUT)
            throw new IllegalArgumentException("Order must be TAKEOUT.");
    }
    // Calculates takeout total without extra fees.
    @Override public BigDecimal calculateTotal() { return order.calculateSubtotal(); }
    // Identifies this billing subtype.
    @Override public String getBillingType() { return "TAKEOUT"; }
    // Describes takeout service on the receipt.
    @Override public String getBillingNote() { return "Service: Takeout | No table required"; }
}
