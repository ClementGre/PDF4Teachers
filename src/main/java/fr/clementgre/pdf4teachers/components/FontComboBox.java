/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class FontComboBox extends ComboBox<String> {
    
    public FontComboBox(boolean bind){
        super();
        if(FontUtils.isFontsLoaded()) updateFonts();
        setCellFactory((ListView<String> stringListView) -> new FontCell(bind));
    }
    
    public void updateFonts(){
        setItems(FontUtils.getAllFonts());
        if(getSelectionModel().getSelectedItem() == null) getSelectionModel().select("Open Sans");
    }
    
    public static class FontCell extends ListCell<String> {
        
        private String lastFontName = "";
        
        public FontCell(boolean bind){
            super();
            
            if(bind){
                Main.settings.zoom.valueProperty()
                        .addListener((o, oldValue, newValue) ->
                                setStyle("-fx-font: " + (14 * Main.settings.zoom.getValue()) + " \"" + lastFontName + "\";"));
            }
        }
        
        @Override
        public void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            
            if(empty){
                setText(null);
                setGraphic(null);
            }else{
                lastFontName = item;
                setText(item);
                if(FontUtils.isDefaultFont(item)){
                    FontUtils.getFont(item, false, false, 14 * Main.settings.zoom.getValue());
                }
                
                setStyle("-fx-font: " + (14 * Main.settings.zoom.getValue()) + " \"" + item + "\";");
            }
        }
    }
    
}
