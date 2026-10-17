package com.curso.mercat.service;

import com.curso.mercat.model.Product;
import com.curso.mercat.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service //Indica a Spring que esta clase es un bean de lógica de negocio
public class ProductService {

    private final ProductRepository productRepository;

    // Inyección de dependencias por constructor
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Consulta de solo lectura por código de barras
    @Transactional(readOnly = true)
    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }

    // Creamos un producto
    public Product createProduct(Product product) {
        if (productRepository.findByBarcode(product.getBarcode()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + product.getBarcode());
        }
        return productRepository.save(product);
    }
}
