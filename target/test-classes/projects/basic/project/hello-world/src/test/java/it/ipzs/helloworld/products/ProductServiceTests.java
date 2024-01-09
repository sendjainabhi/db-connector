package it.ipzs.helloworld.products;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import it.ipzs.helloworld.entity.Product;
import it.ipzs.helloworld.service.ProductServiceImpl;
import it.ipzs.helloworld.repository.ProductRepository;
import it.ipzs.helloworld.service.ProductService;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private final Random random = new Random();

    @Nested
    class GetProducts {

        @Test
        public void getProducts_shouldReturnTheProducts() {
            List<Product> fromRepo = List.of(aProduct(), aProduct(), aProduct());
            when(productRepository.findAll()).thenReturn(fromRepo);

            var products = productService.getProducts();

            // In this case we are not interested in the service remapping the product entities into domain objects,
            // hence we just want the collection to be forwarded as it is
            assertEquals(fromRepo, products);
        }

        @Test
        public void ifRepositoryFails_getProducts_shouldThrowException() {
            givenFindAllWillFail();

            assertThrows(ProductService.ProductsRetrievalException.class, () -> productService.getProducts());
        }
    }

    @Nested
    class GetProductById {

        @Test
        public void ifTheProductExists_getProductById_shouldReturnIt() {
            Product product = aProduct();

            when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

            Optional<Product> result = productService.getProductById(product.getId());

            assertTrue(result.isPresent());
            assertEquals(product, result.get());
        }

        @Test
        public void ifTheProductDoesntExist_getProductById_shouldReturnNull() {
            Product product = aProduct();

            Optional<Product> result = productService.getProductById(product.getId());

            assertFalse(result.isPresent());
        }

        @Test
        public void ifRepositoryFails_getProductById_shouldThrowException() {
            Product product = aProduct();
            givenFindByIdWillFail();

            assertThrows(ProductService.ProductsRetrievalException.class, () -> productService.getProductById(product.getId()));
        }
    }

    @Nested
    class CreateProduct {

        @Test
        public void ifTheProductIsValid_createProduct_ShouldCreateIt() {
            String productName = RandomStringUtils.randomAlphabetic(10);

            productService.createProduct(productName);

            verify(productRepository, times(1)).save(productCaptor.capture());
            assertEquals(productName, productCaptor.getValue().getName());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " "})
        public void ifProductIdIsEmpty_createProduct_shouldThrowException(String emptyId) {
            assertThrows(IllegalArgumentException.class, () -> productService.createProduct(emptyId));
        }

        @Test
        public void ifRepositoryFails_createProduct_shouldThrowException() {
            givenSaveWillFail();

            assertThrows(ProductService.ProductPersistException.class, () -> productService.createProduct(RandomStringUtils.randomAlphabetic(3)));
        }
    }

    @Nested
    class UpdateProduct {

        @Test
        public void ifTheProductIsValid_UpdateProduct_ShouldUpdateIt() {
            var existingProductId = random.nextInt();
            var newName = RandomStringUtils.randomAlphabetic(4);
            givenFindByIdWillFind(new Product(existingProductId, "oldName"));

            productService.updateProduct(existingProductId, newName);

            verify(productRepository, times(1)).save(productCaptor.capture());
            assertEquals(newName, productCaptor.getValue().getName());
        }

        @Test
        public void ifTheProductDoesntExist_UpdateProduct_ShouldThrowException() {
            assertThrows(ProductService.ProductNotFoundException.class,
                    () -> productService.updateProduct(random.nextInt(), "newName"));
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " "})
        public void ifProductNameIsEmpty_updateProduct_shouldThrowException(String emptyName) {
            assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(random.nextInt(), emptyName));
        }

        @Test
        public void ifRepositoryFails_updateProduct_shouldThrowException() {
            var existingProduct = aProduct();
            givenFindByIdWillFind(existingProduct);
            givenSaveWillFail();

            assertThrows(ProductService.ProductPersistException.class, () ->
                    productService.updateProduct(existingProduct.getId(), RandomStringUtils.randomAlphabetic(2)));
        }
    }

    @Nested
    class DeleteProductById {

        @Test
        public void ifTheProductIdExists_deleteProductById_ShouldReturnTrue() {
            int productId = random.nextInt();
            givenDeleteByIdWillReturn(productId, 1L);

            boolean deleted = productService.deleteProductById(productId);

            verify(productRepository).deleteById(productId);
            assertTrue(deleted);
        }

        @Test
        public void ifTheProductIdDoesntExist_deleteProductById_ShouldReturnFalse() {
            int productId = random.nextInt();
            givenDeleteByIdWillReturn(productId, 0L);

            boolean deleted = productService.deleteProductById(productId);

            verify(productRepository).deleteById(productId);
            assertFalse(deleted);
        }

        @Test
        public void ifRepositoryFails_deleteProductById_shouldThrowException() {
            givenDeleteByIdWillFail();

            assertThrows(ProductService.ProductPersistException.class, () ->
                    productService.deleteProductById(random.nextInt()));
        }
    }

    private Product aProduct() {
        return new Product(random.nextInt(), RandomStringUtils.randomAlphanumeric(10));
    }

    private void givenFindAllWillFail() {

        doThrow(mock(DataAccessException.class)).when(productRepository).findAll();
    }

    private void givenFindByIdWillFind(Product product) {
        doReturn(Optional.of(product)).when(productRepository).findById(product.getId());
    }

    private void givenFindByIdWillFail() {
        doThrow(mock(DataAccessException.class)).when(productRepository).findById(anyInt());
    }

    private void givenSaveWillFail() {
        doThrow(mock(DataAccessException.class)).when(productRepository).save(any(Product.class));
    }

    private void givenDeleteByIdWillReturn(int productId, Long result) {
        doReturn(result).when(productRepository).deleteById(productId);
    }

    private void givenDeleteByIdWillFail() {
        doThrow(mock(DataAccessException.class)).when(productRepository).deleteById(anyInt());
    }
}
