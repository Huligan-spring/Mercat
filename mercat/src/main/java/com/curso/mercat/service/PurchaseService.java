package com.curso.mercat.service;

import com.curso.mercat.dto.CartItemRequest;
import com.curso.mercat.dto.CheckoutRequest;
import com.curso.mercat.model.Product;
import com.curso.mercat.model.Purchase;
import com.curso.mercat.model.PurchaseItem;
import com.curso.mercat.repository.ProductRepository;
import com.curso.mercat.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository productRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Purchase checkout(CheckoutRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito no puede estar vacío.");
        }

        Purchase purchase = new Purchase(BigDecimal.ZERO);
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (CartItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            }

            Optional<Product> optionalProduct = productRepository.findById(itemRequest.getProductId());
            if (optionalProduct.isEmpty()) {
                throw new IllegalArgumentException("Producto no encontrado con ID: " + itemRequest.getProductId());
            }

            Product product = optionalProduct.get();

            // Construye la línea congelando nombre y precio actual
            PurchaseItem item = new PurchaseItem(product, itemRequest.getQuantity());

            // Asocia el ítem a la compra
            purchase.addItem(item);

            // Acumula el total con precisión decimal
            calculatedTotal = calculatedTotal.add(item.getSubtotal());
        }

        // Fija el total oficial verificado por el servidor
        purchase.setTotal(calculatedTotal);

        // Guarda la compra y, por cascada, todos sus PurchaseItems
        return purchaseRepository.save(purchase);
    }
}