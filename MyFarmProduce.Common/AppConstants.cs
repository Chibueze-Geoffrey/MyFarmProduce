namespace MyFarmProduce.Common;

public static class AppConstants
{
    /// <summary>Flat delivery fee applied at checkout (MVP – no zone logic yet).</summary>
    public const decimal FlatDeliveryFee = 1500m;

    public static class Roles
    {
        public const string Admin = "Admin";
        public const string Customer = "Customer";
    }

    /// <summary>Subfolders under wwwroot/uploads for uploaded images.</summary>
    public static class UploadFolders
    {
        public const string Products = "products";
        public const string Avatars = "avatars";
    }
}
