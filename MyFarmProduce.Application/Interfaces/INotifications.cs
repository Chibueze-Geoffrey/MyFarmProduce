using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface IEmailSender
{
    Task SendAsync(string to, string subject, string htmlBody);
}

public interface ISmsSender
{
    Task SendAsync(string phone, string message);
}

/// <summary>High-level order notifications (email required, SMS best-effort) per User Story 11.</summary>
public interface INotificationService
{
    Task OrderPlacedAsync(Order order);
    Task PaymentConfirmedAsync(Order order);
    Task OutForDeliveryAsync(Order order);
    Task DeliveredAsync(Order order);
}
