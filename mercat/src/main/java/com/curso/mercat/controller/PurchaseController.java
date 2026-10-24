package com.curso.mercat.controller;

import com.curso.mercat.dto.CheckoutRequest;
import com.curso.mercat.model.Purchase;
import com.curso.mercat.model.User;
import com.curso.mercat.service.PurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchases")
@CrossOrigin(origins = "*")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * Procesa la compra atómica del carrito asociada al usuario autenticado.
     * Ejemplo: POST /api/v1/purchases
     */
    @PostMapping
    public ResponseEntity<?> checkout(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal User user) {
        try {
            Purchase completedPurchase = purchaseService.checkout(request, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(completedPurchase);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Devuelve el historial de compras del usuario autenticado.
     * Ejemplo: GET /api/v1/purchases
     */
    @GetMapping
    public ResponseEntity<List<Purchase>> getMyPurchases(@AuthenticationPrincipal User user) {
        List<Purchase> purchases = purchaseService.getPurchasesByUser(user);
        return ResponseEntity.ok(purchases);
    }
}