package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.SupportAssistant;
import com.myfarmproduce.common.AppConstants;
import com.myfarmproduce.domain.entity.Order;
import com.myfarmproduce.domain.entity.Product;
import com.myfarmproduce.domain.entity.SupportMessage;
import com.myfarmproduce.infrastructure.repository.OrderRepository;
import com.myfarmproduce.infrastructure.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Free, keyless customer-service assistant. Answers common questions using live
 * catalog and order data via keyword matching. Implements {@link SupportAssistant}
 * so a real LLM (Claude, etc.) can be dropped in later without touching callers.
 */
@Service
@Transactional(readOnly = true)
public class RuleBasedSupportAssistant implements SupportAssistant {

    private final OrderRepository orders;
    private final ProductRepository products;
    private static final NumberFormat MONEY = NumberFormat.getIntegerInstance(Locale.US);

    public RuleBasedSupportAssistant(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    @Override
    public String getReply(Integer customerId, String latestMessage, List<SupportMessage> history) {
        String text = latestMessage.toLowerCase();

        if (has(text, "hello", "hi ", "hey", "good morning", "good afternoon") && text.length() < 25)
            return "Hi there! 👋 I'm the MyFarmProduce assistant. I can help with orders, delivery, payment, and what's in stock. What do you need?";

        if (has(text, "my order", "order status", "status of", "track", "where is my")) {
            var order = orders.findFirstByCustomerIdOrderByCreatedAtDesc(customerId);
            if (order.isEmpty())
                return "I couldn't find any orders on your account yet. Once you place and pay for an order, you can track it under \"My Orders\".";

            Order o = order.get();
            return switch (o.getStatus()) {
                case Pending -> "Your most recent order #" + o.getId() + " is still Pending payment. Complete payment from \"My Orders\" to get it moving.";
                case PaymentConfirmed -> "Order #" + o.getId() + " is paid and being prepared. Delivery is same/next day - we'll contact you on " + o.getPhone() + " to confirm timing.";
                case Preparing -> "Order #" + o.getId() + " is being prepared for delivery right now.";
                case OutForDelivery -> "Good news - order #" + o.getId() + " is out for delivery to " + o.getDeliveryAddress() + ".";
                case Delivered -> "Order #" + o.getId() + " was delivered. Enjoy! You can reorder the same items from \"My Orders\".";
                case Cancelled -> "Order #" + o.getId() + " was cancelled. If you didn't expect this, let me know and I'll flag it for an admin.";
            };
        }

        if (has(text, "pay", "payment", "card", "transfer", "ussd", "checkout fail", "money"))
            return "We accept card, bank transfer, and USSD. Payment is taken at checkout; if a payment fails, the order stays Pending and you can retry it from \"My Orders\". Delivery fee is a flat ₦"
                    + MONEY.format(AppConstants.FLAT_DELIVERY_FEE) + ".";

        if (has(text, "deliver", "shipping", "when will", "how long", "fee", "address"))
            return "Delivery is same or next day and we confirm timing by phone. There's a flat delivery fee of ₦"
                    + MONEY.format(AppConstants.FLAT_DELIVERY_FEE) + " added at checkout. You can set your delivery address and note during checkout.";

        if (has(text, "refund", "cancel", "return", "wrong item", "money back"))
            return "For a refund or cancellation, reply here with your order number and reason. I'll log it and an admin will process the refund on the payment gateway and update your order.";

        if (has(text, "stock", "available", "have you got", "do you sell", "in stock", "price of", "how much")) {
            List<Product> sample = products.findAllByAvailableTrueAndStockQtyGreaterThanOrderByNameAsc(0)
                    .stream().limit(5).toList();
            String list = sample.isEmpty() ? "our fresh produce range" : sample.stream()
                    .map(p -> p.getName() + " (₦" + MONEY.format(p.getPrice()) + "/" + p.getUnit() + ")")
                    .collect(Collectors.joining(", "));
            return "You can browse everything in stock on the Shop page. A few in-stock items right now: " + list + ". Search by name there to check a specific product.";
        }

        if (has(text, "how do i order", "how to order", "place an order", "buy"))
            return "Ordering is easy: browse the Shop, add items to your cart, go to Checkout, enter your delivery details, and pay online. You'll get an order confirmation and can track progress under \"My Orders\".";

        if (has(text, "change my", "update my", "phone number", "email address", "profile", "password"))
            return "You can edit your name and photo on your Profile page. Phone and email are locked for security - request a change from your Profile and an admin will apply it.";

        if (has(text, "human", "agent", "speak to", "manager", "admin", "real person"))
            return "No problem - I've logged this conversation, and an admin can review it and follow up. Meanwhile, tell me the details and I'll do my best to help right away.";

        if (has(text, "thank", "thanks", "cheers", "appreciate"))
            return "You're welcome! 🌽 Anything else I can help with?";

        return "I can help with order status, delivery, payment, refunds, and product availability. Could you tell me a bit more - for example your order number, or the product you're asking about?";
    }

    private static boolean has(String text, String... words) {
        for (String w : words)
            if (text.contains(w)) return true;
        return false;
    }
}
