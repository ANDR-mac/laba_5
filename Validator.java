package com.company.lab5.utils;

public final class Validator {
    private Validator() {}
    public static boolean name(String s) {
        return s != null && s.matches("[A-Za-zА-Яа-яЁё0-9\\s\\-]{2,60}");
    }
    public static boolean price(String s) {
        try { double v = Double.parseDouble(s); return v > 0 && v < 1_000_000; }
        catch (NumberFormatException e) { return false; }
    }
    public static boolean discount(String s) {
        try { double v = Double.parseDouble(s); return v >= 0 && v <= 99; }
        catch (NumberFormatException e) { return false; }
    }
}