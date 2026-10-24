package com.curso.mercat.repository;

import com.curso.mercat.model.Product;
import com.curso.mercat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Para usuarios no autenticados (Modo Invitado): solo catálogo maestro
    Optional<Product> findByBarcodeAndUserIsNull(String barcode);

    // 2. Para usuarios autenticados: prioriza el producto propio, si no, toma el global
    @Query(value = """
        SELECT * FROM products 
        WHERE barcode = :barcode 
          AND (user_id = :userId OR user_id IS NULL)
        ORDER BY user_id DESC NULLS LAST
        LIMIT 1
        """, nativeQuery = true)
    Optional<Product> findEffectiveProduct(
            @Param("barcode") String barcode,
            @Param("userId") Long userId
    );

    // 3. Autocompletado/Sugerencias para invitados (solo globales)
    @Query(value = """
        SELECT * FROM products
        WHERE user_id IS NULL
          AND (LOWER(name) LIKE LOWER(CONCAT('%', :query, '%')) OR barcode LIKE CONCAT('%', :query, '%'))
        LIMIT 6
        """, nativeQuery = true)
    List<Product> searchGlobalSuggestions(@Param("query") String query);

    // 4. Autocompletado/Sugerencias para usuarios autenticados:
    // Combina productos personales y globales sin duplicar códigos de barras (DISTINCT ON)
    @Query(value = """
        SELECT DISTINCT ON (barcode) * FROM products
        WHERE (user_id = :userId OR user_id IS NULL)
          AND (LOWER(name) LIKE LOWER(CONCAT('%', :query, '%')) OR barcode LIKE CONCAT('%', :query, '%'))
        ORDER BY barcode, user_id DESC NULLS LAST
        LIMIT 6
        """, nativeQuery = true)
    List<Product> searchEffectiveSuggestions(
            @Param("query") String query,
            @Param("userId") Long userId
    );

    // Búsqueda auxiliar para saber si un usuario ya tiene su copia personalizada
    Optional<Product> findByBarcodeAndUser(String barcode, User user);
}
