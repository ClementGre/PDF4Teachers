/*
 * Copyright (c) 2021-2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.menus.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageLambdaData;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ConfirmAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.KeyCodeCombinaisonInputAlert;
import fr.clementgre.pdf4teachers.utils.image.ExifUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;

import java.io.File;
import java.util.Collections;

public class ImageGridElement extends ImageLambdaData{

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(null);
    private boolean rendering;
    private ExifUtils.BasicExifData exifData;

    private ImageData linkedImageData;

    // MENU
    private final ContextMenu menu = new ContextMenu();
    private final NodeRadioMenuItem isFavoriteItem = new NodeRadioMenuItem(TR.tr("graphicElement.contextMenu.favorite"), true, true, false);
    private final NodeMenuItem addItem = new NodeMenuItem(TR.tr("gallery.imageContextMenu.addOnCurentDocument"), false);
    private final NodeMenuItem openItem = new NodeMenuItem(TR.tr("gallery.imageContextMenu.openFileInExplorer"), false);
    private final NodeMenuItem deleteItem = new NodeMenuItem(TR.tr("actions.deleteFile"), false);
    private final NodeMenuItem editShortcut = new NodeMenuItem(TR.tr("elementMenu.editShortcut"), false);
    

    public ImageGridElement(String imageId, ImageGridView gridView){
        super(imageId);
        loadExifData();
        setup(gridView);
    }
    public ImageGridElement(ImageData linkedImageData, ImageGridView gridView){
        super(linkedImageData.getImageId());
        this.linkedImageData = linkedImageData;
        loadExifData();
        setup(gridView);
    }

    public void loadExifData(){
        exifData = new ExifUtils.BasicExifData(new File(imageId));
    }

    private void setup(ImageGridView gridView){

        isFavoriteItem.setOnAction((event) -> {
            toggleFavorite();
        });
        addItem.setOnAction((event) -> {
            addToDocument(false);
        });
        openItem.setOnAction((event) -> {
            PlatformUtils.openFile(getImageIdDirectory());
        });
        deleteItem.setOnAction((event) -> {
            if(new ConfirmAlert(true, TR.tr("dialog.confirmation.deleteFile.header", getImageIdFileName())).execute()){
                if(new File(getImageId()).delete()){
                    gridView.removeItems(Collections.singletonList(this));
                }else{
                    Log.e("Unable to delete file " + getImageId());
                }
            }
        });
        editShortcut.setOnAction((event) -> {
            editShortcut();
        });

        menu.setOnShowing((e) -> {
            isFavoriteItem.setSelected(isFavorite());
            addItem.setDisable(!MainWindow.mainScreen.hasDocument(false));
        });
        NodeMenuItem.setupMenu(menu);

        menu.getItems().setAll(addItem, isFavoriteItem, new SeparatorMenuItem(), openItem, deleteItem, editShortcut);
    }

    public void toggleFavorite(){
        if(isFavorite()){
            MainWindow.paintTab.favouriteImages.getList().removeItems(Collections.singletonList(this));
            linkedImageData = null;
        }else{
            linkedImageData = new ImageData(imageId, 0, 0, GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS, 0, 0, null);
            MainWindow.paintTab.favouriteImages.getList().addItems(Collections.singletonList(this));
        }
        if(MainWindow.paintTab.galleryWindow != null && MainWindow.paintTab.galleryWindow.isShowing()){
            if(TR.tr("galleryWindow.filterAndEditCombo.favourites").equals(MainWindow.paintTab.galleryWindow.getList().getFilterType())){
                MainWindow.paintTab.galleryWindow.getList().updateItemsFiltered();
            }
        }
    }

    public ImageElement addToDocument(boolean centerOnCursor){
        if(MainWindow.mainScreen.hasDocument(false)) return getImageData().addToDocument(hasLinkedImageData(), centerOnCursor);
        return null;
    }
    public void setAsToPlaceElement(){
        if(MainWindow.mainScreen.hasDocument(false)) getImageData().setAsToPlaceElement(hasLinkedImageData());
    }

    public ImageData getImageData(){
        if(hasLinkedImageData()){
            return linkedImageData;
        }
        if(isFavorite()){
            return MainWindow.paintTab.favouriteImages.getList().getAllItems().get(MainWindow.paintTab.favouriteImages.getList().getAllItems().indexOf(this)).linkedImageData;
        }
        return toImageData();
    }

    // SORTER

    public int compareTimeWith(ImageGridElement element){
        int value = element.getExifData().getEditDate().compareTo(exifData.getEditDate());

        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareUseWith(ImageGridElement element){
        int value = 0;
        if(isFavorite()){
            if(element.isFavorite()){
                value = element.getImageData().getUseCount() - getImageData().getUseCount();
            }else value = -1;
        }else if(element.isFavorite()) value = 1;

        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareLastUseTimeWith(ImageGridElement element){
        int value = 0;
        if(isFavorite()){
            if(element.isFavorite()){
                long val = (element.getImageData().getLastUse() - getImageData().getLastUse());
                value = val > 0 ? 1 : (val < 0 ? -1 : 0);
            }else value = -1;
        }else if(element.isFavorite()) value = 1;

        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareSizeWith(ImageGridElement element){
        int value = Long.compare(element.getExifData().getSize(), exifData.getSize());

        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareDirectoryWith(ImageGridElement element){
        int value = getImageIdDirectory().compareTo(element.getImageIdDirectory());

        if(value == 0) value = getImageIdFileName().compareTo(element.getImageIdFileName());
        return value;
    }
    public int compareNameWith(ImageGridElement element){
        int value = getImageIdFileName().compareTo(element.getImageIdFileName());

        if(value == 0) return compareDirectoryWith(element);
        return value;
    }

    public void resetUseData(){
        if(linkedImageData != null){
            linkedImageData.resetUseData();
        }
    }
    
    public void editShortcut(){
        KeyCodeCombinaisonInputAlert alert = new KeyCodeCombinaisonInputAlert(TR.tr("paintTab.elementShortcutDialog.title"), TR.tr("paintTab.elementShortcutDialog.title"),
                getImageData().getKeyCodeCombination(), true, true);
        KeyCodeCombinaisonInputAlert.Result result = alert.showAndWaitGetResult();
        
        if(result == KeyCodeCombinaisonInputAlert.Result.VALIDATE){
            getImageData().setKeyCodeCombination(alert.getKeyCodeCombinaison());
        }else if(result == KeyCodeCombinaisonInputAlert.Result.DELETE){
            getImageData().setKeyCodeCombination(null);
        }
    }

    // GETTERS / SETTERS

    public Image getImage(){
        return image.get();
    }
    public ObjectProperty<Image> imageProperty(){
        return image;
    }
    public void setImage(Image image){
        this.image.set(image);
    }
    public void setRendering(boolean rendering){
        this.rendering = rendering;
    }
    public boolean isRendering(){
        return rendering;
    }
    public ExifUtils.BasicExifData getExifData(){
        return exifData;
    }
    public void setExifData(ExifUtils.BasicExifData exifData){
        this.exifData = exifData;
    }
    public ImageData getLinkedImageData(){
        return linkedImageData;
    }
    public boolean hasLinkedImageData(){
        return linkedImageData != null;
    }
    public boolean isFavorite(){
        return MainWindow.paintTab.favouriteImages.getList().getAllItems().contains(this);
    }
    public void setLinkedImageData(ImageData linkedImageData){
        this.linkedImageData = linkedImageData;
    }

    public ContextMenu getMenu(){
        return menu;
    }
}
