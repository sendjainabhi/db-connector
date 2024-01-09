package it.ipzs.helloworld.products;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import it.ipzs.helloworld.entity.Product;
import it.ipzs.helloworld.repository.ProductRepository;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Nested
    class FindAll {

        @Test
        public void ifThereAreProducts_findAll_shouldReturnThem() {
            Product expected1 = new Product(1, "product-1");
            Product expected2 = new Product(2, "product-2");

            var result = productRepository.findAll();

            assertThat(result).hasSize(2);
            assertEquals(expected1, result.get(0));
            assertEquals(expected2, result.get(1));
        }

        @Test
        @Sql("/data-empty.sql")
        public void ifThereAreNoProducts_findAll_ShouldReturnEmptyCollection() {
            var result = productRepository.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindById {

        @Test
        public void ifProductIdExists_findById_shouldReturnIt() {
            Product expected = new Product(1, "product-1");

            Optional<Product> result = productRepository.findById(expected.getId());

            assertTrue(result.isPresent());
            assertEquals(expected, result.get());
        }

        @Test
        public void ifProductDoesntExist_findById_shouldReturnEmptyOptional() {
            Optional<Product> result = productRepository.findById(99);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    class Save {

        @Test
        @Sql("/data-empty.sql")
        public void save_shouldPersistTheProduct() {
            var product = aProduct();

            var insertedProduct = productRepository.save(product);

            assertNotNull(insertedProduct);
            assertNotEquals(0, insertedProduct.getId());
            assertEquals(product.getName(), insertedProduct.getName());
            var allProducts = productRepository.findAll();
            assertThat(allProducts).hasSize(1);
            assertEquals(product.getName(), allProducts.get(0).getName());
        }

    }

    @Nested
    class DeleteById {

        @Test
        public void ifProductExists_deleteById_shouldDeleteIt() {
            int productId = 2;

            Long result = productRepository.deleteById(productId);

            assertEquals(1, result);
            assertThat(productRepository.findAll()).hasSize(1);
            assertFalse(productRepository.findById(productId).isPresent());
        }

        @Test
        public void ifProductDoesntExist_deleteById_shouldNotDeleteAnything() {
            int productId = 99;

            Long result = productRepository.deleteById(productId);

            assertEquals(0, result);
            assertThat(productRepository.findAll()).hasSize(2);
        }
    }

    private Product aProduct() {
        Product product = new Product();
        product.setName(RandomStringUtils.randomAlphanumeric(10));
        return product;
    }
}
