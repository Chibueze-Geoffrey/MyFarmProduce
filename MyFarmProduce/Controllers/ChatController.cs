using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;

namespace MyFarmProduce.Web.Controllers;

// Community chat page (real-time via the /hubs/chat SignalR hub).
[Authorize(Roles = AppConstants.Roles.Customer)]
public class ChatController : Controller
{
    private readonly IChatService _chat;

    public ChatController(IChatService chat) => _chat = chat;

    public async Task<IActionResult> Index()
    {
        return View(await _chat.GetRecentAsync(50));
    }
}
