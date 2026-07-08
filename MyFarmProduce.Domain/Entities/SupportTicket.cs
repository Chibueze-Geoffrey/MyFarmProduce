using MyFarmProduce.Common.Enums;

namespace MyFarmProduce.Domain.Entities;

/// <summary>A customer-service conversation handled by the AI assistant (and admins).</summary>
public class SupportTicket
{
    public int Id { get; set; }

    public int CustomerId { get; set; }
    public Customer? Customer { get; set; }

    public string Subject { get; set; } = string.Empty;
    public SupportTicketStatus Status { get; set; } = SupportTicketStatus.Open;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<SupportMessage> Messages { get; set; } = new List<SupportMessage>();
}

public class SupportMessage
{
    public int Id { get; set; }

    public int TicketId { get; set; }
    public SupportTicket? Ticket { get; set; }

    public SupportSender Sender { get; set; }
    public string Content { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
