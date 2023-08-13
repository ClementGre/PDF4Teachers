/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import javafx.scene.input.KeyEvent;

public final class MathUtils {

    // Returns in function of the sign of val
    public static double selectValueBasedOnSign(double val, double negative, double positive) {
        return val < 0 ? negative : positive;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static Double parseDoubleOrNull(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseIntOrNull(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long parseLongOrNull(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static long parseLongOrDefault(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double parseDoubleOrDefault(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public static Integer parseIntFromKeyEventOrNull(KeyEvent e){
        Integer first = parseIntOrNull(e.getCode().getChar());
        Integer second = parseIntOrNull(e.getText());
        return first != null ? first : second;
    }
}
