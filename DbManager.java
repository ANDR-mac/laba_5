package com.company.lab5.utils;

import com.company.lab5.model.Product;
import com.company.lab5.model.DiscountProduct;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public enum DbManager {
    INSTANCE;
    private final Connection conn;

    DbManager() {
        try {
            String url = "jdbc:sqlite:shop.db";
            conn = DriverManager.getConnection(url);
            initSchema();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void initSchema() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS products(
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                name     TEXT    NOT NULL,
                price    REAL    NOT NULL,
                discount REAL    DEFAULT 0
            );
            """;
        conn.createStatement().execute(sql);
    }

    public void insert(Product p) {
        String sql = "INSERT INTO products(name,price,discount) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setDouble(3, (p instanceof DiscountProduct dp) ? dp.getDiscount() : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Product> selectAll() {
        String sql = "SELECT * FROM products";
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<Product> list = new ArrayList<>();
            while (rs.next()) {
                double d = rs.getDouble("discount");
                Product p = (d == 0)
                        ? new Product(rs.getString("name"), rs.getDouble("price"))
                        : new DiscountProduct(rs.getString("name"), rs.getDouble("price"), d);
                list.add(p);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearAll() {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM products");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}