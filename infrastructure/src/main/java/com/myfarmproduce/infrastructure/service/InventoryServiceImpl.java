package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.InventoryService;
import com.myfarmproduce.domain.entity.Category;
import com.myfarmproduce.domain.entity.Product;
import com.myfarmproduce.domain.enums.ProductUnit;
import com.myfarmproduce.infrastructure.repository.CategoryRepository;
import com.myfarmproduce.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository products;
    private final CategoryRepository categories;

    public InventoryServiceImpl(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAll() {
        return products.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(Integer id) {
        return products.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categories.findAllByOrderByNameAsc();
    }

    @Override
    public Product create(String name, String description, Integer categoryId, ProductUnit unit,
                           BigDecimal price, int stock, boolean isAvailable, String imageUrl) {
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category " + categoryId + " not found."));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setUnit(unit);
        product.setPrice(price);
        product.setAvailable(isAvailable);
        product.setImageUrl(imageUrl);
        product.setInitialStock(stock);

        return products.save(product);
    }

    @Override
    public void update(Integer id, String name, String description, Integer categoryId, ProductUnit unit,
                        BigDecimal price, boolean isAvailable, String imageUrl) {
        Product product = products.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product " + id + " not found."));
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category " + categoryId + " not found."));

        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setUnit(unit);
        product.setPrice(price);
        product.setAvailable(isAvailable);
        product.setImageUrl(imageUrl);
    }

    @Override
    public void restock(Integer id, int quantity) {
        Product product = products.findById(id)
                .orElseThrow(() -> new IllegalStateException("Product " + id + " not found."));
        product.restock(quantity);
    }

    @Override
    public void delete(Integer id) {
        products.findById(id).ifPresent(products::delete);
    }
}
