package com.company.lab5.controllers;

import com.company.lab5.model.Product;
import com.company.lab5.model.DiscountProduct;
import com.company.lab5.utils.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class EditorController {

    @FXML private TextField name;
    @FXML private TextField price;
    @FXML private TextField discount;   // может быть null для обычного товара
    @FXML private Button saveBtn;

    private Product product;
    private boolean saved = false;

    /* ====== автоматический вызов JavaFX ====== */
    @FXML
    private void initialize() {
        // если в FXML нет поля discount – оно останется null
        if (discount != null) {
            discount.setVisible(true);
            discount.setManaged(true);
        }
    }

    /* ====== перегрузка «конструктора» через setMode ====== */
    public void setMode(String mode) {
        boolean ro = "Просмотр".equals(mode);
        saveBtn.setVisible(!ro);
        name.setEditable(!ro);
        price.setEditable(!ro);
        if (discount != null) discount.setEditable(!ro);
    }

    public void setProduct(Product p) {
        this.product = p;

        /* ==== для нового товара показываем только promptText ==== */
        if (p.getName().isEmpty() && p.getPrice() == 0) {
            name.clear();          // поле пустое → promptText виден
            price.clear();         // поле пустое → promptText виден
            if (discount != null) discount.clear();
            return;
        }

        /* ==== для существующего – подставляем данные ==== */
        name.setText(p.getName());
        price.setText(String.valueOf(p.getPrice()));
        if (discount != null) {
            if (p instanceof DiscountProduct dp) {
                discount.setText(String.valueOf(dp.getDiscount()));
            } else {
                discount.setText("0");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }

    /* ====== обработчики кнопок ====== */
    @FXML
    private void onSave() {
        /* валидация общих полей */
        if (!Validator.name(name.getText())) {
            alert("Название 2-60 символов");
            return;
        }
        if (!Validator.price(price.getText())) {
            alert("Цена 0.01 – 999 999");
            return;
        }

        /* валидация скидки (только если поле есть) */
        if (discount != null) {
            if (!Validator.discount(discount.getText())) {
                alert("Скидка 0 – 99 %");
                return;
            }
            double d = Double.parseDouble(discount.getText());
            if (product instanceof DiscountProduct dp) {
                dp.setDiscount(d);          // сохраняем новую скидку
            }
        }

        /* общие поля */
        product.setName(name.getText());
        product.setPrice(Double.parseDouble(price.getText()));
        saved = true;
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    /* ====== утилиты ====== */
    private void close() {
        ((Stage) name.getScene().getWindow()).close();
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}