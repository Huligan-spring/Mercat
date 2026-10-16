package com.curso.mercat.repository;

import com.curso.mercat.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring genera automáticamente: SELECT * FROM products WHERE barcode = ?
    Optional<Product> findByBarcode(String barcode);

}
