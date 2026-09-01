package com.myfarmproduce.infrastructure.repository;

import com.myfarmproduce.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findAllByOrderByNameAsc();

    List<Product> findByNameContainingIgnoreCaseOrderByNameAsc(String term);

    List<Product> findByIdIn(List<Integer> ids);

    List<Product> findAllByAvailableTrueAndStockQtyGreaterThanOrderByNameAsc(int minExclusive);
}
