package com.myfarmproduce.infrastructure.service;

import com.myfarmproduce.application.model.PaymentInitResult;
import com.myfarmproduce.application.model.PaymentVerificationResult;
import com.myfarmproduce.application.service.PaymentGateway;
import com.myfarmproduce.domain.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Simulated payment gateway for development. Generates a reference and redirects
 * to an in-app simulated checkout page. Swap for a real Paystack/Flutterwave
 * implementation (same interface) once API keys are configured.
 */
@Component
public class DevPaymentGateway implements PaymentGateway {

    public static final String PROVIDER_NAME = "DevSimulated";

    @Override
    public PaymentInitResult initialize(Order order, String callbackUrl) {
        String raw = "DEV-" + order.getId() + "-" + UUID.randomUUID().toString().replace("-", "");
        String reference = raw.substring(0, Math.min(24, raw.length()));
        String redirectUrl = callbackUrl + "?reference=" + URLEncoder.encode(reference, StandardCharsets.UTF_8);
        return new PaymentInitResult(reference, redirectUrl);
    }

    @Override
    public PaymentVerificationResult verify(String reference) {
        // Dev gateway always verifies successfully. A real gateway would call its API.
        return new PaymentVerificationResult(true, reference, BigDecimal.ZERO);
    }
}
