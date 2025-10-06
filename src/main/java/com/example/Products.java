package com.example;

public class Products{
    private String name;
    private double price;
    private int id;
    private int category;
    private int qty;

    public Products(String name, double price, int id, int category, int qty){
        this.name = name;
        this.price = price;
        this.id = id;
        this.category = category;
        this.qty = qty;
    }

    //getters
    public String getName(){
        return name;
    }
    
    public double getPrice(){
        return price;
    }

    public int getID(){
        return id;
    }

    public int getCategory(){
        return category;
    }

    public int qty(){
        return qty;
    }

    //set quantity when selecting items
    public void setQTY(int qty){
        this.qty = qty;
    }

}