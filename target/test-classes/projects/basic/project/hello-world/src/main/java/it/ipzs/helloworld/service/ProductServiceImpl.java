package it.ipzs.helloworld.service;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import it.ipzs.helloworld.service.ProductService;
import it.ipzs.helloworld.entity.Product;
import it.ipzs.helloworld.repository.ProductRepository;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Collection<Product> getProducts() {
        try {
            return productRepository.findAll();
        } catch (DataAccessException e) {
            throw new ProductsRetrievalException(e);
        }
    }

    public Optional<Product> getProductById(int productId) {
        try {
            return productRepository.findById(productId);
        } catch (DataAccessException e) {
            throw new ProductsRetrievalException(e);
        }
    }

    public Product createProduct(String productName) {
        validateProductName(productName);
        try {
            var product = new Product();
            product.setName(productName);
            return productRepository.save(product);
        } catch (DataAccessException e) {
            throw new ProductPersistException(e);
        }
    }

    public void updateProduct(int productId, String newName) {
        validateProductName(newName);
        try {
            var existingProduct = productRepository.findById(productId);
            if(existingProduct.isEmpty()) {
                throw new ProductNotFoundException();
            }
            existingProduct.get().setName(newName);
            productRepository.save(existingProduct.get());
        } catch (DataAccessException e) {
            throw new ProductPersistException(e);
        }
    }

    public boolean deleteProductById(int productId) {
        try {
            return productRepository.deleteById(productId) != 0;
        } catch (DataAccessException e) {
            throw new ProductPersistException(e);
        }
    }

    private static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException();
        }
    }
}
