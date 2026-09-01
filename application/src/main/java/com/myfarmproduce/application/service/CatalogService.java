package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.Category;
import com.myfarmproduce.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface CatalogService {
    List<Category> getCategoriesWithProducts(String search);

    Optional<Product> getProductById(Integer id);

    List<Product> getProductsByIds(Iterable<Integer> ids);
}
