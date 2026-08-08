using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace MyFarmProduce.Infrastructure.Data;

// Used only by EF Core tooling (dotnet ef) at design time so migrations can be
// generated against the Infrastructure project directly.
public class AppDbContextFactory : IDesignTimeDbContextFactory<AppDbContext>
{
    public AppDbContext CreateDbContext(string[] args)
    {
        var options = new DbContextOptionsBuilder<AppDbContext>()
            .UseNpgsql(
                "Host=localhost;Database=myfarmproduce;Username=postgres;Password=postgres")
            .Options;

        return new AppDbContext(options);
    }
}
