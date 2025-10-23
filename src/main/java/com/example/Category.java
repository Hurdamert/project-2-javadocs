package com.example;

/**
 * The Category class represents a product category in the POS system.
 * It holds a unique category ID and name for display and database use.
 * 
 * @author Sahil Kasturi
 */
public class Category {
    private final int category_id;
    private final String category_name;

    /**
     * This is the constructor for creating a new Category.
     * 
     * @param id   The unique ID of the category
     * @param name The name of the category
     */
    public Category(int id, String name) {
        this.category_id = id;
        this.category_name = name;
    }

    /**
     * Retrieves the unique ID of the category.
     * 
     * @return The category ID
     */
    public int getId() {
        return category_id;
    }

    /**
     * Retrieves the name of the category.
     * 
     * @return The category name
     */
    public String getName() {
        return category_name;
    }

    /**
     * Returns the category name as a string for display in dropdowns or lists.
     * 
     * @return The category name
     */
    @Override
    public String toString() {
        return category_name;
    }
}
