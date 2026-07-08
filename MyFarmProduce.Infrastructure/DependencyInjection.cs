using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Infrastructure.Data;
using MyFarmProduce.Infrastructure.Security;
using MyFarmProduce.Infrastructure.Services;

namespace MyFarmProduce.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration config)
    {
        services.AddDbContext<AppDbContext>(options =>
            options.UseSqlServer(config.GetConnectionString("DefaultConnection")));

        services.AddScoped<IPasswordHasher, Pbkdf2PasswordHasher>();
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IAdminAuthService, AdminAuthService>();
        services.AddScoped<ICatalogService, CatalogService>();
        services.AddScoped<IInventoryService, InventoryService>();
        services.AddScoped<IOrderService, OrderService>();
        services.AddScoped<IPaymentGateway, DevPaymentGateway>();

        services.AddScoped<IProfileService, ProfileService>();
        services.AddScoped<IUserAdminService, UserAdminService>();
        services.AddScoped<IChatService, ChatService>();
        services.AddScoped<ISupportService, SupportService>();
        // Free keyless assistant by default; swap for a Claude-backed ISupportAssistant later.
        services.AddScoped<ISupportAssistant, RuleBasedSupportAssistant>();

        services.AddScoped<IEmailSender, LoggingEmailSender>();
        services.AddScoped<ISmsSender, NoopSmsSender>();
        services.AddScoped<INotificationService, NotificationService>();

        return services;
    }
}
