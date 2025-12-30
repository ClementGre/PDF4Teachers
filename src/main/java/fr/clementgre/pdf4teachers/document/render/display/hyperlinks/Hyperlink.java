/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display.hyperlinks;

/**
 * Represents a hyperlink found in a PDF page
 */
public class Hyperlink {
    
    public enum Type {
        URL,        // External URL link
        GOTO,       // Internal page reference
        MAILTO,     // Email link
        UNKNOWN     // Other types
    }
    
    private final Type type;
    private final String destination;  // URL, email, or null for internal links
    private final Integer targetPage;  // Target page number for internal links, null otherwise
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    
    public Hyperlink(Type type, String destination, Integer targetPage, double x, double y, double width, double height) {
        this.type = type;
        this.destination = destination;
        this.targetPage = targetPage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public Integer getTargetPage() {
        return targetPage;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
}
