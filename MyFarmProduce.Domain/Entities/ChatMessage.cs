namespace MyFarmProduce.Domain.Entities;

/// <summary>A message in the shared community chat room.</summary>
public class ChatMessage
{
    public int Id { get; set; }

    public int CustomerId { get; set; }
    public Customer? Customer { get; set; }

    public string SenderName { get; set; } = string.Empty;
    public string Content { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
