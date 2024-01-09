package it.ipzs.helloworld.service;

import java.util.Collection;
import java.util.Optional;
import it.ipzs.helloworld.entity.Product;

public interface ProductService {

    /**
     * @exception ProductsRetrievalException if something goes wrong trying to fetch the products
     */
    Collection<Product> getProducts();

    /**
     * @exception ProductsRetrievalException if something goes wrong trying to fetch the product
     */
    Optional<Product> getProductById(int productId);

    /**
     * @exception IllegalArgumentException if the product name is invalid
     * @return The newly created product
     */
    Product createProduct(String name);

    /**
     * @exception IllegalArgumentException if the product name is invalid
     * @exception ProductNotFoundException if the given product id doesn't exist
     */
    void updateProduct(int productId, String newName);

    /**
     * @return true if the product has been deleted, false if it was already missing
     */
    boolean deleteProductById(int productId);

    class ProductsRetrievalException extends RuntimeException {
        public ProductsRetrievalException(Throwable cause) {
            super(null, cause);
        }
    }

    class ProductPersistException extends RuntimeException {
        public ProductPersistException(Throwable cause) {
            super(null, cause);
        }
    }

    class ProductNotFoundException extends RuntimeException { }
}
