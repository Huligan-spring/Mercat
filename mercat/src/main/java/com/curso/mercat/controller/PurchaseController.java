package com.curso.mercat.controller;

import com.curso.mercat.dto.CheckoutRequest;
import com.curso.mercat.model.Purchase;
import com.curso.mercat.service.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchases")
@CrossOrigin(origins = "*")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Procesa la compra atómica del carrito.
     * Ejemplo: POST /api/v1/purchases
     */
    @PostMapping
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            Purchase completedPurchase = purchaseService.checkout(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(completedPurchase);
        } catch (IllegalArgumentException ex) {
            // Devuelve un error 400 limpio con el mensaje descriptivo si faltan datos o el carrito es inválido
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
