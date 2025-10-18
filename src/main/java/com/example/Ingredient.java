package com.example;

public class Ingredient {
    private final int ingredient_id;
    private final String ingredient_name;
    private final String ingredient_unit;

    public Ingredient(int id, String name, String unit) {
        this.ingredient_id = id;
        this.ingredient_name = name;
        this.ingredient_unit = unit;
    }

    public int getId() { return ingredient_id; }
    public String getName() { return ingredient_name; }

    @Override
    public String toString() {
        // This determines what text shows in the ComboBox dropdown
        return ingredient_name + " (" + ingredient_unit + ")";
    }
}
