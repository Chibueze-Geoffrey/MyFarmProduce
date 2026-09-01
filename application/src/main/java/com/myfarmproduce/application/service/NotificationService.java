package com.myfarmproduce.application.service;

import com.myfarmproduce.domain.entity.Order;

public interface NotificationService {
    void orderPlaced(Order order);

    void paymentConfirmed(Order order);

    void outForDelivery(Order order);

    void delivered(Order order);
}
