using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using MyFarmProduce.Domain.Entities;

namespace MyFarmProduce.Infrastructure.Data.Configurations;

public class PaymentConfiguration : IEntityTypeConfiguration<Payment>
{
    public void Configure(EntityTypeBuilder<Payment> builder)
    {
        builder.HasKey(p => p.Id);

        builder.Property(p => p.Provider).IsRequired().HasMaxLength(50);
        builder.Property(p => p.Reference).IsRequired().HasMaxLength(100);
        builder.Property(p => p.Status).HasConversion<int>();
        builder.Property(p => p.Amount).HasPrecision(18, 2);

        builder.HasIndex(p => p.Reference).IsUnique();
    }
}
