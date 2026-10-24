package com.curso.mercat.dto;

import java.math.BigDecimal;

public class ProductRequestDTO {
    private String barcode;
    private String name;
    private BigDecimal price;
    private boolean makeGlobal; // Solo tendrá efecto si el usuario es ROLE_ADMIN

    public ProductRequestDTO() {}

    public ProductRequestDTO(String barcode, String name, BigDecimal price, boolean makeGlobal) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.makeGlobal = makeGlobal;
    }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isMakeGlobal() { return makeGlobal; }
    public void setMakeGlobal(boolean makeGlobal) { this.makeGlobal = makeGlobal; }
}