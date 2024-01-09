package it.ipzs.helloworld.controller;

import it.ipzs.dapcommons.http.exceptions.InternalServerErrorException;
import it.ipzs.dapcommons.http.exceptions.InvalidFieldException;
import it.ipzs.dapcommons.http.exceptions.NotFoundException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import it.ipzs.helloworld.entity.Product;
import it.ipzs.helloworld.service.ProductService;
import it.ipzs.helloworld.model.GetProductDto;
import it.ipzs.helloworld.model.CreateOrUpdateProductDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import it.ipzs.helloworld.model.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Optional;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService){

        this.productService = productService;
    }
   
    @GetMapping("")
    public Iterable<GetProductDto> getProducts(){
        log.info("Product Controller: getProducts called");
        try {
            return productService.getProducts().stream().map(this::toGetProductDto).toList();
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @GetMapping("/{productId}")
    @SneakyThrows
    public GetProductDto getProductById(@PathVariable int productId){
        log.info("getProductById called with productId: {}" , productId);
        try {
            Optional<Product> product = productService.getProductById(productId);
            if(product.isPresent()) {
                return toGetProductDto(product.get());
            }
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(e);
        }
        throw new NotFoundException();
    }

    @PostMapping("")
    public ResponseEntity<Object> addProduct(@RequestBody CreateOrUpdateProductDto productDto) {
        log.info("addProduct called with product Name: {}", productDto.getName());
        try {
            Product createdProduct = productService.createProduct(productDto.getName());
            return created(createdProduct);
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException("name", e);
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Object> updateProduct(@PathVariable int productId, @RequestBody CreateOrUpdateProductDto productDto) {
        log.info("updateProduct called on product {}. New product name: {} " , productId, productDto.getName());
        try {
            productService.updateProduct(productId, productDto.getName());
            return noContent();
        } catch (ProductService.ProductNotFoundException e) {
            throw new NotFoundException(e);
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException("name", e);
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Object> deleteProduct(@PathVariable int productId) {
        log.info("deleteProduct called with productId: {}" , productId);
        boolean deleted;
        try {
            deleted = productService.deleteProductById(productId);
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(e);
        }

        if(deleted) {
            return noContent();
        }
        else {
            throw new NotFoundException();
        }
    }

    private GetProductDto toGetProductDto(Product product) {
        return new GetProductDto(product.getId(), product.getName());
    }

    private static ResponseEntity<Object> created(Product product) {
        return ResponseEntity.created(UriComponentsBuilder.fromPath("/products/{id}").buildAndExpand(product.getId()).toUri()).build();
    }

    private static ResponseEntity<Object> noContent() {
        return ResponseEntity.noContent().build();
    }
}
