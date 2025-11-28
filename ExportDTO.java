package com.company.lab5.utils;

import com.company.lab5.model.Product;
import com.company.lab5.model.DiscountProduct;

public class ExportDTO {
    public String name;
    public double price;
    public double discount;

    public static ExportDTO fromProduct(Product p) {
        ExportDTO d = new ExportDTO();
        d.name = p.getName();
        d.price = p.getPrice();
        d.discount = (p instanceof DiscountProduct dp) ? dp.getDiscount() : 0;
        return d;
    }
}