package com.myfarmproduce.web.dto;

import com.myfarmproduce.domain.entity.Product;

import java.math.BigDecimal;

public class CartLineViewModel {
    private Product product;
    private int quantity;

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() { return product.getPrice().multiply(BigDecimal.valueOf(quantity)); }
    public boolean isExceedsStock() { return quantity > product.getStockQty() || !product.isAvailable(); }
}
