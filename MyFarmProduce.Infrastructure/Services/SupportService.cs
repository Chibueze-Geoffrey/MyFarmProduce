using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class SupportService : ISupportService
{
    private readonly AppDbContext _db;
    private readonly ISupportAssistant _assistant;

    public SupportService(AppDbContext db, ISupportAssistant assistant)
    {
        _db = db;
        _assistant = assistant;
    }

    public async Task<SupportTicket> StartTicketAsync(int customerId, string firstMessage)
    {
        var subject = firstMessage.Trim();
        if (subject.Length > 60) subject = subject[..60] + "…";

        var ticket = new SupportTicket
        {
            CustomerId = customerId,
            Subject = string.IsNullOrWhiteSpace(subject) ? "Support request" : subject,
            Status = SupportTicketStatus.Open
        };
        _db.SupportTickets.Add(ticket);
        await _db.SaveChangesAsync();

        await SendCustomerMessageAsync(ticket.Id, customerId, firstMessage);
        return ticket;
    }

    public Task<SupportTicket?> GetTicketAsync(int ticketId, int? restrictToCustomerId = null)
    {
        var query = _db.SupportTickets
            .Include(t => t.Customer)
            .Include(t => t.Messages.OrderBy(m => m.CreatedAt))
            .AsQueryable();
        if (restrictToCustomerId is int cid)
            query = query.Where(t => t.CustomerId == cid);
        return query.FirstOrDefaultAsync(t => t.Id == ticketId);
    }

    public Task<List<SupportTicket>> GetCustomerTicketsAsync(int customerId) =>
        _db.SupportTickets.Where(t => t.CustomerId == customerId)
            .OrderByDescending(t => t.CreatedAt).ToListAsync();

    public Task<List<SupportTicket>> GetAllTicketsAsync() =>
        _db.SupportTickets.Include(t => t.Customer)
            .OrderByDescending(t => t.CreatedAt).ToListAsync();

    public async Task<SupportMessage> SendCustomerMessageAsync(int ticketId, int customerId, string content)
    {
        var ticket = await _db.SupportTickets
            .Include(t => t.Messages)
            .FirstOrDefaultAsync(t => t.Id == ticketId && t.CustomerId == customerId)
            ?? throw new InvalidOperationException("Ticket not found.");

        var customerMsg = new SupportMessage
        {
            TicketId = ticket.Id,
            Sender = SupportSender.Customer,
            Content = content.Trim()
        };
        ticket.Messages.Add(customerMsg);
        await _db.SaveChangesAsync();

        var history = ticket.Messages.OrderBy(m => m.CreatedAt).ToList();
        var replyText = await _assistant.GetReplyAsync(customerId, content, history);

        var reply = new SupportMessage
        {
            TicketId = ticket.Id,
            Sender = SupportSender.Assistant,
            Content = replyText
        };
        _db.SupportMessages.Add(reply);
        await _db.SaveChangesAsync();
        return reply;
    }
}
