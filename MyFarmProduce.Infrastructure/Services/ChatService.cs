using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class ChatService : IChatService
{
    private readonly AppDbContext _db;

    public ChatService(AppDbContext db) => _db = db;

    public async Task<List<ChatMessage>> GetRecentAsync(int count = 50)
    {
        var recent = await _db.ChatMessages
            .OrderByDescending(m => m.CreatedAt)
            .Take(count)
            .ToListAsync();
        recent.Reverse(); // oldest first for display
        return recent;
    }

    public async Task<ChatMessage> AddMessageAsync(int customerId, string senderName, string content)
    {
        var message = new ChatMessage
        {
            CustomerId = customerId,
            SenderName = senderName,
            Content = content.Trim()
        };
        _db.ChatMessages.Add(message);
        await _db.SaveChangesAsync();
        return message;
    }
}
