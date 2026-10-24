package com.curso.mercat.controller;

import com.curso.mercat.dto.ProductRequestDTO;
import com.curso.mercat.model.Product;
import com.curso.mercat.model.User;
import com.curso.mercat.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Escanear código de barras (Abierto a invitados y usuarios logueados)
    @GetMapping("/by-barcode/{code}")
    public ResponseEntity<Product> getByBarcode(
            @PathVariable String code,
            @AuthenticationPrincipal User user) {
        return productService.getByBarcode(code, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Búsqueda predictiva (Abierto a invitados y usuarios logueados)
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchSuggestions(
            @RequestParam("q") String query,
            @AuthenticationPrincipal User user) {
        List<Product> results = productService.searchSuggestions(query, user);
        return ResponseEntity.ok(results);
    }

    // Crear o modificar producto (Requiere login)
    @PostMapping
    public ResponseEntity<?> upsertProduct(
            @RequestBody ProductRequestDTO dto,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Se requiere iniciar sesión para guardar productos.");
        }
        try {
            Product saved = productService.upsertProduct(dto, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Eliminar producto por código de barras (Requiere login)
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String code,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        productService.deleteProduct(code, user);
        return ResponseEntity.noContent().build();
    }
}