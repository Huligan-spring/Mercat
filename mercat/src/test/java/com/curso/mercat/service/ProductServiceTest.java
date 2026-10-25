package com.curso.mercat.service;

import com.curso.mercat.dto.ProductRequestDTO;
import com.curso.mercat.model.Product;
import com.curso.mercat.model.Role;
import com.curso.mercat.model.User;
import com.curso.mercat.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private User standardUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        standardUser = new User("user@mercat.com", "pass123", Role.ROLE_USER);
        standardUser.setId(10L);

        adminUser = new User("admin@mercat.com", "admin123", Role.ROLE_ADMIN);
        adminUser.setId(1L);
    }

    // ---------------------------------------------------------
    // getByBarcode
    // ---------------------------------------------------------

    @Test
    @DisplayName("getByBarcode: Cuando el usuario es anónimo (invitado), busca exclusivamente en catálogo global")
    void getByBarcode_GuestUser_ShouldSearchGlobalOnly() {
        String barcode = "841000000001";
        Product globalProduct = new Product(barcode, "Leche Entera", new BigDecimal("1.20"));
        when(productRepository.findByBarcodeAndUserIsNull(barcode)).thenReturn(Optional.of(globalProduct));

        Optional<Product> result = productService.getByBarcode(barcode, null);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Leche Entera");
        verify(productRepository).findByBarcodeAndUserIsNull(barcode);
        verify(productRepository, never()).findEffectiveProduct(any(), any());
    }

    @Test
    @DisplayName("getByBarcode: Cuando el usuario está autenticado, busca con prioridad efectiva (Personal > Global)")
    void getByBarcode_AuthenticatedUser_ShouldSearchEffective() {
        String barcode = "841000000001";
        Product personalProduct = new Product(barcode, "Leche Mi Marca", new BigDecimal("1.10"));
        personalProduct.setUser(standardUser);
        when(productRepository.findEffectiveProduct(barcode, 10L)).thenReturn(Optional.of(personalProduct));

        Optional<Product> result = productService.getByBarcode(barcode, standardUser);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Leche Mi Marca");
        verify(productRepository).findEffectiveProduct(barcode, 10L);
        verify(productRepository, never()).findByBarcodeAndUserIsNull(any());
    }

    // ---------------------------------------------------------
    // searchSuggestions
    // ---------------------------------------------------------

    @Test
    @DisplayName("searchSuggestions: Retorna lista vacía si la query es nula, vacía o tiene menos de 2 caracteres")
    void searchSuggestions_ShortOrNullQuery_ShouldReturnEmptyListWithoutQueryingRepo() {
        assertThat(productService.searchSuggestions(null, standardUser)).isEmpty();
        assertThat(productService.searchSuggestions("", standardUser)).isEmpty();
        assertThat(productService.searchSuggestions(" ", standardUser)).isEmpty();
        assertThat(productService.searchSuggestions("a", standardUser)).isEmpty();

        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("searchSuggestions: En modo invitado delega en searchGlobalSuggestions")
    void searchSuggestions_GuestUser_ShouldCallSearchGlobalSuggestions() {
        Product p = new Product("84123", "Arroz Redondo", new BigDecimal("0.95"));
        when(productRepository.searchGlobalSuggestions("arr")).thenReturn(List.of(p));

        List<Product> result = productService.searchSuggestions("arr", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Arroz Redondo");
        verify(productRepository).searchGlobalSuggestions("arr");
        verify(productRepository, never()).searchEffectiveSuggestions(any(), any());
    }

    @Test
    @DisplayName("searchSuggestions: Usuario autenticado delega en searchEffectiveSuggestions")
    void searchSuggestions_AuthenticatedUser_ShouldCallSearchEffectiveSuggestions() {
        Product p = new Product("84123", "Arroz Personal", new BigDecimal("1.00"));
        when(productRepository.searchEffectiveSuggestions("arr", 10L)).thenReturn(List.of(p));

        List<Product> result = productService.searchSuggestions("arr", standardUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Arroz Personal");
        verify(productRepository).searchEffectiveSuggestions("arr", 10L);
        verify(productRepository, never()).searchGlobalSuggestions(any());
    }

    // ---------------------------------------------------------
    // upsertProduct
    // ---------------------------------------------------------

    @Test
    @DisplayName("upsertProduct: Lanza IllegalArgumentException si el código de barras es nulo o vacío")
    void upsertProduct_MissingBarcode_ShouldThrowException() {
        ProductRequestDTO dto = new ProductRequestDTO("", "Test", new BigDecimal("1.00"), false);

        assertThatThrownBy(() -> productService.upsertProduct(dto, standardUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El código de barras es obligatorio");

        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("upsertProduct: Administrador con makeGlobal=true crea un producto global nuevo (user=null)")
    void upsertProduct_AdminMakeGlobalNew_ShouldSaveWithUserNull() {
        ProductRequestDTO dto = new ProductRequestDTO("849999", "Aceite de Oliva 1L", new BigDecimal("8.50"), true);
        when(productRepository.findByBarcodeAndUserIsNull("849999")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product saved = productService.upsertProduct(dto, adminUser);

        assertThat(saved.getBarcode()).isEqualTo("849999");
        assertThat(saved.getName()).isEqualTo("Aceite de Oliva 1L");
        assertThat(saved.getPrice()).isEqualTo(new BigDecimal("8.50"));
        assertThat(saved.getUser()).isNull();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isNull();
    }

    @Test
    @DisplayName("upsertProduct: Administrador con makeGlobal=true actualiza un producto global existente")
    void upsertProduct_AdminMakeGlobalExisting_ShouldUpdateExistingGlobal() {
        Product existing = new Product("849999", "Aceite Antiguo", new BigDecimal("6.00"));
        existing.setUser(null);

        ProductRequestDTO dto = new ProductRequestDTO("849999", "Aceite Actualizado", new BigDecimal("7.50"), true);
        when(productRepository.findByBarcodeAndUserIsNull("849999")).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.upsertProduct(dto, adminUser);

        assertThat(updated.getName()).isEqualTo("Aceite Actualizado");
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("7.50"));
        assertThat(updated.getUser()).isNull();
    }

    @Test
    @DisplayName("upsertProduct: Usuario estándar hace Forking y crea copia personal si no tenía una previa")
    void upsertProduct_StandardUserNoExistingPersonal_ShouldForkAndCreatePersonalProduct() {
        ProductRequestDTO dto = new ProductRequestDTO("841111", "Café Molido Propio", new BigDecimal("2.30"), false);
        when(productRepository.findByBarcodeAndUser("841111", standardUser)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productService.upsertProduct(dto, standardUser);

        assertThat(created.getBarcode()).isEqualTo("841111");
        assertThat(created.getName()).isEqualTo("Café Molido Propio");
        assertThat(created.getPrice()).isEqualTo(new BigDecimal("2.30"));
        assertThat(created.getUser()).isEqualTo(standardUser);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("upsertProduct: Usuario estándar actualiza su copia personal si ya existía")
    void upsertProduct_StandardUserExistingPersonal_ShouldUpdateExistingPersonalProduct() {
        Product existingPersonal = new Product("841111", "Café Normal", new BigDecimal("2.00"));
        existingPersonal.setUser(standardUser);

        ProductRequestDTO dto = new ProductRequestDTO("841111", "Café Descafeinado", new BigDecimal("2.50"), false);
        when(productRepository.findByBarcodeAndUser("841111", standardUser)).thenReturn(Optional.of(existingPersonal));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.upsertProduct(dto, standardUser);

        assertThat(updated.getName()).isEqualTo("Café Descafeinado");
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("2.50"));
        assertThat(updated.getUser()).isEqualTo(standardUser);
    }

    // ---------------------------------------------------------
    // deleteProduct
    // ---------------------------------------------------------

    @Test
    @DisplayName("deleteProduct: Administrador puede borrar tanto copia global como personal si existen")
    void deleteProduct_Admin_ShouldDeleteBothGlobalAndPersonalIfPresent() {
        String barcode = "840001";
        Product globalProduct = new Product(barcode, "Global", new BigDecimal("1.00"));
        Product personalProduct = new Product(barcode, "Personal Admin", new BigDecimal("1.50"));

        when(productRepository.findByBarcodeAndUserIsNull(barcode)).thenReturn(Optional.of(globalProduct));
        when(productRepository.findByBarcodeAndUser(barcode, adminUser)).thenReturn(Optional.of(personalProduct));

        productService.deleteProduct(barcode, adminUser);

        verify(productRepository).delete(globalProduct);
        verify(productRepository).delete(personalProduct);
    }

    @Test
    @DisplayName("deleteProduct: Usuario estándar solo borra su copia personal, nunca la global")
    void deleteProduct_StandardUser_ShouldOnlyDeletePersonal() {
        String barcode = "840001";
        Product personalProduct = new Product(barcode, "Personal User", new BigDecimal("1.50"));

        when(productRepository.findByBarcodeAndUser(barcode, standardUser)).thenReturn(Optional.of(personalProduct));

        productService.deleteProduct(barcode, standardUser);

        verify(productRepository, never()).findByBarcodeAndUserIsNull(any());
        verify(productRepository).delete(personalProduct);
    }
}
