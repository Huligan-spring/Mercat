package com.curso.mercat.controller;

import com.curso.mercat.model.Product;
import com.curso.mercat.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*") // Permite peticiones desde la PWA en desarrollo y GitHub Pages
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Busca un producto por su código de barras escaneado.
     * Ejemplo: GET /api/v1/products/by-barcode/8410100012345
     */
    @GetMapping("/by-barcode/{barcode}")
    public ResponseEntity<Product> getProductByBarcode(@PathVariable String barcode) {
        return productService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(){
        List<Product> products = productService.findAllProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{barcode}")
    public ResponseEntity<?> updateProduct(@PathVariable String barcode, @RequestBody Product productData) {
        try {
            Product updatedProduct = productService.updateProduct(barcode, productData);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            // Si el mensaje indica que no se encontró, devolvemos 404; si es colisión de barcode, 400
            if (e.getMessage().contains("No se encontró")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{barcode}")
    public ResponseEntity<?> deleteProduct(@PathVariable String barcode) {
        try {
            productService.deleteProductByBarcode(barcode);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "No se puede eliminar el producto porque está asociado a compras existentes."
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchSuggestions(@RequestParam("q") String query) {
        List<Product> suggestions = productService.getSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }
}
