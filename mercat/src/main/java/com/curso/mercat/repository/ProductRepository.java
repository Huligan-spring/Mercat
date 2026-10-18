package com.curso.mercat.repository;

import com.curso.mercat.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring genera automáticamente: SELECT * FROM products WHERE barcode = ?
    Optional<Product> findByBarcode(String barcode);

    // "Top6": Trae como máximo 6 sugerencias cuando se busca un producto escribiendo letra por letra
    // "ContainingIgnoreCase": Equivalente a WHERE LOWER(name) LIKE '%texto%'
    List<Product> findTop6ByNameContainingIgnoreCase(String name);

}
