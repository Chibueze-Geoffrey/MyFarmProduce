using MyFarmProduce.Common.Enums;
using MyFarmProduce.Application.Models;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface IOrderService
{
    /// <summary>Creates a Pending order from the given cart lines, pricing them at current product prices.</summary>
    Task<Order> CreateOrderAsync(int customerId, PlaceOrderRequest request, IReadOnlyList<CartLineInput> lines);

    Task<Order?> GetOrderAsync(int orderId, int? restrictToCustomerId = null);

    /// <summary>Records a pending payment (with gateway reference) against an order.</summary>
    Task<Payment> InitiatePaymentAsync(int orderId, string provider, string reference);

    Task<List<Order>> GetCustomerOrdersAsync(int customerId);

    Task<List<Order>> GetOrdersAsync(OrderStatus? status, DateTime? from, DateTime? to);

    Task UpdateStatusAsync(int orderId, OrderStatus status);

    /// <summary>Marks the order paid: payment success, status PaymentConfirmed, decrements stock. Idempotent.</summary>
    Task<Order?> ConfirmPaymentAsync(string reference);

    Task CancelOrderAsync(int orderId);

    Task RefundOrderAsync(int orderId, string note);
}
