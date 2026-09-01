package com.myfarmproduce.web.dto;

import com.myfarmproduce.domain.enums.ProductUnit;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductForm {

    private Integer id;

    @NotBlank @Size(max = 150)
    private String name = "";

    @Size(max = 1000)
    private String description = "";

    private Integer categoryId;

    private ProductUnit unit;

    @NotNull @DecimalMin("0")
    private BigDecimal price;

    @Min(0)
    private int stockQty;

    private boolean available = true;

    @Size(max = 500)
    private String imageUrl;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public ProductUnit getUnit() { return unit; }
    public void setUnit(ProductUnit unit) { this.unit = unit; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
