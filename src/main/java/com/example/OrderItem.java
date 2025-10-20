package com.example;


/**
  * The OrderItems class creates an abstraction of an order that can be made when 
  * a customer orders an item at the boba shop
  * @author Licheng Yi
*/
public class OrderItem {


   private int product_id;
   private String product_name;
   private double item_price;
   private int item_count;
   /**
   * This is the constructor for creating a new OrderItem
   *
   *@param product_id The unique product ID
   *@param product_name The name of the product
   *@param item_price The price of the product
   *@param item_count The number of products to order
   */
   public OrderItem(int product_id, String product_name, double item_price, int item_count) {
       this.product_id = product_id;
       this.product_name = product_name;
       this.item_price = item_price;
       this.item_count = item_count;
   }
   
   /**
   * This method is to get how many stocks for this specific item
   * @return return the amount of the item
   */
   public int getItem_count() {
       return item_count;
   }


   /**
   * Public getter method to return the item_price of the OrderItem
   * @return The OrderItem’s price
   */
   public double getItem_price() {
       return item_price;
   }
   /**
     * Retrieves the unique product ID.
     * @return The product's unique identifier.
     */
   public int getProduct_id() {
       return product_id;
   }


   /**
     * Retrieves the product name.
     * @return The name of the product.
     */
   public String getProduct_name() {
       return product_name;
   }
}
