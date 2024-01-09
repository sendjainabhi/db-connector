#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import ${package}.repository.ProductRepository;
import ${package}.entity.Product;
import ${package}.service.ProductServiceImpl;
import ${package}.service.ProductService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductServiceImpl productService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Random random = new Random();

    @Nested
    class GetProducts {

        @Test
        // SneakyThrows can be used to sneakily throw checked exceptions without actually declaring this in your method's throws clause.
        // See https://projectlombok.org/features/SneakyThrows
        @SneakyThrows
        public void ifThereAreProducts_getProducts_shouldReturnThem() {
            Product product1 = aProduct();
            Product product2 = aProduct();
            givenGetProductsWillReturn(product1, product2);

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            [
                                {
                                    "id": %d,
                                    "name": "%s"
                                },
                                {
                                    "id": %d,
                                    "name": "%s"
                                }
                            ]
                            """.formatted(product1.getId(), product1.getName(), product2.getId(), product2.getName())));
        }

        @Test
        @SneakyThrows
        public void ifThereAreNoProducts_getProducts_shouldReturnAnEmptyCollection() {
            givenGetProductsWillReturnNothing();

            mockMvc.perform(get("/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            []
                            """));
        }

        @Test
        @SneakyThrows
        public void ifProductsRetrieveFails_getProducts_shouldReturnHttpInternalServerError() {
            givenGetProductsWillFail();

            mockMvc.perform(get("/products"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class GetProductById {

        @Test
        @SneakyThrows
        public void ifThereIsAProductWithAGivenId_getProductById_shouldReturnThatProduct() {
            Product product = aProduct();
            givenGetProductByIdWillReturn(product);

            mockMvc.perform(get("/products/{productId}", product.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                            {
                                "id": %d,
                                "name": "%s"
                            }
                            """.formatted(product.getId(), product.getName())));
        }

        @Test
        @SneakyThrows
        public void ifThereIsNotAProductWithAGivenId_getProductById_shouldReturnNotFound() {
            mockMvc.perform(get("/products/{productId}", RandomStringUtils.randomNumeric(1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @SneakyThrows
        public void ifProductByIdRetrieveFails_getProducts_shouldReturnHttpInternalServerError() {
            givenGetProductByIdWillFail();

            mockMvc.perform(get("/products/{productId}", RandomStringUtils.randomNumeric(1)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class AddProduct {

        @Test
        @SneakyThrows
        public void ifProductIsInvalid_addProduct_shouldReturnBadRequest() {
            givenProductNameIsInvalid();

            ResultActions result = mockMvc.perform(post("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "name": "%s"
                        }
                        """.formatted(RandomStringUtils.randomAlphabetic(5))));

            result.andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                        {
                            "fieldName": "name"
                        }
                        """));
        }

        @Test
        @SneakyThrows
        public void ifProductIsValid_addProduct_shouldAddANewProduct() {
            String productName = RandomStringUtils.randomAlphabetic(5);
            var createdProduct = givenCreateProductWillSucceed(productName);

            ResultActions result = mockMvc.perform(post("/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "name": "%s"
                        }
                        """.formatted(createdProduct.getName())));

            result.andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/products/" + createdProduct.getId()));
        }

        @Test
        @SneakyThrows
        public void ifProductCreationFails_addProduct_shouldReturnHttpInternalServerError() {
            givenCreateProductWillFail();

            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(aProduct())))

                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class DeleteProduct {

        @Test
        @SneakyThrows
        public void givenProductId_deleteProduct_shouldReturnNoContent() {
            int productId = random.nextInt();
            when(productService.deleteProductById(productId)).thenReturn(true);

            mockMvc.perform(delete("/products/{productId}", productId))
                    .andExpect(status().isNoContent());
            verify(productService).deleteProductById(productId);
        }

        @Test
        @SneakyThrows
        public void ifTheGivenProductIdDoesntExist_deleteProduct_shouldReturnNotFound() {
            int productId = random.nextInt();

            when(productService.deleteProductById(productId)).thenReturn(false);

            mockMvc.perform(delete("/products/{productId}", productId))
                    .andExpect(status().isNotFound());
            verify(productService).deleteProductById(productId);
        }

        @Test
        @SneakyThrows
        public void ifProductDeleteFails_deleteProduct_shouldReturnNotFound() {
            givenDeleteProductByIdWillFail();

            mockMvc.perform(delete("/products/{productId}", RandomStringUtils.randomNumeric(2)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    class UpdateProduct {

        @Test
        @SneakyThrows
        public void ifProductIdExistsAndProductIsValid_updateProduct_shouldReturnNoContent() {
            Product product = aProduct();

            ResultActions result = mockMvc.perform(put("/products/{productId}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "name": "%s"
                        }
                        """.formatted(product.getName())));

            result.andExpect(status().isNoContent());
            verify(productService).updateProduct(product.getId(), product.getName());
        }

        @Test
        @SneakyThrows
        public void ifProductIdDoesntExist_updateProduct_shouldReturnNotFound() {
            Product product = aProduct();
            doThrow(new ProductService.ProductNotFoundException()).when(productService).updateProduct(anyInt(), anyString());

            ResultActions result = mockMvc.perform(put("/products/{productId}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "name": "%s"
                        }
                        """.formatted(product.getName())));

            result.andExpect(status().isNotFound());
        }

        @Test
        @SneakyThrows
        public void ifProductIsInvalid_updateProduct_shouldReturnBadRequest() {
            Product product = aProduct();
            givenProductNameIsInvalid();

            ResultActions result = mockMvc.perform(put("/products/{productId}",  product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "name": "%s"
                        }
                        """.formatted(product.getName())));

            result.andExpect(status().isBadRequest())
                    .andExpect(content().json("""
                            {
                                "fieldName": "name"
                            }
                            """));
        }

        @Test
        @SneakyThrows
        public void ifUpdateFails_updateProduct_shouldReturnInternalServerError() {
            Product product = aProduct();
            givenUpdateProductWillFail();

            ResultActions result = mockMvc.perform(put("/products/{productId}", product.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "name": "%s"
                        }
                        """.formatted(product.getName())));

            result.andExpect(status().isInternalServerError());
        }
    }

    private Product aProduct() {
        return new Product(random.nextInt(), RandomStringUtils.randomAlphanumeric(10));
    }

    private void givenGetProductsWillReturn(Product... products) {
        when(productService.getProducts()).thenReturn(Arrays.stream(products).toList());
    }

    private void givenGetProductsWillReturnNothing() {
        givenGetProductsWillReturn();
    }

    private void givenGetProductByIdWillReturn(Product product) {
        when(productService.getProductById(product.getId())).thenReturn(Optional.of(product));
    }

    private Product givenCreateProductWillSucceed(String productName) {
        Product product = aProduct();
        product.setName(productName);
        when(productService.createProduct(productName)).thenReturn(product);
        return product;
    }

    private void givenCreateProductWillFail() {
        doThrow(new RuntimeException()).when(productService).createProduct(anyString());
    }

    private void givenGetProductsWillFail() {
        doThrow(new RuntimeException()).when(productService).getProducts();
    }

    private void givenGetProductByIdWillFail() {
        doThrow(new RuntimeException()).when(productService).getProductById(anyInt());
    }

    private void givenDeleteProductByIdWillFail() {
        doThrow(new RuntimeException()).when(productService).deleteProductById(anyInt());
    }

    private void givenUpdateProductWillFail() {
        doThrow(new RuntimeException()).when(productService).updateProduct(anyInt(), anyString());
    }

    private void givenProductNameIsInvalid() {
        doThrow(new IllegalArgumentException()).when(productService).createProduct(anyString());
        doThrow(new IllegalArgumentException()).when(productService).updateProduct(anyInt(), anyString());
    }
}
