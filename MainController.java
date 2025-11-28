package com.company.lab5.controllers;

import com.company.lab5.model.Product;
import com.company.lab5.model.DiscountProduct;
import com.company.lab5.model.Shop;
import com.company.lab5.utils.FileRepo;
import com.company.lab5.utils.DbManager;
import com.company.lab5.utils.ExportDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;

public class MainController {

    @FXML private TableView<Product> table;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Number> colPrice, colDiscount, colFinal;

    private final ObservableList<Product> items = FXCollections.observableArrayList();
    private final Shop shop = new Shop();

    /* -------------------- инициализация -------------------- */
    @FXML
    private void initialize() {
        table.setItems(items);
        colName.setCellValueFactory(d -> d.getValue().nameProperty());
        colPrice.setCellValueFactory(d -> d.getValue().priceProperty());

        colDiscount.setCellFactory(tc -> new TableCell<Product, Number>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? "" :
                        (v.doubleValue() == 0 ? "-" : String.format("%.0f%%", v.doubleValue())));
            }
        });
        colDiscount.setCellValueFactory(d -> {
            if (d.getValue() instanceof DiscountProduct dp)
                return new javafx.beans.property.SimpleDoubleProperty(dp.getDiscount());
            return new javafx.beans.property.SimpleDoubleProperty(0);
        });

        colFinal.setCellValueFactory(d ->
                new javafx.beans.property.ReadOnlyDoubleWrapper(d.getValue().getFinalPrice()));

        /* загружаем данные из БД при старте */
        items.setAll(DbManager.INSTANCE.selectAll());
        System.out.println(">>> initialize: загружено из БД = " + items.size());
    }

    /* -------------------- CRUD -------------------- */
    @FXML
    private void onAddRegular() {
        Product p = new Product("", 0);
        openEditor(p, "Добавить товар");
        System.out.println(">>> onAddRegular: после редактора = " + p.getName() + " / " + p.getPrice());
    }

    @FXML
    private void onAddDiscount() {
        Product p = new DiscountProduct("", 0, 0);
        openEditor(p, "Добавить со скидкой");
        System.out.println(">>> onAddDiscount: после редактора = " + p.getName() + " / " + p.getPrice());
    }

    @FXML
    private void onEdit() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Выберите товар");
            return;
        }
        openEditor(selected, "Изменить товар");
    }

    @FXML
    private void onRemove() {
        Product selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        items.remove(selected);
        shop.getProducts().remove(selected);
        /* перезаписываем БД упрощённо */
        DbManager.INSTANCE.clearAll();
        for (Product p : items) DbManager.INSTANCE.insert(p);
        System.out.println(">>> onRemove: перезаписано = " + items.size());
    }

    /* -------------------- Экспорт / Импорт БД ↔ файл -------------------- */
    @FXML
    private void onExport() {        // «Экспорт БД в файл»
        List<Product> data = DbManager.INSTANCE.selectAll();
        System.out.println(">>> onExport: размер data = " + data.size());
        for (Product p : data) {
            System.out.println("  - " + p.getName() + " / " + p.getPrice());
        }

        FileChooser ch = new FileChooser();
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = ch.showSaveDialog(table.getScene().getWindow());
        if (f == null) return;
        try {
            System.out.println(">>> выбран путь для экспорта: " + f.toPath().toAbsolutePath());
            System.out.println(">>> перед FileRepo.write: " + data);

            // --- Сериализуем в DTO, чтобы избежать проблем с JavaFX properties ---
            List<ExportDTO> dto = data.stream()
                    .map(ExportDTO::fromProduct)
                    .collect(Collectors.toList());

            FileRepo.writeObject(f.toPath(), dto);

            // Дополнительная проверка: прочитать файл сразу и вывести длину (отладка)
            try {
                String after = Files.readString(f.toPath());
                System.out.println(">>> после записи длина = " + after.length());
                System.out.println(">>> начало файла: " + (after.length() > 200 ? after.substring(0,200) : after));
            } catch (Exception ex) {
                System.out.println(">>> не удалось прочитать файл для проверки: " + ex.getMessage());
            }

            alert("Экспорт БД завершён: " + dto.size() + " записей");
        } catch (IOException e) {
            alert("Ошибка экспорта: " + e.getMessage());
        }
    }

    @FXML
    private void onImport() {        // «Импорт в БД из файла»
        FileChooser ch = new FileChooser();
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = ch.showOpenDialog(table.getScene().getWindow());
        if (f == null) return;
        try {
            // Попробуем сначала читать как список DTO (советую экспортировать в том же формате)
            List<ExportDTO> loadedDto = FileRepo.readAs(ExportDTO[].class, f.toPath());
            if (loadedDto != null && !loadedDto.isEmpty()) {
                DbManager.INSTANCE.clearAll();
                for (ExportDTO d : loadedDto) {
                    Product p;
                    if (d.discount != 0) {
                        p = new DiscountProduct(d.name, d.price, d.discount);
                    } else {
                        p = new Product(d.name, d.price);
                    }
                    DbManager.INSTANCE.insert(p);
                }
                items.setAll(DbManager.INSTANCE.selectAll());
                System.out.println(">>> onImport: импортировано (DTO) = " + loadedDto.size());
                alert("Импорт в БД завершён: " + loadedDto.size() + " записей");
                return;
            }

            // Если DTO не обнаружены — fallback: читать как List<Product> (на всякий случай)
            List<Product> loaded = FileRepo.read(f.toPath());
            DbManager.INSTANCE.clearAll();
            for (Product p : loaded) DbManager.INSTANCE.insert(p);
            items.setAll(DbManager.INSTANCE.selectAll());
            System.out.println(">>> onImport: импортировано = " + loaded.size());
            alert("Импорт в БД завершён: " + loaded.size() + " записей");
        } catch (IOException e) {
            alert("Ошибка импорта: " + e.getMessage());
        }
    }

    /* -------------------- старые Save/Load (если нужны) -------------------- */
    @FXML
    private void onSave() {
        FileChooser ch = new FileChooser();
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = ch.showSaveDialog(table.getScene().getWindow());
        if (f == null) return;
        try {
            FileRepo.write(f.toPath(), new ArrayList<>(items));
            alert("Сохранено: " + items.size() + " записей");
        } catch (IOException e) {
            alert("Ошибка сохранения: " + e.getMessage());
        }
    }

    @FXML
    private void onLoad() {
        FileChooser ch = new FileChooser();
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File f = ch.showOpenDialog(table.getScene().getWindow());
        if (f == null) return;
        try {
            List<Product> loaded = FileRepo.read(f.toPath());
            items.setAll(loaded);
            shop.getProducts().clear();
            shop.getProducts().addAll(loaded);
            alert("Загружено: " + loaded.size() + " записей");
        } catch (IOException e) {
            alert("Ошибка загрузки: " + e.getMessage());
        }
    }

    /* -------------------- сортировка -------------------- */
    @FXML
    private void onSort() {
        FXCollections.sort(items, Comparator.comparingDouble(Product::getFinalPrice));
    }

    /* -------------------- редактор (2-е окно) -------------------- */
    private void openEditor(Product product, String title) {
        try {
            String fxml = (product instanceof DiscountProduct)
                    ? "/editor_discount.fxml"
                    : "/editor_regular.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(loader.load()));

            EditorController ec = loader.getController();
            ec.setProduct(product);
            ec.setMode(title);
            stage.showAndWait();

            if (ec.isSaved()) {
                if (!items.contains(product)) {        // новый
                    items.add(product);
                    shop.getProducts().add(product);
                    DbManager.INSTANCE.insert(product);   // ← в БД
                    System.out.println(">>> openEditor: INSERT в БД = " + product.getName());
                }
                /* перезаписываем всю БД после редактирования */
                DbManager.INSTANCE.clearAll();
                for (Product p : items) DbManager.INSTANCE.insert(p);
                System.out.println(">>> openEditor: перезаписано = " + items.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            alert("Не удалось открыть окно: " + e.getMessage());
        }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}