/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display.hyperlinks;

/**
 * Represents a hyperlink found in a PDF page
 *
 * @param destination URL, email, or null for internal links
 * @param targetPage  Target page number for internal links, null otherwise
 */
public record Hyperlink(Type type, String destination, Integer targetPage, Integer targetY, double x, double y,
                        double width,
                        double height) {
    
    public enum Type {
        URL,        // External URL link
        GOTO,       // Internal page reference
        MAILTO,     // Email link
        UNKNOWN     // Other types
    }
    
}
