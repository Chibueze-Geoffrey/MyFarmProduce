using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;

namespace MyFarmProduce.Web.Hubs;

/// <summary>Real-time community chat. Persists each message and broadcasts to all clients.</summary>
[Authorize(Roles = AppConstants.Roles.Customer)]
public class ChatHub : Hub
{
    private readonly IChatService _chat;

    public ChatHub(IChatService chat) => _chat = chat;

    public async Task SendMessage(string content)
    {
        content = (content ?? string.Empty).Trim();
        if (content.Length == 0) return;
        if (content.Length > 2000) content = content[..2000];

        var customerId = int.Parse(Context.User!.FindFirstValue(ClaimTypes.NameIdentifier)!);
        var name = Context.User!.Identity!.Name ?? "Customer";

        var saved = await _chat.AddMessageAsync(customerId, name, content);

        await Clients.All.SendAsync("ReceiveMessage",
            saved.SenderName, saved.Content, saved.CreatedAt.ToLocalTime().ToString("g"));
    }
}
