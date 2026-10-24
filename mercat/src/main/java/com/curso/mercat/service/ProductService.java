package com.curso.mercat.service;

import com.curso.mercat.dto.ProductRequestDTO;
import com.curso.mercat.model.Product;
import com.curso.mercat.model.Role;
import com.curso.mercat.model.User;
import com.curso.mercat.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Búsqueda por código de barras con soporte híbrido / invitado
    @Transactional(readOnly = true)
    public Optional<Product> getByBarcode(String barcode, User user) {
        if (user != null) {
            return productRepository.findEffectiveProduct(barcode, user.getId());
        }
        return productRepository.findByBarcodeAndUserIsNull(barcode);
    }

    // Sugerencias de autocompletado híbridas / invitado
    @Transactional(readOnly = true)
    public List<Product> searchSuggestions(String query, User user) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }
        if (user != null) {
            return productRepository.searchEffectiveSuggestions(query.trim(), user.getId());
        }
        return productRepository.searchGlobalSuggestions(query.trim());
    }

    // Creación o modificación inteligente (Forking)
    @Transactional
    public Product upsertProduct(ProductRequestDTO dto, User user) {
        if (dto.getBarcode() == null || dto.getBarcode().isBlank()) {
            throw new IllegalArgumentException("El código de barras es obligatorio.");
        }

        // Caso 1: Administrador creando o modificando producto global
        if (user.getRole() == Role.ROLE_ADMIN && dto.isMakeGlobal()) {
            Product globalProduct = productRepository.findByBarcodeAndUserIsNull(dto.getBarcode())
                    .orElse(new Product());
            globalProduct.setBarcode(dto.getBarcode());
            globalProduct.setName(dto.getName());
            globalProduct.setPrice(dto.getPrice());
            globalProduct.setUser(null); // Asegura que sea global
            return productRepository.save(globalProduct);
        }

        // Caso 2: Usuario estándar (o admin guardando copia privada)
        Optional<Product> personalProduct = productRepository.findByBarcodeAndUser(dto.getBarcode(), user);

        if (personalProduct.isPresent()) {
            // Ya tiene copia personal: la actualiza
            Product p = personalProduct.get();
            p.setName(dto.getName());
            p.setPrice(dto.getPrice());
            return productRepository.save(p);
        } else {
            // No tiene copia personal: crea una nueva vinculada a su cuenta (Forking)
            Product newPersonal = new Product();
            newPersonal.setBarcode(dto.getBarcode());
            newPersonal.setName(dto.getName());
            newPersonal.setPrice(dto.getPrice());
            newPersonal.setUser(user);
            return productRepository.save(newPersonal);
        }
    }

    // Eliminación segura de productos
    @Transactional
    public void deleteProduct(String barcode, User user) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            // Un admin puede borrar tanto la copia global como una personal
            productRepository.findByBarcodeAndUserIsNull(barcode)
                    .ifPresent(productRepository::delete);
        }
        // Un usuario estándar solo puede borrar su propia versión personalizada
        productRepository.findByBarcodeAndUser(barcode, user)
                .ifPresent(productRepository::delete);
    }
}