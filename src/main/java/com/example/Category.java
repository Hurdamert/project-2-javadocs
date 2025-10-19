package com.example;

public class Category {
    private final int category_id;
    private final String category_name;

    public Category(int id, String name) {
        this.category_id = id;
        this.category_name = name;
    }

    public int getId() { return category_id; }
    public String getName() { return category_name; }

    @Override
    public String toString() {
        // This determines what text shows in the ComboBox dropdown
        return category_name;
    }
}
