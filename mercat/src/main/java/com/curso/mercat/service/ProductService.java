package com.curso.mercat.service;

import com.curso.mercat.model.Product;
import com.curso.mercat.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    // Creamos un producto
    public Product createProduct(Product product) {
        if (productRepository.findByBarcode(product.getBarcode()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + product.getBarcode());
        }
        return productRepository.save(product);
    }


    //Modificamos un producto
    @Transactional
    public Product updateProduct(String barcode, Product productData) {
        // 1. Buscar el producto existente
        Product existingProduct = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto con código: " + barcode));

        // 2. Si se quiere cambiar el barcode, verificar que el nuevo no esté repetido en otro producto
        if (productData.getBarcode() != null && !productData.getBarcode().equals(barcode)) {
            if (productRepository.findByBarcode(productData.getBarcode()).isPresent()) {
                throw new IllegalArgumentException("El nuevo código de barras ya está en uso: " + productData.getBarcode());
            }
            existingProduct.setBarcode(productData.getBarcode());
        }

        // 3. Actualizar campos permitidos
        if (productData.getName() != null) {
            existingProduct.setName(productData.getName());
        }
        if (productData.getPrice() != null) {
            existingProduct.setPrice(productData.getPrice());
        }

        // 4. Guardar los cambios
        return productRepository.save(existingProduct);
    }


    @Transactional
    public void deleteProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto con código: " + barcode));

        productRepository.delete(product);
    }

}
