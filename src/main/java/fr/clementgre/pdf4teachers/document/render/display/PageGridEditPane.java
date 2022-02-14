/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display;

import javafx.scene.layout.Pane;

public class PageGridEditPane extends Pane {
    
    private final PageRenderer page;
    public PageGridEditPane(PageRenderer page){
        this.page = page;
    
    }
}
