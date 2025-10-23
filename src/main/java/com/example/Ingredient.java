package com.example;

/**
  * The Ingredient class creates an abstraction of an ingredient that
  * is used to make a product at a boba shop
  * @author Evan Ganske
*/
public class Ingredient {
    private final int ingredient_id;
    private final String ingredient_name;
    private final String ingredient_unit;

    /**
    * This is the constructor for creating a new Ingredient
    *
    *@param id The unique ingredient ID
    *@param name The name of the ingredient
    *@param unit The measurable unit that this ingredient is stored in (g or ml)
    */
    public Ingredient(int id, String name, String unit) {
        this.ingredient_id = id;
        this.ingredient_name = name;
        this.ingredient_unit = unit;
    }

    /**
    * Public getter method to return the ingredient_id of the Ingredient
    * @return The ingredients id
    */ 
    public int getId() { return ingredient_id; }

    /**
    * Public getter method to return the ingredient_name of the Ingredient
    * @return The ingredients name
    */ 
    public String getName() { return ingredient_name; }

    /**
    * Override of the toString() default method
    * This is used to make sure that the correct text is show in the ingredient drop-down box
    * @return The ingredients name and unit type
    */ 
    @Override
    public String toString() {
        // This determines what text shows in the ComboBox dropdown
        return ingredient_name + " (" + ingredient_unit + ")";
    }
}
