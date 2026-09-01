package com.myfarmproduce.application.service;

import com.myfarmproduce.application.model.PaymentInitResult;
import com.myfarmproduce.application.model.PaymentVerificationResult;
import com.myfarmproduce.domain.entity.Order;

public interface PaymentGateway {
    PaymentInitResult initialize(Order order, String callbackUrl);

    PaymentVerificationResult verify(String reference);
}
