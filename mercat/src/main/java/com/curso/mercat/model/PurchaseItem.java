package com.curso.mercat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_items")
public class PurchaseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la compra que contiene esta línea
    //Muchas líneas pertenecen a una única compra (purchase) y hacen referencia a un único producto (product).
    // LAZY (carga perezosa) optimiza la memoria: la base de datos no cargará toda la información del producto
    // asociado a menos que tu código la pida explícitamente con un getter.
    @ManyToOne(fetch = FetchType.LAZY) //No cargues el producto de la base de datos todavía. Déjalo en espera y solo tráelo si realmente llego a necesitarlo.
    @JoinColumn(name = "purchase_id", nullable = false) //Indica que en la tabla física purchase_items de PostgreSQL se creará una columna de clave foránea (Foreign Key) llamada purchase_id apuntando a la tabla purchases
    @JsonIgnore //Evita el fallo común de recursión infinita. Como Purchase tiene una lista de PurchaseItem, y cada PurchaseItem apunta a su Purchase, al convertir la respuesta a JSON para enviarla al navegador entraría en un bucle sin fin
    private Purchase purchase;

    // Relación con el producto del catálogo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // 1. Constructor vacío (Obligatorio para JPA)
    public PurchaseItem() {
    }

    // 2. Constructor para instanciar una línea calculando su subtotal
    public PurchaseItem(Product product, Integer quantity) {
        this.product = product;
        this.productName = product.getName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    // 3. Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
