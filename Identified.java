package com.company.lab5.model;

public interface Identified {
    default String id() {
        return String.valueOf(System.nanoTime());
    }
}