using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.EntityFrameworkCore;
using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Infrastructure;
using MyFarmProduce.Infrastructure.Data;
using MyFarmProduce.Web.Hubs;
using MyFarmProduce.Web.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllersWithViews();
builder.Services.AddSignalR();

// Application + infrastructure services (DbContext, domain services, gateways).
builder.Services.AddInfrastructure(builder.Configuration);

// Image uploads to wwwroot/uploads.
builder.Services.AddScoped<IFileStorage, LocalFileStorage>();

// Session-backed shopping cart.
builder.Services.AddHttpContextAccessor();
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(o =>
{
    o.IdleTimeout = TimeSpan.FromHours(2);
    o.Cookie.HttpOnly = true;
    o.Cookie.IsEssential = true;
});
builder.Services.AddScoped<ICartService, CartService>();
builder.Services.AddScoped<CartFactory>();

// Cookie authentication (no full Identity stack).
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.LoginPath = "/Account/Login";
        options.AccessDeniedPath = "/Account/Login";
        options.ExpireTimeSpan = TimeSpan.FromDays(7);
        // Send admin-area visitors to the admin login instead of the customer login.
        options.Events.OnRedirectToLogin = context =>
        {
            var isAdminArea = context.Request.Path.StartsWithSegments("/Admin");
            var target = isAdminArea ? "/Admin/Login" : options.LoginPath.Value ?? "/Account/Login";
            context.Response.Redirect($"{target}?returnUrl={Uri.EscapeDataString(context.Request.Path + context.Request.QueryString)}");
            return Task.CompletedTask;
        };
    });
builder.Services.AddAuthorization();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseRouting();

app.UseSession();
app.UseAuthentication();
app.UseAuthorization();

app.MapStaticAssets();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Catalog}/{action=Index}/{id?}")
    .WithStaticAssets();

app.MapHub<ChatHub>("/hubs/chat");

// Apply migrations and seed an admin account on startup.
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    db.Database.Migrate();
    await DbInitializer.SeedAdminAsync(scope.ServiceProvider);
}

app.Run();
