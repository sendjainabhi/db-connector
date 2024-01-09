#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import ${package}.entity.Product;

@Repository
public interface ProductRepository extends org.springframework.data.repository.Repository<Product, String>{

    List<Product> findAll();

    Optional<Product> findById(int productId);

    Long deleteById(int productId);

    Product save(Product product);
}

