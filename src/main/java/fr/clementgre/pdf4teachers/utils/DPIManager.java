/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

public class DPIManager{
    
    
    // The width of 1 cm, must be updated via the methods below.
    private double oneCmWidth = 1;
    
    private final int dpi;
    
    public DPIManager(int dpi){
        this.dpi = dpi;
    }
    
    public void initOneCmWidth(double oneCmWidth){
        this.oneCmWidth = oneCmWidth;
    }
    public void initOneCmWidth(double width, double cmWidth){
        oneCmWidth = width / cmWidth;
    }
    public void initOneCmWidthFromA4Width(double width){
        oneCmWidth = width / 21; // A4 width = 21cm
    }
    
    public int getPixelsLength(double width){
        double mmWidth = 10d * width / oneCmWidth;
        // 1 in = 25.4 mm
        return (int) (dpi * mmWidth / 25.4);
    }
}
