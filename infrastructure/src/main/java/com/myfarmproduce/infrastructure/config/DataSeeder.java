package com.myfarmproduce.infrastructure.config;

import com.myfarmproduce.application.service.PasswordHasher;
import com.myfarmproduce.domain.entity.Admin;
import com.myfarmproduce.domain.entity.Category;
import com.myfarmproduce.domain.entity.Product;
import com.myfarmproduce.domain.enums.ProductUnit;
import com.myfarmproduce.infrastructure.repository.AdminRepository;
import com.myfarmproduce.infrastructure.repository.CategoryRepository;
import com.myfarmproduce.infrastructure.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Idempotent startup seeding: catalog data and default admin accounts. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final AdminRepository admins;
    private final PasswordHasher hasher;

    public DataSeeder(CategoryRepository categories, ProductRepository products, AdminRepository admins,
                       PasswordHasher hasher) {
        this.categories = categories;
        this.products = products;
        this.admins = admins;
        this.hasher = hasher;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedCatalog();
        seedAdmin("admin@myfarmproduce.local", "Store Admin", "Admin@123");
        seedAdmin("chibuezegeoffrey@gmail.com", "Chibueze Geoffrey", "Admin@123");
    }

    private void seedCatalog() {
        if (categories.count() > 0) return;

        Category vegetables = categories.save(category("Vegetables"));
        Category tubers = categories.save(category("Tubers/Roots"));
        Category grains = categories.save(category("Grains/Legumes"));
        Category fruits = categories.save(category("Fruits"));
        Category proteins = categories.save(category("Proteins/Livestock"));

        Map<String, Object[]> entries = new LinkedHashMap<>();
        // name -> {category, description, unit, price, stock}
        entries.put("Fresh Tomatoes", new Object[]{vegetables, "Ripe red tomatoes", ProductUnit.Basket, "8000", 40});
        entries.put("Spinach (Efo)", new Object[]{vegetables, "Green leafy spinach", ProductUnit.Bunch, "500", 120});
        entries.put("Bell Peppers", new Object[]{vegetables, "Mixed colour bell peppers", ProductUnit.Kg, "2500", 60});
        entries.put("Onions", new Object[]{vegetables, "Red onions", ProductUnit.Kg, "1800", 100});

        entries.put("Yam Tuber", new Object[]{tubers, "Large white yam", ProductUnit.Piece, "3500", 80});
        entries.put("Irish Potatoes", new Object[]{tubers, "Fresh Irish potatoes", ProductUnit.Kg, "2200", 90});
        entries.put("Sweet Potatoes", new Object[]{tubers, "Orange-flesh sweet potatoes", ProductUnit.Kg, "1500", 70});
        entries.put("Cassava", new Object[]{tubers, "Fresh cassava tubers", ProductUnit.Kg, "900", 150});

        entries.put("Local Rice", new Object[]{grains, "Destoned local rice", ProductUnit.Kg, "1700", 200});
        entries.put("Brown Beans", new Object[]{grains, "Oloyin brown beans", ProductUnit.Kg, "2100", 130});
        entries.put("White Maize", new Object[]{grains, "Dried white maize", ProductUnit.Kg, "1200", 160});

        entries.put("Bananas", new Object[]{fruits, "Ripe bananas", ProductUnit.Bunch, "1500", 75});
        entries.put("Pineapple", new Object[]{fruits, "Sweet pineapple", ProductUnit.Piece, "1200", 50});
        entries.put("Watermelon", new Object[]{fruits, "Large watermelon", ProductUnit.Piece, "2500", 40});
        entries.put("Oranges", new Object[]{fruits, "Juicy oranges", ProductUnit.Basket, "6000", 30});

        entries.put("Live Chicken", new Object[]{proteins, "Broiler chicken", ProductUnit.Piece, "9000", 25});
        entries.put("Catfish", new Object[]{proteins, "Fresh live catfish", ProductUnit.Kg, "4000", 60});
        entries.put("Eggs", new Object[]{proteins, "Crate of eggs", ProductUnit.Crate, "4500", 45});
        entries.put("Goat Meat", new Object[]{proteins, "Fresh goat meat", ProductUnit.Kg, "6500", 35});

        entries.forEach((name, fields) -> {
            Product product = new Product();
            product.setName(name);
            product.setCategory((Category) fields[0]);
            product.setDescription((String) fields[1]);
            product.setUnit((ProductUnit) fields[2]);
            product.setPrice(new BigDecimal((String) fields[3]));
            product.setAvailable(true);
            product.setInitialStock((int) fields[4]);
            products.save(product);
        });
    }

    private Category category(String name) {
        Category category = new Category();
        category.setName(name);
        return category;
    }

    private void seedAdmin(String email, String name, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        if (admins.existsByEmail(normalizedEmail)) return;

        Admin admin = new Admin();
        admin.setName(name);
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(hasher.hash(password));
        admins.save(admin);
    }
}
