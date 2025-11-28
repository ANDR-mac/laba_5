package com.company.lab5.model;

import javafx.beans.property.*;

public class Product implements Priceable, Identified {
    private final StringProperty name  = new SimpleStringProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();

    public Product() {}
    public Product(String name, double price) {
        setName(name); setPrice(price);
    }

    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v); }
    public StringProperty nameProperty() { return name; }

    public double getPrice() { return price.get(); }
    public void setPrice(double v) { price.set(v); }
    public DoubleProperty priceProperty() { return price; }

    @Override
    public double getFinalPrice() { return getPrice(); }

    /* ==== перегрузка операторов (через статические методы) ==== */
    public static boolean operatorLess(Product a, Product b) {
        return a.getFinalPrice() < b.getFinalPrice();
    }
    public static Product operatorMinus(Product p, double discount) {
        return new Product(p.getName(), Math.max(0, p.getPrice() - discount));
    }

    @Override
    public String toString() { return getName() + " (" + getPrice() + ")"; }
}