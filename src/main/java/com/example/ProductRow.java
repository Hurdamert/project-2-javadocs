package com.example;

public class ProductRow {
    private final int productId;
    private final String productName;
    private final float productPrice;

    public ProductRow(int productId, String productName, float productPrice) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
    }

    public int getProductId() {
        return productId;
    }
    public String getProductName() {
        return productName;
    }
    public float getProductPrice() {
        return productPrice;
    }
}

