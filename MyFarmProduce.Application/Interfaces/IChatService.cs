using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Application.Interfaces;

public interface IChatService
{
    Task<List<ChatMessage>> GetRecentAsync(int count = 50);
    Task<ChatMessage> AddMessageAsync(int customerId, string senderName, string content);
}
