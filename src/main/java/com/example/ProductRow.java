package com.example;

/**
  * The ProductRow class gets the product information from the database,
  * allowing it to be put into the product table
  * @author Jake Hewett
*/

public class ProductRow {
    private final int productId;
    private final String productName;
    private final float productPrice;
    /**
    * This is the constructor for creating a new ProductRow
    *
    *@param productId The unique product ID
    *@param productName The name of the product
    *@param productPrice The price of the product
    */
    public ProductRow(int productId, String productName, float productPrice) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
    }

    /**
    * This method is to get the unique product ID
    * @return return the unique product ID
    */
    public int getProductId() {
        return productId;
    }

    /**
    * This method is to get the name of the product
    * @return return the name of the product
    */
    public String getProductName() {
        return productName;
    }

    /**
    * This method is to get the price of the product
    * @return return the price of the product
    */
    public float getProductPrice() {
        return productPrice;
    }
}

