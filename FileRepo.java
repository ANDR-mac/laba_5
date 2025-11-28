package com.company.lab5.utils;

import com.company.lab5.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.List;

public final class FileRepo {
    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<List<Product>>(){}.getType();

    /* ---------- запись JSON в файл (старый метод, оставляем) ---------- */
    public static void write(Path file, List<Product> list) throws IOException {
        // ========== ОТЛАДКА ==========
        System.out.println(">>> FileRepo.write: размер list = " + list.size());
        String json = GSON.toJson(list);
        System.out.println(">>> JSON длина = " + json.length());
        System.out.println(">>> JSON = " + json);
        // =============================

        /* пишем и сразу сбрасываем буфер */
        try {
            Files.writeString(file, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println(">>> writeString выполнен успешно");
        } catch (Exception e) {
            System.out.println(">>> ОШИБКА в FileRepo.write: " + e.getMessage());
            e.printStackTrace();
            throw e;   // пробросим дальше
        }
    }

    /* ---------- запись произвольного объекта (DTO, Map и т.п.) ---------- */
    public static void writeObject(Path file, Object obj) throws IOException {
        String json = GSON.toJson(obj);
        System.out.println(">>> FileRepo.writeObject: JSON длина = " + json.length());
        System.out.println(">>> FileRepo.writeObject: JSON = " + (json.length() > 1000 ? json.substring(0,1000) + "..." : json));
        try {
            Files.writeString(file, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println(">>> writeObject выполнен успешно");
        } catch (Exception e) {
            System.out.println(">>> ОШИБКА в FileRepo.writeObject: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /* ---------- чтение JSON из файла в List<Product> (старый метод) ---------- */
    public static List<Product> read(Path file) throws IOException {
        String json = Files.readString(file);
        return GSON.fromJson(json, TYPE);
    }

    /* ---------- чтение JSON в произвольный тип (например: ExportDTO[]) ---------- */
    public static <T> List<T> readAs(Class<T[]> clazz, Path file) throws IOException {
        String json = Files.readString(file);
        T[] arr = GSON.fromJson(json, clazz);
        if (arr == null) return List.of();
        return List.of(arr);
    }
}