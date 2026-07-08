using MyFarmProduce.Application.Interfaces;

namespace MyFarmProduce.Web.Services;

/// <summary>Stores uploaded images under wwwroot/uploads/{folder} and returns a relative URL.</summary>
public class LocalFileStorage : IFileStorage
{
    private static readonly HashSet<string> AllowedExtensions =
        new(StringComparer.OrdinalIgnoreCase) { ".jpg", ".jpeg", ".png", ".gif", ".webp" };

    private readonly IWebHostEnvironment _env;

    public LocalFileStorage(IWebHostEnvironment env) => _env = env;

    public async Task<string> SaveImageAsync(Stream content, string originalFileName, string folder)
    {
        var ext = Path.GetExtension(originalFileName);
        if (string.IsNullOrEmpty(ext) || !AllowedExtensions.Contains(ext))
            throw new InvalidOperationException("Only image files (jpg, png, gif, webp) are allowed.");

        var webRoot = _env.WebRootPath ?? Path.Combine(_env.ContentRootPath, "wwwroot");
        var dir = Path.Combine(webRoot, "uploads", folder);
        Directory.CreateDirectory(dir);

        var fileName = $"{Guid.NewGuid():N}{ext.ToLowerInvariant()}";
        var fullPath = Path.Combine(dir, fileName);

        await using (var fs = new FileStream(fullPath, FileMode.Create))
            await content.CopyToAsync(fs);

        return $"/uploads/{folder}/{fileName}";
    }
}
