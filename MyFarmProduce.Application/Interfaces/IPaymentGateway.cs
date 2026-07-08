using MyFarmProduce.Application.Models;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

/// <summary>
/// Abstraction over a payment provider (Paystack/Flutterwave in production).
/// A simulated dev implementation is used until real gateway keys are configured.
/// </summary>
public interface IPaymentGateway
{
    Task<PaymentInitResult> InitializeAsync(Order order, string callbackUrl);

    Task<PaymentVerificationResult> VerifyAsync(string reference);
}
