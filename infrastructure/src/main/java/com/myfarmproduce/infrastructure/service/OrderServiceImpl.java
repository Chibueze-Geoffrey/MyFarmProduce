package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.model.CartLineInput;
import com.myfarmproduce.application.model.PlaceOrderRequest;
import com.myfarmproduce.application.service.OrderService;
import com.myfarmproduce.common.AppConstants;
import com.myfarmproduce.domain.entity.*;
import com.myfarmproduce.domain.enums.OrderStatus;
import com.myfarmproduce.domain.enums.PaymentStatus;
import com.myfarmproduce.infrastructure.repository.CustomerRepository;
import com.myfarmproduce.infrastructure.repository.OrderRepository;
import com.myfarmproduce.infrastructure.repository.PaymentRepository;
import com.myfarmproduce.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final ProductRepository products;
    private final CustomerRepository customers;

    public OrderServiceImpl(OrderRepository orders, PaymentRepository payments, ProductRepository products,
                             CustomerRepository customers) {
        this.orders = orders;
        this.payments = payments;
        this.products = products;
        this.customers = customers;
    }

    @Override
    public Order createOrder(Integer customerId, PlaceOrderRequest request, List<CartLineInput> lines) {
        if (lines.isEmpty())
            throw new IllegalStateException("Cannot place an order with an empty cart.");

        Map<Integer, Product> byId = new HashMap<>();
        for (Product p : products.findByIdIn(lines.stream().map(CartLineInput::productId).toList()))
            byId.put(p.getId(), p);

        Order order = new Order();
        order.setCustomer(customers.getReferenceById(customerId));
        order.setStatus(OrderStatus.Pending);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPhone(request.getPhone());
        order.setDeliveryNote(request.getDeliveryNote());
        order.setDeliveryFee(AppConstants.FLAT_DELIVERY_FEE);
        order.setCreatedAt(Instant.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartLineInput line : lines) {
            Product product = byId.get(line.productId());
            if (product == null)
                throw new IllegalStateException("Product " + line.productId() + " no longer exists.");
            if (line.quantity() <= 0) continue;
            if (!product.isAvailable() || product.getStockQty() < line.quantity())
                throw new IllegalStateException("'" + product.getName() + "' does not have " + line.quantity() + " in stock.");

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(line.quantity());
            item.setUnitPriceAtOrder(product.getPrice());
            order.getItems().add(item);

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }

        if (order.getItems().isEmpty())
            throw new IllegalStateException("Cannot place an order with an empty cart.");

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getDeliveryFee()));

        return orders.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrder(Integer orderId, Integer restrictToCustomerId) {
        return restrictToCustomerId == null
                ? orders.findById(orderId)
                : orders.findByIdAndCustomerId(orderId, restrictToCustomerId);
    }

    @Override
    public Payment initiatePayment(Integer orderId, String provider, String reference) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order " + orderId + " not found."));

        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.Success)
            return order.getPayment();

        if (order.getPayment() != null) {
            payments.delete(order.getPayment());
            order.setPayment(null);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setProvider(provider);
        payment.setReference(reference);
        payment.setStatus(PaymentStatus.Pending);
        payment.setAmount(order.getTotal());
        payments.save(payment);
        order.setPayment(payment);
        return payment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getCustomerOrders(Integer customerId) {
        return orders.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders(OrderStatus status, Instant from, Instant to) {
        Instant toExclusive = to == null ? null : to.plusSeconds(86_400);
        return orders.search(status, from, toExclusive);
    }

    @Override
    public void updateStatus(Integer orderId, OrderStatus status) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order " + orderId + " not found."));
        order.setStatus(status);
    }

    @Override
    public Optional<Order> confirmPayment(String reference) {
        Optional<Payment> paymentOpt = payments.findByReference(reference);
        if (paymentOpt.isEmpty() || paymentOpt.get().getOrder() == null) return Optional.empty();

        Payment payment = paymentOpt.get();
        Order order = payment.getOrder();

        if (payment.getStatus() == PaymentStatus.Success) return Optional.of(order);

        payment.setStatus(PaymentStatus.Success);
        payment.setPaidAt(Instant.now());
        order.setStatus(OrderStatus.PaymentConfirmed);

        for (OrderItem item : order.getItems())
            if (item.getProduct() != null) item.getProduct().reduceStock(item.getQuantity());

        return Optional.of(order);
    }

    @Override
    public void cancelOrder(Integer orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order " + orderId + " not found."));

        if (isStockDecremented(order.getStatus()))
            for (OrderItem item : order.getItems())
                if (item.getProduct() != null) item.getProduct().restock(item.getQuantity());

        order.setStatus(OrderStatus.Cancelled);
    }

    @Override
    public void refundOrder(Integer orderId, String note) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order " + orderId + " not found."));

        if (order.getPayment() != null) {
            order.getPayment().setStatus(PaymentStatus.Refunded);
            order.getPayment().setRefundedAt(Instant.now());
            order.getPayment().setAdminNote(note);
        }

        if (isStockDecremented(order.getStatus()))
            for (OrderItem item : order.getItems())
                if (item.getProduct() != null) item.getProduct().restock(item.getQuantity());

        order.setStatus(OrderStatus.Cancelled);
    }

    private static boolean isStockDecremented(OrderStatus status) {
        return status == OrderStatus.PaymentConfirmed
                || status == OrderStatus.Preparing
                || status == OrderStatus.OutForDelivery;
    }
}
