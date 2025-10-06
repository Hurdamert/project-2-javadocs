package com.example;

public class OrderItem {

    private int product_id;
    private String product_name;
    private double item_price;
    private int item_count;

    public OrderItem(int product_id, String product_name, double item_price, int item_count) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.item_price = item_price;
        this.item_count = item_count;
    }

    public int getItem_count() {
        return item_count;
    }

    public double getItem_price() {
        return item_price;
    }

    public int getProduct_id() {
        return product_id;
    }

    public String getProduct_name() {
        return product_name;
    }
}
