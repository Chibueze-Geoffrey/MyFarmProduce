package com.myfarmproduce.application.model;

/** Result of asking the gateway to initialize a transaction. */
public record PaymentInitResult(String reference, String redirectUrl) {
}
