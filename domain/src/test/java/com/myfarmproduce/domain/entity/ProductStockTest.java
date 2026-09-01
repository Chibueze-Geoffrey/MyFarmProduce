package com.myfarmproduce.domain.entity;

import com.myfarmproduce.domain.enums.ProductUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductStockTest {

    private static Product newProduct(int stock) {
        Product p = new Product();
        p.setName("Test");
        p.setUnit(ProductUnit.Kg);
        p.setPrice(new BigDecimal("100"));
        p.setInitialStock(stock);
        return p;
    }

    @Test
    void reduceStock_decrementsStock() {
        Product p = newProduct(10);
        p.reduceStock(4);
        assertThat(p.getStockQty()).isEqualTo(6);
    }

    @Test
    void reduceStock_exactStock_goesToZero() {
        Product p = newProduct(5);
        p.reduceStock(5);
        assertThat(p.getStockQty()).isZero();
    }

    @Test
    void reduceStock_moreThanAvailable_throws() {
        Product p = newProduct(3);
        assertThatThrownBy(() -> p.reduceStock(4))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
        assertThat(p.getStockQty()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void reduceStock_nonPositive_throws(int qty) {
        Product p = newProduct(10);
        assertThatThrownBy(() -> p.reduceStock(qty)).isInstanceOf(IllegalArgumentException.class);
        assertThat(p.getStockQty()).isEqualTo(10);
    }

    @Test
    void restock_incrementsStock() {
        Product p = newProduct(10);
        p.restock(5);
        assertThat(p.getStockQty()).isEqualTo(15);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    void restock_nonPositive_throws(int qty) {
        Product p = newProduct(10);
        assertThatThrownBy(() -> p.restock(qty)).isInstanceOf(IllegalArgumentException.class);
        assertThat(p.getStockQty()).isEqualTo(10);
    }
}
