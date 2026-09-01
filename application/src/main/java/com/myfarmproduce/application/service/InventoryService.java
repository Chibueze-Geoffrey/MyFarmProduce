package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.Category;
import com.myfarmproduce.domain.entity.Product;
import com.myfarmproduce.domain.enums.ProductUnit;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService {
    List<Product> getAll();

    Product getById(Integer id);

    List<Category> getCategories();

    Product create(String name, String description, Integer categoryId, ProductUnit unit,
                    BigDecimal price, int stock, boolean isAvailable, String imageUrl);

    void update(Integer id, String name, String description, Integer categoryId, ProductUnit unit,
                BigDecimal price, boolean isAvailable, String imageUrl);

    void restock(Integer id, int quantity);

    void delete(Integer id);
}
