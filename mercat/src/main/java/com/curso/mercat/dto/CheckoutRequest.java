package com.curso.mercat.dto;

import java.util.List;

public class CheckoutRequest {

    private List<CartItemRequest> items;

    public CheckoutRequest() {
    }

    public CheckoutRequest(List<CartItemRequest> items) {
        this.items = items;
    }

    public List<CartItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CartItemRequest> items) {
        this.items = items;
    }
}