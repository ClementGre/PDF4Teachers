/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.toc;

/**
 * Represents a table of contents entry
 */
public class TocEntry {
    private final String title;
    private final int pageNumber;
    
    public TocEntry(String title, int pageNumber) {
        this.title = title;
        this.pageNumber = pageNumber;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getPageNumber() {
        return pageNumber;
    }
    
    @Override
    public String toString() {
        if (pageNumber >= 0) {
            return title + " (p. " + (pageNumber + 1) + ")";
        }
        return title;
    }
}
