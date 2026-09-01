package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.service.EmailSender;
import com.myfarmproduce.application.service.NotificationService;
import com.myfarmproduce.application.service.SmsSender;
import com.myfarmproduce.domain.entity.Order;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final EmailSender email;
    private final SmsSender sms;

    public NotificationServiceImpl(EmailSender email, SmsSender sms) {
        this.email = email;
        this.sms = sms;
    }

    private static String email(Order o) {
        return o.getCustomer() != null ? o.getCustomer().getEmail() : null;
    }

    @Override
    public void orderPlaced(Order order) {
        String to = email(order);
        if (to != null)
            email.send(to, "Order #" + order.getId() + " received",
                    "<p>We've received your order <strong>#" + order.getId() + "</strong> totalling ₦"
                            + order.getTotal() + ". Please complete payment.</p>");
    }

    @Override
    public void paymentConfirmed(Order order) {
        String to = email(order);
        if (to != null)
            email.send(to, "Payment confirmed for order #" + order.getId(),
                    "<p>Your payment of ₦" + order.getTotal() + " for order <strong>#" + order.getId()
                            + "</strong> is confirmed. We're preparing it for delivery.</p>");
        sms.send(order.getPhone(), "MyFarmProduce: payment confirmed for order #" + order.getId() + ".");
    }

    @Override
    public void outForDelivery(Order order) {
        String to = email(order);
        if (to != null)
            email.send(to, "Order #" + order.getId() + " is out for delivery",
                    "<p>Your order <strong>#" + order.getId() + "</strong> is on its way to " + order.getDeliveryAddress() + ".</p>");
        sms.send(order.getPhone(), "MyFarmProduce: order #" + order.getId() + " is out for delivery.");
    }

    @Override
    public void delivered(Order order) {
        String to = email(order);
        if (to != null)
            email.send(to, "Order #" + order.getId() + " delivered",
                    "<p>Your order <strong>#" + order.getId() + "</strong> has been delivered. Enjoy!</p>");
    }
}
