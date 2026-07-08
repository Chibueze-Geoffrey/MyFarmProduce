using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using MyFarmProduce.Common;

namespace MyFarmProduce.Web.Filters;

/// <summary>
/// Forces a customer carrying the MustChangePassword claim onto the change-password
/// screen — every other action redirects there until they set a new password.
/// </summary>
public class ForcePasswordChangeFilter : IAsyncActionFilter
{
    public async Task OnActionExecutionAsync(ActionExecutingContext context, ActionExecutionDelegate next)
    {
        var user = context.HttpContext.User;
        var mustChange = user.Identity?.IsAuthenticated == true
                         && user.HasClaim(AppConstants.Claims.MustChangePassword, "true");

        if (mustChange)
        {
            var rd = context.RouteData.Values;
            var ctrl = rd["controller"]?.ToString();
            var act = rd["action"]?.ToString();

            var allowed = ctrl == "Account" && act is "ChangePassword" or "Logout";
            if (!allowed)
            {
                context.Result = new RedirectToActionResult("ChangePassword", "Account", null);
                return;
            }
        }

        await next();
    }
}
