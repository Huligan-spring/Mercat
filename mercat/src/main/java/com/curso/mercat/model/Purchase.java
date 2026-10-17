package com.curso.mercat.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchased_at", nullable = false) //el estándar para representar fecha y hora junto con la zona horaria/desplazamiento UTC (por ejemplo, 2026-10-16T21:59:00+02:00)
    private OffsetDateTime purchasedAt;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // Relación bidireccional con las líneas de compra
    //mappedBy = "purchase": Indica que la clave foránea real no está en la tabla purchases, sino en el campo purchase_id dentro de la clase PurchaseItem
    //cascade = CascadeType.ALL: Fundamental para transacciones atómicas.
    //orphanRemoval = true: Si eliminas un elemento de la lista items en Java, Hibernate generará un DELETE en la base de datos para borrar esa fila huérfana.
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items = new ArrayList<>();


    // 1. Constructor vacío (Obligatorio para JPA)
    public Purchase() {
    }

    // 2. Constructor para inicializar una compra
    public Purchase(BigDecimal total) {
        this.purchasedAt = OffsetDateTime.now();
        this.total = total;
    }

    // 3. Método auxiliar de conveniencia para añadir líneas de compra
    //Al ser una relación bidireccional, es una buena práctica mantener sincronizados ambos lados del vínculo en memoria:
    // añade el elemento a la lista local y, a su vez, le asigna a ese item su referencia a esta compra (item.setPurchase(this)).
    public void addItem(PurchaseItem item) {
        items.add(item);
        item.setPurchase(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(OffsetDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }
}
