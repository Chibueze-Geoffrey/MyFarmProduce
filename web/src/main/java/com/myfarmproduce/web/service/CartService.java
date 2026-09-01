package com.myfarmproduce.web.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.LinkedHashMap;
import java.util.Map;

/** Session-backed cart: maps productId -> quantity. Persists across navigation. */
@Component
@SessionScope
public class CartService {

    private final Map<Integer, Integer> items = new LinkedHashMap<>();

    public Map<Integer, Integer> getItems() {
        return Map.copyOf(items);
    }

    public void setQuantity(Integer productId, int quantity) {
        if (quantity <= 0) items.remove(productId);
        else items.put(productId, quantity);
    }

    public void add(Integer productId, int quantity) {
        if (quantity <= 0) return;
        items.merge(productId, quantity, Integer::sum);
    }

    public void remove(Integer productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public int totalItemCount() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }
}
