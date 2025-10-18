package com.example;

public class ProductIngredient {
    private final int ingredient_id;
    private final String ingredient_name;
    private final int amount;

    public ProductIngredient(int ingredient_id, String ingredient_name, int amount) {
        this.ingredient_id = ingredient_id;
        this.ingredient_name = ingredient_name;
        this.amount = amount;
    }

    public int getIngredientId() { return ingredient_id; }
    public String getIngredientName() { return ingredient_name; }
    public int getAmount() { return amount; }
}
