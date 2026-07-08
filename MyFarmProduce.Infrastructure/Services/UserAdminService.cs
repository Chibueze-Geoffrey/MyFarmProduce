using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common.Enums;
using MyFarmProduce.Domain.Entities;
using MyFarmProduce.Infrastructure.Data;

namespace MyFarmProduce.Infrastructure.Services;

public class UserAdminService : IUserAdminService
{
    private readonly AppDbContext _db;
    private readonly IPasswordHasher _hasher;

    public UserAdminService(AppDbContext db, IPasswordHasher hasher)
    {
        _db = db;
        _hasher = hasher;
    }

    public Task<List<Customer>> GetCustomersAsync(string? search = null)
    {
        var query = _db.Customers.AsQueryable();
        if (!string.IsNullOrWhiteSpace(search))
        {
            var term = search.Trim();
            query = query.Where(c => EF.Functions.Like(c.Name, $"%{term}%")
                                  || EF.Functions.Like(c.Email, $"%{term}%"));
        }
        return query.OrderBy(c => c.Name).ToListAsync();
    }

    public Task<Customer?> GetCustomerAsync(int id) => _db.Customers.FirstOrDefaultAsync(c => c.Id == id);

    public async Task<Customer?> CreateCustomerAsync(string name, string email, string phone, string password)
    {
        email = email.Trim().ToLowerInvariant();
        if (await _db.Customers.AnyAsync(c => c.Email == email))
            return null;

        var customer = new Customer
        {
            Name = name.Trim(),
            Email = email,
            Phone = phone.Trim(),
            PasswordHash = _hasher.Hash(password)
        };
        _db.Customers.Add(customer);
        await _db.SaveChangesAsync();
        return customer;
    }

    public async Task UpdateCustomerAsync(int id, string name, string email, string phone)
    {
        var customer = await _db.Customers.FirstOrDefaultAsync(c => c.Id == id)
            ?? throw new InvalidOperationException("Customer not found.");
        customer.Name = name.Trim();
        customer.Email = email.Trim().ToLowerInvariant();
        customer.Phone = phone.Trim();
        await _db.SaveChangesAsync();
    }

    public async Task DeleteCustomerAsync(int id)
    {
        var customer = await _db.Customers.FirstOrDefaultAsync(c => c.Id == id);
        if (customer is null) return;
        _db.Customers.Remove(customer);
        await _db.SaveChangesAsync();
    }

    public Task<List<ProfileChangeRequest>> GetChangeRequestsAsync(bool pendingOnly = true)
    {
        var query = _db.ProfileChangeRequests.Include(r => r.Customer).AsQueryable();
        if (pendingOnly) query = query.Where(r => r.Status == ChangeRequestStatus.Pending);
        return query.OrderByDescending(r => r.CreatedAt).ToListAsync();
    }

    public async Task ApproveChangeRequestAsync(int requestId, string? note)
    {
        var request = await _db.ProfileChangeRequests.Include(r => r.Customer)
            .FirstOrDefaultAsync(r => r.Id == requestId)
            ?? throw new InvalidOperationException("Request not found.");

        if (request.Status == ChangeRequestStatus.Pending && request.Customer is not null)
        {
            if (request.Field.Equals("Email", StringComparison.OrdinalIgnoreCase))
                request.Customer.Email = request.RequestedValue.ToLowerInvariant();
            else
                request.Customer.Phone = request.RequestedValue;
        }

        request.Status = ChangeRequestStatus.Approved;
        request.ResolvedAt = DateTime.UtcNow;
        request.AdminNote = note;
        await _db.SaveChangesAsync();
    }

    public async Task RejectChangeRequestAsync(int requestId, string? note)
    {
        var request = await _db.ProfileChangeRequests.FirstOrDefaultAsync(r => r.Id == requestId)
            ?? throw new InvalidOperationException("Request not found.");
        request.Status = ChangeRequestStatus.Rejected;
        request.ResolvedAt = DateTime.UtcNow;
        request.AdminNote = note;
        await _db.SaveChangesAsync();
    }
}
