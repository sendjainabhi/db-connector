package it.ipzs.helloworld.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import it.ipzs.helloworld.entity.Product;

@Repository
public interface ProductRepository extends org.springframework.data.repository.Repository<Product, String>{

    List<Product> findAll();

    Optional<Product> findById(int productId);

    Long deleteById(int productId);

    Product save(Product product);
}

