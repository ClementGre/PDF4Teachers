/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.HBoxSpacer;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.io.File;

public class DirFilterListCell extends ListCell<String> {
    
    private final GalleryWindow gallery;
    public DirFilterListCell(GalleryWindow galleryWindow){
        gallery = galleryWindow;
    }
    
    @Override
    protected void updateItem(String item, boolean empty){
        
        super.updateItem(item, empty);
        if(empty || item == null){
            setGraphic(null);
            setText(null);
            setStyle(null);
        }else if(item.equals(TR.tr("galleryWindow.filterAndEditCombo.everywhere")) || item.equals(TR.tr("galleryWindow.filterAndEditCombo.favourites"))){
            // FAVORITES AND
            setGraphic(null);
            setText(item);
            setStyle("-fx-font-size: " + (12 * Main.settings.zoom.getValue()) + ";");
            
        }else if(item.equals(TR.tr("galleryWindow.filterAndEditCombo.addDirectoryButton"))){
            // ADD BUTTON
            Button add = new Button(TR.tr("galleryWindow.filterAndEditCombo.addDirectoryButton"));
            PaneUtils.setHBoxPosition(add, 0, 30, 10, 2);
            
            add.setOnMousePressed((e) -> {
                PlatformUtils.runLaterOnUIThread(100, () -> {
                    File dir = FilesChooserManager.showDirectoryDialog(FilesChooserManager.SyncVar.LAST_GALLERY_OPEN_DIR, gallery);
                    if(dir != null){
                        PlatformUtils.runLaterOnUIThread(100, () -> {
                            GalleryManager.addSavePath(dir.getAbsolutePath());
                            gallery.updateComboBoxItems();
                            gallery.reloadImageList();
                        });
                    }
                });
            });
            
            HBox root = new HBox();
            root.getChildren().addAll(new HBoxSpacer(), add, new HBoxSpacer());
            
            if(Main.settings.zoom.getValue() != 1){
                PaneUtils.setupScaling(root, true, false);
            }
            
            setGraphic(root);
            setText(null);
            setStyle("-fx-background-color: transparent;");
        }else{
            HBox root = new HBox();
            
            Label text = new Label(FilesUtils.getPathReplacingUserHome(item));
            text.setStyle("-fx-font-size: 12;");
            Button delete = new Button();
            delete.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TRASH, "darkred", 0, 14, ImageUtils.defaultDarkColorAdjust));
            delete.setStyle("-fx-background-color: transparent;");
            delete.setOnMouseEntered((e) -> delete.setStyle(null));
            delete.setOnMouseExited((e) -> delete.setStyle("-fx-background-color: transparent;"));
            
            delete.setOnMousePressed((e) -> {
                e.consume();
                GalleryManager.removeSavePath(item);
                gallery.filter.getItems().remove(item);
                gallery.reloadImageList();
                
                // Force ComboBox to reopen after 50 to 500 ms (after the click was released)
                for(int i = 50; i <= 500; i += 50){
                    PlatformUtils.runLaterOnUIThread(i, gallery.filter::show);
                }
            });
            
            if(Main.settings.zoom.getValue() == 1) root.setStyle("-fx-padding: -4 0;");
            else PaneUtils.setupScaling(root, true, false);
            
            PaneUtils.setHBoxPosition(text, 0, 26, 0);
            PaneUtils.setHBoxPosition(delete, 26, 26, new Insets(0, 5, 0, 5));
            
            Region spacer = new HBoxSpacer();
            root.getChildren().addAll(text, spacer, delete);
            
            setGraphic(root);
            setText(null);
            setStyle(null);
        }
    }
    
}
