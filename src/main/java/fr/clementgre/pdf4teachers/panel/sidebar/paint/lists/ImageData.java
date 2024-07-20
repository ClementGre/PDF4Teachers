/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridCell;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.scene.input.KeyCodeCombination;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ImageData extends ImageLambdaData {
    
    private int width;
    private int height;
    private GraphicElement.RepeatMode repeatMode;
    private GraphicElement.ResizeMode resizeMode;
    private long lastUse;
    private int useCount;
    private KeyCodeCombination keyCodeCombination;
    
    public ImageData(String imageId, int width, int height, GraphicElement.RepeatMode repeatMode, GraphicElement.ResizeMode resizeMode, long lastUse, int useCount, KeyCodeCombination keyCodeCombination)
    {
        super(imageId);
        this.width = width;
        this.height = height;
        this.repeatMode = repeatMode;
        this.resizeMode = resizeMode;
        this.lastUse = lastUse;
        this.useCount = useCount;
        this.keyCodeCombination = keyCodeCombination;
    }
    
    public ImageElement addToDocument(boolean link, boolean centerOnCursor){
        ImageData linkedImage = link ? this : null;
        PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
        
        int x = page.getNewElementXOnGrid(true);
        if(centerOnCursor) x = page.getNewElementXOnGrid(false);
        
        ImageElement element = new ImageElement(x, page.getNewElementYOnGrid(), page.getPage(), true,
                width, height, repeatMode, resizeMode, imageId, linkedImage);
        
        page.addElement(element, true, UType.ELEMENT);
        
        if(centerOnCursor) element.centerOnCoordinatesX();
        element.centerOnCoordinatesY();
        
        MainWindow.mainScreen.setSelected(element);
        Main.window.requestFocus();
        
        incrementUsesAndLastUse();
        return element;
    }
    public void setAsToPlaceElement(boolean link){
        ImageData linkedImage = link ? this : null;
        ImageElement element = new ImageElement(0, 0, 0, false, 1, 1, repeatMode, resizeMode, imageId, linkedImage);
        element.updateImage(true);
        
        MainWindow.mainScreen.setToPlace(element);
        PlatformUtils.runLaterOnUIThread(200, () -> { // Allow the time to double click if wanted
            Main.window.requestFocus();
        });
    }
    
    public void incrementUsesAndLastUse(){
        lastUse = System.currentTimeMillis();
        useCount++;
        ImageGridCell.updateGalleryAndFavoritesSort();
    }
    
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
        data.put("width", width);
        data.put("height", height);
        data.put("repeatMode", getRepeatMode().name());
        data.put("resizeMode", getResizeMode().name());
        data.put("imageId", getImageId());
        data.put("lastUse", lastUse);
        data.put("useCount", useCount);
        data.put("keyCodeCombination", keyCodeCombination == null ? null : keyCodeCombination.getName());
        
        return data;
    }
    
    public static ImageData readYAMLDataAndGive(HashMap<String, Object> data){
        
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        GraphicElement.RepeatMode repeatMode = GraphicElement.RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        GraphicElement.ResizeMode resizeMode = GraphicElement.ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        String imageId = Config.getString(data, "imageId");
        int useCount = (int) Config.getLong(data, "useCount");
        long lastUse = Config.getLong(data, "lastUse");
        
        KeyCodeCombination keyCodeCombination = null;
        if(data.get("keyCodeCombination") != null){
            try{
                keyCodeCombination = (KeyCodeCombination) KeyCodeCombination.valueOf(Config.getString(data, "keyCodeCombination"));
            }catch(IllegalArgumentException e){
                Log.e("Unable to parse ImageData keyCodeCombination: " + e.getMessage());
            }
        }
        
        return new ImageData(imageId, width, height, repeatMode, resizeMode, lastUse, useCount, keyCodeCombination);
    }
    
    public int getWidth(){
        return width;
    }
    public void setWidth(int width){
        this.width = width;
    }
    public int getHeight(){
        return height;
    }
    public void setHeight(int height){
        this.height = height;
    }
    
    public GraphicElement.RepeatMode getRepeatMode(){
        return repeatMode;
    }
    public void setRepeatMode(GraphicElement.RepeatMode repeatMode){
        this.repeatMode = repeatMode;
    }
    public GraphicElement.ResizeMode getResizeMode(){
        return resizeMode;
    }
    public void setResizeMode(GraphicElement.ResizeMode resizeMode){
        this.resizeMode = resizeMode;
    }
    @Override
    public String getImageId(){
        return imageId;
    }
    @Override
    public void setImageId(String imageId){
        this.imageId = imageId;
    }
    
    public long getLastUse(){
        return lastUse;
    }
    public void setLastUse(long lastUse){
        this.lastUse = lastUse;
    }
    public int getUseCount(){
        return useCount;
    }
    public void setUseCount(int useCount){
        this.useCount = useCount;
    }
    
    public KeyCodeCombination getKeyCodeCombination(){
        return keyCodeCombination;
    }
    public void setKeyCodeCombination(KeyCodeCombination keyCodeCombination){
        this.keyCodeCombination = keyCodeCombination;
    }
    
    public void resetUseData(){
        lastUse = System.currentTimeMillis();
        useCount = 0;
    }
}
