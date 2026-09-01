package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.CatalogService;
import com.myfarmproduce.domain.entity.Category;
import com.myfarmproduce.domain.entity.Product;
import com.myfarmproduce.infrastructure.repository.CategoryRepository;
import com.myfarmproduce.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CatalogServiceImpl(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Override
    public List<Category> getCategoriesWithProducts(String search) {
        List<Category> allCategories = categories.findAllByOrderByNameAsc();

        List<Product> matched = StringUtils.hasText(search)
                ? products.findByNameContainingIgnoreCaseOrderByNameAsc(search.trim())
                : products.findAllByOrderByNameAsc();

        for (Category category : allCategories) {
            category.setProducts(matched.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(category.getId()))
                    .sorted(Comparator.comparing(Product::getName))
                    .toList());
        }

        if (!StringUtils.hasText(search)) return allCategories;

        return allCategories.stream().filter(c -> !c.getProducts().isEmpty()).toList();
    }

    @Override
    public Optional<Product> getProductById(Integer id) {
        return products.findById(id);
    }

    @Override
    public List<Product> getProductsByIds(Iterable<Integer> ids) {
        List<Integer> idList = new ArrayList<>();
        for (Integer id : ids)
            if (!idList.contains(id)) idList.add(id);
        return products.findByIdIn(idList);
    }
}
