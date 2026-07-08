using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

/// <summary>
/// Free, keyless customer-service assistant. Answers common questions using live
/// catalog and order data via keyword matching. Implements <see cref="ISupportAssistant"/>
/// so a real LLM (Claude, etc.) can be dropped in later without touching callers.
/// </summary>
public class RuleBasedSupportAssistant : ISupportAssistant
{
    private readonly AppDbContext _db;

    public RuleBasedSupportAssistant(AppDbContext db) => _db = db;

    public async Task<string> GetReplyAsync(int customerId, string latestMessage, IReadOnlyList<SupportMessage> history)
    {
        var text = latestMessage.ToLowerInvariant();

        bool Has(params string[] words) => words.Any(w => text.Contains(w));

        if (Has("hello", "hi ", "hey", "good morning", "good afternoon") && text.Length < 25)
            return "Hi there! 👋 I'm the MyFarmProduce assistant. I can help with orders, delivery, payment, and what's in stock. What do you need?";

        // Order status (explicit order references only, so "how much / when will it arrive"
        // routes to the pricing/delivery answers below instead).
        if (Has("my order", "order status", "status of", "track", "where is my"))
        {
            var order = await _db.Orders
                .Where(o => o.CustomerId == customerId)
                .OrderByDescending(o => o.CreatedAt)
                .FirstOrDefaultAsync();

            if (order is null)
                return "I couldn't find any orders on your account yet. Once you place and pay for an order, you can track it under \"My Orders\".";

            return order.Status switch
            {
                OrderStatus.Pending => $"Your most recent order #{order.Id} is still Pending payment. Complete payment from \"My Orders\" to get it moving.",
                OrderStatus.PaymentConfirmed => $"Order #{order.Id} is paid and being prepared. Delivery is same/next day — we'll contact you on {order.Phone} to confirm timing.",
                OrderStatus.Preparing => $"Order #{order.Id} is being prepared for delivery right now.",
                OrderStatus.OutForDelivery => $"Good news — order #{order.Id} is out for delivery to {order.DeliveryAddress}.",
                OrderStatus.Delivered => $"Order #{order.Id} was delivered. Enjoy! You can reorder the same items from \"My Orders\".",
                OrderStatus.Cancelled => $"Order #{order.Id} was cancelled. If you didn't expect this, let me know and I'll flag it for an admin.",
                _ => $"Your latest order is #{order.Id} (status: {order.Status})."
            };
        }

        // Payment
        if (Has("pay", "payment", "card", "transfer", "ussd", "checkout fail", "money"))
            return "We accept card, bank transfer, and USSD. Payment is taken at checkout; if a payment fails, the order stays Pending and you can retry it from \"My Orders\". Delivery fee is a flat ₦" + AppConstants.FlatDeliveryFee.ToString("N0") + ".";

        // Delivery
        if (Has("deliver", "shipping", "when will", "how long", "fee", "address"))
            return $"Delivery is same or next day and we confirm timing by phone. There's a flat delivery fee of ₦{AppConstants.FlatDeliveryFee:N0} added at checkout. You can set your delivery address and note during checkout.";

        // Refund / cancel
        if (Has("refund", "cancel", "return", "wrong item", "money back"))
            return "For a refund or cancellation, reply here with your order number and reason. I'll log it and an admin will process the refund on the payment gateway and update your order.";

        // Availability / products
        if (Has("stock", "available", "have you got", "do you sell", "in stock", "price of", "how much"))
        {
            var sample = await _db.Products
                .Where(p => p.IsAvailable && p.StockQty > 0)
                .OrderBy(p => p.Name)
                .Take(5)
                .Select(p => $"{p.Name} (₦{p.Price:N0}/{p.Unit})")
                .ToListAsync();

            var list = sample.Count > 0 ? string.Join(", ", sample) : "our fresh produce range";
            return $"You can browse everything in stock on the Shop page. A few in-stock items right now: {list}. Search by name there to check a specific product.";
        }

        // How to order
        if (Has("how do i order", "how to order", "place an order", "buy"))
            return "Ordering is easy: browse the Shop, add items to your cart, go to Checkout, enter your delivery details, and pay online. You'll get an order confirmation and can track progress under \"My Orders\".";

        // Account / profile
        if (Has("change my", "update my", "phone number", "email address", "profile", "password"))
            return "You can edit your name and photo on your Profile page. Phone and email are locked for security — request a change from your Profile and an admin will apply it.";

        // Human / admin
        if (Has("human", "agent", "speak to", "manager", "admin", "real person"))
            return "No problem — I've logged this conversation, and an admin can review it and follow up. Meanwhile, tell me the details and I'll do my best to help right away.";

        if (Has("thank", "thanks", "cheers", "appreciate"))
            return "You're welcome! 🌽 Anything else I can help with?";

        return "I can help with order status, delivery, payment, refunds, and product availability. Could you tell me a bit more — for example your order number, or the product you're asking about?";
    }
}
