using System.Text.Json;

namespace MyFarmProduce.Web.Services;

/// <summary>Session-backed cart: maps productId -> quantity. Persists across navigation.</summary>
public interface ICartService
{
    IReadOnlyDictionary<int, int> GetItems();
    void SetQuantity(int productId, int quantity);
    void Add(int productId, int quantity);
    void Remove(int productId);
    void Clear();
    int TotalItemCount();
}

public class CartService : ICartService
{
    private const string SessionKey = "cart.v1";
    private readonly IHttpContextAccessor _accessor;

    public CartService(IHttpContextAccessor accessor) => _accessor = accessor;

    private ISession Session =>
        _accessor.HttpContext?.Session ?? throw new InvalidOperationException("No active session.");

    private Dictionary<int, int> Load()
    {
        var json = Session.GetString(SessionKey);
        return string.IsNullOrEmpty(json)
            ? new Dictionary<int, int>()
            : JsonSerializer.Deserialize<Dictionary<int, int>>(json) ?? new Dictionary<int, int>();
    }

    private void Save(Dictionary<int, int> items) =>
        Session.SetString(SessionKey, JsonSerializer.Serialize(items));

    public IReadOnlyDictionary<int, int> GetItems() => Load();

    public void SetQuantity(int productId, int quantity)
    {
        var items = Load();
        if (quantity <= 0) items.Remove(productId);
        else items[productId] = quantity;
        Save(items);
    }

    public void Add(int productId, int quantity)
    {
        if (quantity <= 0) return;
        var items = Load();
        items[productId] = items.GetValueOrDefault(productId) + quantity;
        Save(items);
    }

    public void Remove(int productId)
    {
        var items = Load();
        if (items.Remove(productId)) Save(items);
    }

    public void Clear() => Session.Remove(SessionKey);

    public int TotalItemCount() => Load().Values.Sum();
}
