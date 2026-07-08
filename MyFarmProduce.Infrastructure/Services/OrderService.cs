using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Application.Models;
using MyFarmProduce.Common;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class OrderService : IOrderService
{
    private readonly AppDbContext _db;

    public OrderService(AppDbContext db) => _db = db;

    public async Task<Order> CreateOrderAsync(int customerId, PlaceOrderRequest request, IReadOnlyList<CartLineInput> lines)
    {
        if (lines.Count == 0)
            throw new InvalidOperationException("Cannot place an order with an empty cart.");

        var productIds = lines.Select(l => l.ProductId).ToList();
        var products = await _db.Products.Where(p => productIds.Contains(p.Id)).ToDictionaryAsync(p => p.Id);

        var order = new Order
        {
            CustomerId = customerId,
            Status = OrderStatus.Pending,
            DeliveryAddress = request.DeliveryAddress,
            Phone = request.Phone,
            DeliveryNote = request.DeliveryNote,
            DeliveryFee = AppConstants.FlatDeliveryFee,
            CreatedAt = DateTime.UtcNow
        };

        decimal subtotal = 0m;
        foreach (var line in lines)
        {
            if (!products.TryGetValue(line.ProductId, out var product))
                throw new InvalidOperationException($"Product {line.ProductId} no longer exists.");
            if (line.Quantity <= 0)
                continue;
            if (!product.IsAvailable || product.StockQty < line.Quantity)
                throw new InvalidOperationException($"'{product.Name}' does not have {line.Quantity} in stock.");

            order.Items.Add(new OrderItem
            {
                ProductId = product.Id,
                Quantity = line.Quantity,
                UnitPriceAtOrder = product.Price
            });
            subtotal += product.Price * line.Quantity;
        }

        if (order.Items.Count == 0)
            throw new InvalidOperationException("Cannot place an order with an empty cart.");

        order.Subtotal = subtotal;
        order.Total = subtotal + order.DeliveryFee;

        _db.Orders.Add(order);
        await _db.SaveChangesAsync();
        return order;
    }

    public Task<Order?> GetOrderAsync(int orderId, int? restrictToCustomerId = null)
    {
        var query = _db.Orders
            .Include(o => o.Items).ThenInclude(i => i.Product)
            .Include(o => o.Payment)
            .Include(o => o.Customer)
            .AsQueryable();

        if (restrictToCustomerId is int cid)
            query = query.Where(o => o.CustomerId == cid);

        return query.FirstOrDefaultAsync(o => o.Id == orderId);
    }

    public async Task<Payment> InitiatePaymentAsync(int orderId, string provider, string reference)
    {
        var order = await _db.Orders.Include(o => o.Payment).FirstOrDefaultAsync(o => o.Id == orderId)
            ?? throw new InvalidOperationException($"Order {orderId} not found.");

        if (order.Payment is { Status: PaymentStatus.Success })
            return order.Payment;

        // Replace any prior (failed/pending) attempt.
        if (order.Payment is not null)
            _db.Payments.Remove(order.Payment);

        var payment = new Payment
        {
            OrderId = order.Id,
            Provider = provider,
            Reference = reference,
            Status = PaymentStatus.Pending,
            Amount = order.Total
        };
        _db.Payments.Add(payment);
        await _db.SaveChangesAsync();
        return payment;
    }

    public Task<List<Order>> GetCustomerOrdersAsync(int customerId) =>
        _db.Orders
            .Where(o => o.CustomerId == customerId)
            .Include(o => o.Items)
            .OrderByDescending(o => o.CreatedAt)
            .ToListAsync();

    public Task<List<Order>> GetOrdersAsync(OrderStatus? status, DateTime? from, DateTime? to)
    {
        var query = _db.Orders.Include(o => o.Customer).Include(o => o.Items).AsQueryable();

        if (status is OrderStatus s)
            query = query.Where(o => o.Status == s);
        if (from is DateTime f)
            query = query.Where(o => o.CreatedAt >= f);
        if (to is DateTime t)
            query = query.Where(o => o.CreatedAt < t.AddDays(1));

        return query.OrderByDescending(o => o.CreatedAt).ToListAsync();
    }

    public async Task UpdateStatusAsync(int orderId, OrderStatus status)
    {
        var order = await _db.Orders.FirstOrDefaultAsync(o => o.Id == orderId)
            ?? throw new InvalidOperationException($"Order {orderId} not found.");
        order.Status = status;
        await _db.SaveChangesAsync();
    }

    public async Task<Order?> ConfirmPaymentAsync(string reference)
    {
        var payment = await _db.Payments
            .Include(p => p.Order).ThenInclude(o => o!.Items).ThenInclude(i => i.Product)
            .FirstOrDefaultAsync(p => p.Reference == reference);

        if (payment?.Order is null)
            return null;

        var order = payment.Order;

        // Idempotent: if already confirmed, just return.
        if (payment.Status == PaymentStatus.Success)
            return order;

        payment.Status = PaymentStatus.Success;
        payment.PaidAt = DateTime.UtcNow;
        order.Status = OrderStatus.PaymentConfirmed;

        // Decrement stock now that payment is confirmed.
        foreach (var item in order.Items)
            item.Product?.ReduceStock(item.Quantity);

        await _db.SaveChangesAsync();
        return order;
    }

    public async Task CancelOrderAsync(int orderId)
    {
        var order = await _db.Orders
            .Include(o => o.Items).ThenInclude(i => i.Product)
            .FirstOrDefaultAsync(o => o.Id == orderId)
            ?? throw new InvalidOperationException($"Order {orderId} not found.");

        // Return stock if it had already been decremented (i.e. payment was confirmed).
        if (order.Status is OrderStatus.PaymentConfirmed or OrderStatus.Preparing or OrderStatus.OutForDelivery)
        {
            foreach (var item in order.Items)
                item.Product?.Restock(item.Quantity);
        }

        order.Status = OrderStatus.Cancelled;
        await _db.SaveChangesAsync();
    }

    public async Task RefundOrderAsync(int orderId, string note)
    {
        var order = await _db.Orders
            .Include(o => o.Payment)
            .Include(o => o.Items).ThenInclude(i => i.Product)
            .FirstOrDefaultAsync(o => o.Id == orderId)
            ?? throw new InvalidOperationException($"Order {orderId} not found.");

        if (order.Payment is not null)
        {
            order.Payment.Status = PaymentStatus.Refunded;
            order.Payment.RefundedAt = DateTime.UtcNow;
            order.Payment.AdminNote = note;
        }

        // A refunded order is cancelled; restore stock if it had been decremented.
        if (order.Status is OrderStatus.PaymentConfirmed or OrderStatus.Preparing or OrderStatus.OutForDelivery)
        {
            foreach (var item in order.Items)
                item.Product?.Restock(item.Quantity);
        }

        order.Status = OrderStatus.Cancelled;
        await _db.SaveChangesAsync();
    }
}
