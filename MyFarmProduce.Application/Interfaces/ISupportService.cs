using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface ISupportService
{
    Task<SupportTicket> StartTicketAsync(int customerId, string firstMessage);
    Task<SupportTicket?> GetTicketAsync(int ticketId, int? restrictToCustomerId = null);
    Task<List<SupportTicket>> GetCustomerTicketsAsync(int customerId);
    Task<List<SupportTicket>> GetAllTicketsAsync();

    /// <summary>Adds the customer's message, generates an assistant reply, persists both.</summary>
    Task<SupportMessage> SendCustomerMessageAsync(int ticketId, int customerId, string content);
}

/// <summary>
/// Pluggable customer-service AI. The default implementation is free and keyless
/// (rule-based, reads catalog/order data). Swap for Claude or another provider later.
/// </summary>
public interface ISupportAssistant
{
    /// <summary>Produces a reply given the customer's latest message and prior turns.</summary>
    Task<string> GetReplyAsync(int customerId, string latestMessage, IReadOnlyList<SupportMessage> history);
}
