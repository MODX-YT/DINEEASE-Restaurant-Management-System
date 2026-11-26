package dineease;

import java.math.BigDecimal;

public class DineInBilling extends Billing {
    // Enforces dine-in order type.
    public DineInBilling(Order order) {
        super(order);
        if (order.getOrderType() != Order.OrderType.DINE_IN)
            throw new IllegalArgumentException("Order must be DINE_IN.");
    }
    // Calculates dine-in total without extra fees.
    @Override public BigDecimal calculateTotal() { return order.calculateSubtotal(); }
    // Identifies this billing subtype.
    @Override public String getBillingType() { return "DINE-IN"; }
    // Describes the dine-in table on the receipt.
    @Override public String getBillingNote() {
        return "Service: Dine-In | Table " + order.getTableId();
    }
}
