package com.myfarmproduce.application.service;

import com.myfarmproduce.application.model.CartLineInput;
import com.myfarmproduce.application.model.PlaceOrderRequest;
import com.myfarmproduce.domain.entity.Order;
import com.myfarmproduce.domain.entity.Payment;
import com.myfarmproduce.domain.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(Integer customerId, PlaceOrderRequest request, List<CartLineInput> lines);

    Optional<Order> getOrder(Integer orderId, Integer restrictToCustomerId);

    Payment initiatePayment(Integer orderId, String provider, String reference);

    List<Order> getCustomerOrders(Integer customerId);

    List<Order> getOrders(OrderStatus status, Instant from, Instant to);

    void updateStatus(Integer orderId, OrderStatus status);

    Optional<Order> confirmPayment(String reference);

    void cancelOrder(Integer orderId);

    void refundOrder(Integer orderId, String note);
}
