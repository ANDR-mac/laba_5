package com.company.lab5.model;

import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Path;
import com.company.lab5.utils.FileRepo;

public class Shop {
    private final List<Product> products = new ArrayList<>();

    /* добавление любого товара (объект как аргумент функции) */
    public void addProduct(Product p) { products.add(p); }

    /* поиск товара с минимальной ценой (со скидкой) */
    public Optional<Product> findCheapest() {
        return products.stream()
                .min(Comparator.comparingDouble(Product::getFinalPrice));
    }

    public List<Product> getProducts() { return products; }

    public void saveToFile(Path file) throws IOException {
        FileRepo.write(file, products);
    }
    public void loadFromFile(Path file) throws IOException {
        products.clear();
        products.addAll(FileRepo.read(file));
    }
}