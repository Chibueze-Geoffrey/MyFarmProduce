using MyFarmProduce.Application.Interfaces;
using MyFarmProduce.Common;
using MyFarmProduce.Web.Models;

namespace MyFarmProduce.Web.Services;

/// <summary>Builds a display cart by joining session quantities with current product data.</summary>
public class CartFactory
{
    private readonly ICartService _cart;
    private readonly ICatalogService _catalog;

    public CartFactory(ICartService cart, ICatalogService catalog)
    {
        _cart = cart;
        _catalog = catalog;
    }

    public async Task<CartViewModel> BuildAsync()
    {
        var items = _cart.GetItems();
        var vm = new CartViewModel();

        if (items.Count > 0)
        {
            var products = await _catalog.GetProductsByIdsAsync(items.Keys);
            foreach (var product in products)
            {
                vm.Lines.Add(new CartLineViewModel
                {
                    Product = product,
                    Quantity = items[product.Id]
                });
            }
            vm.Lines = vm.Lines.OrderBy(l => l.Product.Name).ToList();
        }

        vm.DeliveryFee = vm.IsEmpty ? 0m : AppConstants.FlatDeliveryFee;
        return vm;
    }
}
