package com.myfarmproduce.domain.entity;

import com.myfarmproduce.domain.enums.ProductUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Column(nullable = false, length = 150)
    private String name = "";

    @Setter
    @Column(length = 1000)
    private String description = "";

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductUnit unit;

    @Setter
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    // Intentionally no public setter: the only ways to change stock are
    // reduceStock/restock/setInitialStock, which keep the value consistent.
    @Column(name = "stock_qty", nullable = false)
    private int stockQty;

    @Setter
    @Column(name = "is_available", nullable = false)
    private boolean available;

    @Setter
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * Decrease available stock by {@code quantity}.
     * Throws if the quantity is not positive or exceeds current stock.
     */
    public void reduceStock(int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        if (quantity > stockQty)
            throw new IllegalStateException(
                    "Insufficient stock for '" + name + "'. Available: " + stockQty + ", requested: " + quantity + ".");
        stockQty -= quantity;
    }

    /** Increase available stock by {@code quantity}. Throws if the quantity is not positive. */
    public void restock(int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        stockQty += quantity;
    }

    /** Sets the initial stock level. Intended for entity creation / seeding only. */
    public void setInitialStock(int quantity) {
        if (quantity < 0)
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        stockQty = quantity;
    }
}
