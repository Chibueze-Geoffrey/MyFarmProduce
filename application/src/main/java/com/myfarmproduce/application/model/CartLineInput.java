package com.myfarmproduce.application.model;

/** A single line submitted from the cart when placing an order. */
public record CartLineInput(Integer productId, int quantity) {
}
