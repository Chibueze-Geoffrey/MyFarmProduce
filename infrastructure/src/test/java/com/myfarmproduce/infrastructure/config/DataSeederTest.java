package com.myfarmproduce.infrastructure.config;

import com.myfarmproduce.infrastructure.repository.AdminRepository;
import com.myfarmproduce.infrastructure.repository.CategoryRepository;
import com.myfarmproduce.infrastructure.repository.ProductRepository;
import com.myfarmproduce.infrastructure.service.Pbkdf2PasswordHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DataSeeder.class, Pbkdf2PasswordHasher.class})
class DataSeederTest {

    @Autowired
    private DataSeeder seeder;
    @Autowired
    private CategoryRepository categories;
    @Autowired
    private ProductRepository products;
    @Autowired
    private AdminRepository admins;

    @Test
    void seedsFiveCategories() {
        seeder.run();
        assertThat(categories.count()).isEqualTo(5);
    }

    @Test
    void seedsNineteenProductsAtLeastThreePerCategory() {
        seeder.run();
        assertThat(products.count()).isEqualTo(19);

        Map<Integer, Long> byCategory = products.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getCategory().getId(), Collectors.counting()));
        assertThat(byCategory.values()).allMatch(count -> count >= 3);
    }

    @Test
    void seedsTwoDefaultAdmins() {
        seeder.run();
        assertThat(admins.count()).isEqualTo(2);
    }

    @Test
    void isIdempotent() {
        seeder.run();
        seeder.run();
        assertThat(categories.count()).isEqualTo(5);
        assertThat(admins.count()).isEqualTo(2);
    }
}
