package com.myfarmproduce.web.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartViewModel {
    private List<CartLineViewModel> lines = new ArrayList<>();
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    public List<CartLineViewModel> getLines() { return lines; }
    public void setLines(List<CartLineViewModel> lines) { this.lines = lines; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getSubtotal() {
        return lines.stream().map(CartLineViewModel::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal getTotal() { return getSubtotal().add(deliveryFee); }
    public boolean isEmpty() { return lines.isEmpty(); }
    public boolean isHasStockIssues() { return lines.stream().anyMatch(CartLineViewModel::isExceedsStock); }
}
