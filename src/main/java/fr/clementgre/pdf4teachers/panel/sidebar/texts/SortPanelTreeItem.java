/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;

public class SortPanelTreeItem extends TreeItem {
    
    public GridPane pane = new GridPane();
    
    public SortPanelTreeItem(){
    
    }
    
    public void updateCell(TreeCell cell){
        cell.setPrefHeight(pane.getPrefHeight());
        cell.setContextMenu(null);
        cell.setOnMouseClicked(null);
        cell.setStyle("-fx-padding: 0 0 0 -40; -fx-margin: 0; -fx-background-color: " + StyleManager.getHexAccentColor() + ";");
        cell.setGraphic(pane);
    }
}
