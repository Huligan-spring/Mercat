package com.curso.mercat.service;

import com.curso.mercat.dto.CartItemRequest;
import com.curso.mercat.dto.CheckoutRequest;
import com.curso.mercat.model.Product;
import com.curso.mercat.model.Purchase;
import com.curso.mercat.model.PurchaseItem;
import com.curso.mercat.model.Role;
import com.curso.mercat.model.User;
import com.curso.mercat.repository.ProductRepository;
import com.curso.mercat.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseService purchaseService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("shopper@mercat.com", "secret", Role.ROLE_USER);
        testUser.setId(5L);
    }

    // ---------------------------------------------------------
    // checkout validations
    // ---------------------------------------------------------

    @Test
    @DisplayName("checkout: Lanza IllegalArgumentException si el request es nulo")
    void checkout_NullRequest_ShouldThrowException() {
        assertThatThrownBy(() -> purchaseService.checkout(null, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El carrito no puede estar vacío");

        verifyNoInteractions(purchaseRepository);
    }

    @Test
    @DisplayName("checkout: Lanza IllegalArgumentException si la lista de items es nula o vacía")
    void checkout_EmptyCart_ShouldThrowException() {
        CheckoutRequest nullItems = new CheckoutRequest(null);
        assertThatThrownBy(() -> purchaseService.checkout(nullItems, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El carrito no puede estar vacío");

        CheckoutRequest emptyItems = new CheckoutRequest(Collections.emptyList());
        assertThatThrownBy(() -> purchaseService.checkout(emptyItems, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El carrito no puede estar vacío");

        verifyNoInteractions(purchaseRepository);
    }

    @Test
    @DisplayName("checkout: Lanza IllegalArgumentException si un ítem tiene cantidad nula o menor/igual a cero")
    void checkout_InvalidQuantity_ShouldThrowException() {
        CheckoutRequest requestZero = new CheckoutRequest(List.of(
                new CartItemRequest(1L, 0)
        ));
        assertThatThrownBy(() -> purchaseService.checkout(requestZero, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La cantidad debe ser mayor a cero");

        CheckoutRequest requestNegative = new CheckoutRequest(List.of(
                new CartItemRequest(1L, -2)
        ));
        assertThatThrownBy(() -> purchaseService.checkout(requestNegative, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La cantidad debe ser mayor a cero");

        CheckoutRequest requestNullQty = new CheckoutRequest(List.of(
                new CartItemRequest(1L, null)
        ));
        assertThatThrownBy(() -> purchaseService.checkout(requestNullQty, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La cantidad debe ser mayor a cero");

        verifyNoInteractions(purchaseRepository);
    }

    @Test
    @DisplayName("checkout: Lanza IllegalArgumentException si un producto no existe en el catálogo")
    void checkout_ProductNotFound_ShouldThrowException() {
        CheckoutRequest request = new CheckoutRequest(List.of(
                new CartItemRequest(999L, 2)
        ));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.checkout(request, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Producto no encontrado con ID: 999");

        verify(purchaseRepository, never()).save(any());
    }

    // ---------------------------------------------------------
    // checkout success
    // ---------------------------------------------------------

    @Test
    @DisplayName("checkout: Compra exitosa calcula total exacto, congela precios y asocia al usuario")
    void checkout_Success_ShouldFreezePricesCalculateTotalAndSave() {
        Product p1 = new Product("841001", "Manzana Golden", new BigDecimal("1.50"));
        p1.setId(1L);
        Product p2 = new Product("841002", "Detergente Líquido", new BigDecimal("4.25"));
        p2.setId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(p2));

        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckoutRequest request = new CheckoutRequest(List.of(
                new CartItemRequest(1L, 3), // 3 * 1.50 = 4.50
                new CartItemRequest(2L, 2)  // 2 * 4.25 = 8.50 -> Total = 13.00
        ));

        Purchase purchase = purchaseService.checkout(request, testUser);

        assertThat(purchase).isNotNull();
        assertThat(purchase.getUser()).isEqualTo(testUser);
        assertThat(purchase.getTotal()).isEqualByComparingTo(new BigDecimal("13.00"));
        assertThat(purchase.getItems()).hasSize(2);

        // Validar congelación inmutable de líneas de compra
        PurchaseItem item1 = purchase.getItems().get(0);
        assertThat(item1.getProductName()).isEqualTo("Manzana Golden");
        assertThat(item1.getUnitPrice()).isEqualByComparingTo(new BigDecimal("1.50"));
        assertThat(item1.getQuantity()).isEqualTo(3);
        assertThat(item1.getSubtotal()).isEqualByComparingTo(new BigDecimal("4.50"));

        PurchaseItem item2 = purchase.getItems().get(1);
        assertThat(item2.getProductName()).isEqualTo("Detergente Líquido");
        assertThat(item2.getUnitPrice()).isEqualByComparingTo(new BigDecimal("4.25"));
        assertThat(item2.getQuantity()).isEqualTo(2);
        assertThat(item2.getSubtotal()).isEqualByComparingTo(new BigDecimal("8.50"));

        ArgumentCaptor<Purchase> captor = ArgumentCaptor.forClass(Purchase.class);
        verify(purchaseRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getId()).isEqualTo(5L);
        assertThat(captor.getValue().getTotal()).isEqualByComparingTo(new BigDecimal("13.00"));
    }

    // ---------------------------------------------------------
    // getPurchasesByUser
    // ---------------------------------------------------------

    @Test
    @DisplayName("getPurchasesByUser: Consulta el historial de compras exclusivo del usuario ordenado por fecha desc")
    void getPurchasesByUser_ShouldReturnUserPurchasesOrdered() {
        Purchase p1 = new Purchase(new BigDecimal("15.50"));
        p1.setUser(testUser);
        Purchase p2 = new Purchase(new BigDecimal("30.00"));
        p2.setUser(testUser);

        when(purchaseRepository.findByUserIdOrderByPurchasedAtDesc(5L)).thenReturn(List.of(p2, p1));

        List<Purchase> result = purchaseService.getPurchasesByUser(testUser);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTotal()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(result.get(1).getTotal()).isEqualByComparingTo(new BigDecimal("15.50"));
        verify(purchaseRepository).findByUserIdOrderByPurchasedAtDesc(5L);
    }
}
