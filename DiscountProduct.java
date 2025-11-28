package com.company.lab5.model;

public class DiscountProduct extends Product {
    private double discount;   // 0..100 %

    public DiscountProduct(String name, double price, double discount) {
        super(name, price);
        this.discount = discount;
    }
    public double getDiscount() { return discount; }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    @Override
    public double getFinalPrice() {
        return getPrice() * (1 - discount / 100.0);
    }

    @Override
    public String toString() {
        return super.toString() + " –" + discount + "%";
    }
}