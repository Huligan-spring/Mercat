package com.curso.mercat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity // Gestiona esta clase como una tabla
@Table(name="products") //El nombre de la tabla va a ser products (si no lo ponemos también lo creará, pero se llamará "product"
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "barcode", nullable = false, length = 13)
    private String barcode;

    @Column(name = "name", nullable = false, length = 150) //nullable = false es NOT NULL en la BD
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2) //Define que admite hasta 10 dígitos en total, de los cuales 2 son decimales
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable por defecto (null = producto global)
    @JsonIgnore // <-- Evita exponer datos privados del usuario al devolver un producto
    private User user;

    // 1. Constructor vacío (Obligatorio para JPA/Hibernate)
    public Product() {
    }

    // 2. Constructor para crear productos (no necesita la id porque es autoincrement y se genera por la BD)
    public Product(String barcode, String name, BigDecimal price) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
    }



    // Getter y Setter
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
