using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;
using MyFarmProduce.Web.Extensions;

namespace MyFarmProduce.Web.Controllers;

// AI customer service. Each ticket is a conversation with the assistant (admins can view).
[Authorize(Roles = AppConstants.Roles.Customer)]
public class SupportController : Controller
{
    private readonly ISupportService _support;

    public SupportController(ISupportService support) => _support = support;

    public async Task<IActionResult> Index()
    {
        return View(await _support.GetCustomerTicketsAsync(User.GetCustomerId()));
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Start(string message)
    {
        if (string.IsNullOrWhiteSpace(message))
            return RedirectToAction(nameof(Index));

        var ticket = await _support.StartTicketAsync(User.GetCustomerId(), message);
        return RedirectToAction(nameof(Ticket), new { id = ticket.Id });
    }

    public async Task<IActionResult> Ticket(int id)
    {
        var ticket = await _support.GetTicketAsync(id, User.GetCustomerId());
        if (ticket is null) return NotFound();
        return View(ticket);
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Send(int id, string message)
    {
        if (!string.IsNullOrWhiteSpace(message))
            await _support.SendCustomerMessageAsync(id, User.GetCustomerId(), message);
        return RedirectToAction(nameof(Ticket), new { id });
    }
}
