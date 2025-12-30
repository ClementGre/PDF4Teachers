/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.toc;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a table of contents entry
 */
public record TocEntry(String title, int pageNumber, int pageY) {
    
    @Override
    public @NotNull String toString(){
        if(pageNumber >= 0){
            return title + " (p. " + (pageNumber + 1) + ")";
        }
        return title;
    }
}
