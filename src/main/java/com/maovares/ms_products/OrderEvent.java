package com.maovares.ms_products;

import java.util.List;

public record OrderEvent(
        String orderId,
        String customerEmail,
        double total,
        List<Item> items
) {
    public record Item(String sku, int qty) {}
}
