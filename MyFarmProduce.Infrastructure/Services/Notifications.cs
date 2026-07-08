using Microsoft.Extensions.Logging;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Infrastructure.Services;

/// <summary>Dev email sender — logs the message instead of sending. Swap for SMTP/SendGrid.</summary>
public class LoggingEmailSender : IEmailSender
{
    private readonly ILogger<LoggingEmailSender> _logger;
    public LoggingEmailSender(ILogger<LoggingEmailSender> logger) => _logger = logger;

    public Task SendAsync(string to, string subject, string htmlBody)
    {
        _logger.LogInformation("EMAIL -> {To} | {Subject}\n{Body}", to, subject, htmlBody);
        return Task.CompletedTask;
    }
}

/// <summary>No-op SMS sender (SMS is optional for MVP – add Termii later).</summary>
public class NoopSmsSender : ISmsSender
{
    private readonly ILogger<NoopSmsSender> _logger;
    public NoopSmsSender(ILogger<NoopSmsSender> logger) => _logger = logger;

    public Task SendAsync(string phone, string message)
    {
        _logger.LogInformation("SMS -> {Phone} | {Message}", phone, message);
        return Task.CompletedTask;
    }
}

public class NotificationService : INotificationService
{
    private readonly IEmailSender _email;
    private readonly ISmsSender _sms;

    public NotificationService(IEmailSender email, ISmsSender sms)
    {
        _email = email;
        _sms = sms;
    }

    private string? Email(Order o) => o.Customer?.Email;

    public async Task OrderPlacedAsync(Order order)
    {
        if (Email(order) is string to)
            await _email.SendAsync(to, $"Order #{order.Id} received",
                $"<p>We've received your order <strong>#{order.Id}</strong> totalling ₦{order.Total:N2}. Please complete payment.</p>");
    }

    public async Task PaymentConfirmedAsync(Order order)
    {
        if (Email(order) is string to)
            await _email.SendAsync(to, $"Payment confirmed for order #{order.Id}",
                $"<p>Your payment of ₦{order.Total:N2} for order <strong>#{order.Id}</strong> is confirmed. We're preparing it for delivery.</p>");
        await _sms.SendAsync(order.Phone, $"MyFarmProduce: payment confirmed for order #{order.Id}.");
    }

    public async Task OutForDeliveryAsync(Order order)
    {
        if (Email(order) is string to)
            await _email.SendAsync(to, $"Order #{order.Id} is out for delivery",
                $"<p>Your order <strong>#{order.Id}</strong> is on its way to {order.DeliveryAddress}.</p>");
        await _sms.SendAsync(order.Phone, $"MyFarmProduce: order #{order.Id} is out for delivery.");
    }

    public async Task DeliveredAsync(Order order)
    {
        if (Email(order) is string to)
            await _email.SendAsync(to, $"Order #{order.Id} delivered",
                $"<p>Your order <strong>#{order.Id}</strong> has been delivered. Enjoy!</p>");
    }
}
