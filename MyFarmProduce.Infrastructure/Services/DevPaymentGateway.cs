using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Application.Models;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Infrastructure.Services;

/// <summary>
/// Simulated payment gateway for development. Generates a reference and redirects
/// to an in-app simulated checkout page. Swap for a real Paystack/Flutterwave
/// implementation (same interface) once API keys are configured.
/// </summary>
public class DevPaymentGateway : IPaymentGateway
{
    public const string ProviderName = "DevSimulated";

    public Task<PaymentInitResult> InitializeAsync(Order order, string callbackUrl)
    {
        var reference = $"DEV-{order.Id}-{Guid.NewGuid():N}"[..24];
        // The "redirect" is our own simulated gateway page carrying the reference.
        var redirectUrl = $"{callbackUrl}?reference={Uri.EscapeDataString(reference)}";
        return Task.FromResult(new PaymentInitResult(reference, redirectUrl));
    }

    public Task<PaymentVerificationResult> VerifyAsync(string reference)
    {
        // Dev gateway always verifies successfully. A real gateway would call its API.
        return Task.FromResult(new PaymentVerificationResult(true, reference, 0m));
    }
}
