using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class ProfileService : IProfileService
{
    private readonly AppDbContext _db;

    public ProfileService(AppDbContext db) => _db = db;

    public Task<Customer?> GetCustomerAsync(int customerId) =>
        _db.Customers.FirstOrDefaultAsync(c => c.Id == customerId);

    public async Task UpdateCustomerProfileAsync(int customerId, string name, string? photoUrl)
    {
        var customer = await _db.Customers.FirstOrDefaultAsync(c => c.Id == customerId)
            ?? throw new InvalidOperationException("Customer not found.");
        customer.Name = name.Trim();
        if (photoUrl is not null) customer.PhotoUrl = photoUrl;
        await _db.SaveChangesAsync();
    }

    public async Task<ProfileChangeRequest> RequestFieldChangeAsync(int customerId, string field, string requestedValue)
    {
        var customer = await _db.Customers.FirstOrDefaultAsync(c => c.Id == customerId)
            ?? throw new InvalidOperationException("Customer not found.");

        var current = field.Equals("Email", StringComparison.OrdinalIgnoreCase) ? customer.Email : customer.Phone;

        var request = new ProfileChangeRequest
        {
            CustomerId = customerId,
            Field = field,
            CurrentValue = current,
            RequestedValue = requestedValue.Trim(),
            Status = ChangeRequestStatus.Pending
        };
        _db.ProfileChangeRequests.Add(request);
        await _db.SaveChangesAsync();
        return request;
    }

    public Task<List<ProfileChangeRequest>> GetMyChangeRequestsAsync(int customerId) =>
        _db.ProfileChangeRequests
            .Where(r => r.CustomerId == customerId)
            .OrderByDescending(r => r.CreatedAt)
            .ToListAsync();

    public Task<Admin?> GetAdminAsync(int adminId) => _db.Admins.FirstOrDefaultAsync(a => a.Id == adminId);

    public async Task UpdateAdminProfileAsync(int adminId, string name, string? photoUrl)
    {
        var admin = await _db.Admins.FirstOrDefaultAsync(a => a.Id == adminId)
            ?? throw new InvalidOperationException("Admin not found.");
        admin.Name = name.Trim();
        if (photoUrl is not null) admin.PhotoUrl = photoUrl;
        await _db.SaveChangesAsync();
    }
}
