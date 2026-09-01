package com.myfarmproduce.web.service;

import com.myfarmproduce.application.service.CatalogService;
import com.myfarmproduce.common.AppConstants;
import com.myfarmproduce.domain.entity.Product;
import com.myfarmproduce.web.dto.CartLineViewModel;
import com.myfarmproduce.web.dto.CartViewModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;

/** Builds a display cart by joining session quantities with current product data. */
@Component
public class CartFactory {

    private final CartService cart;
    private final CatalogService catalog;

    public CartFactory(CartService cart, CatalogService catalog) {
        this.cart = cart;
        this.catalog = catalog;
    }

    public CartViewModel build() {
        Map<Integer, Integer> items = cart.getItems();
        CartViewModel vm = new CartViewModel();

        if (!items.isEmpty()) {
            for (Product product : catalog.getProductsByIds(items.keySet())) {
                CartLineViewModel line = new CartLineViewModel();
                line.setProduct(product);
                line.setQuantity(items.get(product.getId()));
                vm.getLines().add(line);
            }
            vm.setLines(vm.getLines().stream()
                    .sorted(Comparator.comparing(l -> l.getProduct().getName()))
                    .toList());
        }

        vm.setDeliveryFee(vm.isEmpty() ? BigDecimal.ZERO : AppConstants.FLAT_DELIVERY_FEE);
        return vm;
    }
}
