/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

public class MathUtils {
    
    // Returns in function of the sign of val
    public static double averageNegativeOrPositive(double val, double negative, double positive){
        return val < 0 ? negative : positive;
    }
    
    public static double clamp(double val, double min, double max){
        return Math.max(min, Math.min(max, val));
    }
    public static float clamp(float val, float min, float max){
        return Math.max(min, Math.min(max, val));
    }
    public static int clamp(int val, int min, int max){
        return Math.max(min, Math.min(max, val));
    }
    public static Double getDouble(String text){
        try{
            return Double.parseDouble(text);
        }catch(NumberFormatException e){
            return null;
        }
    }
    public static Integer getInt(String text){
        try{
            return Integer.parseInt(text);
        }catch(NumberFormatException e){
            return null;
        }
    }
    public static int getAlwaysInt(String text){
        try{
            return Integer.parseInt(text);
        }catch(NumberFormatException e){
            return 0;
        }
    }
    public static Long getLong(String text){
        try{
            return Long.parseLong(text);
        }catch(NumberFormatException e){
            return null;
        }
    }
    public static long getAlwaysLong(String text){
        try{
            return Long.parseLong(text);
        }catch(NumberFormatException e){
            return 0;
        }
    }
    public static double getAlwaysDouble(String text){
        try{
            return Double.parseDouble(text);
        }catch(NumberFormatException e){
            return 0;
        }
    }
}
