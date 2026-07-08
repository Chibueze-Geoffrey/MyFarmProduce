using System.Security.Claims;

namespace MyFarmProduce.Web.Extensions;

public static class ClaimsPrincipalExtensions
{
    public static int GetCustomerId(this ClaimsPrincipal user)
    {
        var id = user.FindFirstValue(ClaimTypes.NameIdentifier);
        return int.TryParse(id, out var value)
            ? value
            : throw new InvalidOperationException("No authenticated customer.");
    }
}
