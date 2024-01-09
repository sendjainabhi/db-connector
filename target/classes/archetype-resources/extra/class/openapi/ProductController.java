#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package it.ipzs.helloworld.controller;

import it.ipzs.dapcommons.http.exceptions.InternalServerErrorException;
import it.ipzs.dapcommons.http.exceptions.InvalidFieldException;
import it.ipzs.dapcommons.http.exceptions.NotFoundException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ${package}.entity.Product;
import ${package}.service.ProductService;
import jakarta.validation.Valid;
import ${package}.model.GetProductDto;
import ${package}.model.CreateOrUpdateProductDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@Slf4j
public class ProductController implements ProductsApi {

    private final ProductService productService;

    public ProductController(ProductService productService){

        this.productService = productService;
    }
    
	@SuppressWarnings("unchecked")
	@Override
	@GetMapping("")
	public ResponseEntity<Object> getProducts() {
      log.info("Product Controller: getProducts called");
      try {
    	  List<GetProductDto> list = productService.getProducts().stream().map(this::toGetProductDto).toList();
    	  
          return new ResponseEntity<>(list, HttpStatus.OK);
      } catch (RuntimeException e) {
          throw new InternalServerErrorException(e);
      }
	}
    
	@Override
	@DeleteMapping("/{productId}")
	public ResponseEntity<Object> deleteProduct(Integer productId) {
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

	@Override
    @GetMapping("/{productId}")
    @SneakyThrows
	public ResponseEntity<GetProductDto> getProductById(Integer productId) {
        log.info("getProductById called with productId: {}" , productId);
        try {
            Optional<Product> product = productService.getProductById(productId);
            if(product.isPresent()) {
            	
                return new ResponseEntity<>(toGetProductDto(product.get()), HttpStatus.OK);
            }
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(e);
        }
        throw new NotFoundException();
	}

	@Override
	@PutMapping("/{productId}")
	public ResponseEntity<Object> updateProduct(Integer productId,
			@Valid CreateOrUpdateProductDto createOrUpdateProductDto) {
        log.info("updateProduct called on product {}. New product name: {} " , productId, createOrUpdateProductDto.getName());
        try {
            productService.updateProduct(productId, createOrUpdateProductDto.getName());
            return noContent();
        } catch (ProductService.ProductNotFoundException e) {
            throw new NotFoundException(e);
        } catch (IllegalArgumentException e) {
            throw new InvalidFieldException("name", e);
        } catch (RuntimeException e) {
            throw new InternalServerErrorException(e);
        }
	}

	@Override
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

    private GetProductDto toGetProductDto(Product product) {
    	GetProductDto getProductDto = new GetProductDto();
    	getProductDto.setId(product.getId());
    	getProductDto.setName(product.getName());
        return getProductDto;
    }

    private static ResponseEntity<Object> created(Product product) {
        return ResponseEntity.created(UriComponentsBuilder.fromPath("/products/{id}").buildAndExpand(product.getId()).toUri()).build();
    }

    private static ResponseEntity<Object> noContent() {
        return ResponseEntity.noContent().build();
    }


}
