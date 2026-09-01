package com.myfarmproduce.application.model;

import java.math.BigDecimal;

/** Result of verifying a transaction with the gateway. */
public record PaymentVerificationResult(boolean success, String reference, BigDecimal amount) {
}
