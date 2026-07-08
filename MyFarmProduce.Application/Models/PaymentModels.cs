namespace MyFarmProduce.Application.Models;

/// <summary>Result of asking the gateway to initialize a transaction.</summary>
public record PaymentInitResult(string Reference, string RedirectUrl);

/// <summary>Result of verifying a transaction with the gateway.</summary>
public record PaymentVerificationResult(bool Success, string Reference, decimal Amount);
