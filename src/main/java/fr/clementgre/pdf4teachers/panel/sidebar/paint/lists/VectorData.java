/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridCell;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VectorData{
    
    private int width;
    private int height;
    private GraphicElement.RepeatMode repeatMode;
    private GraphicElement.ResizeMode resizeMode;
    private boolean doFill;
    private Color fill;
    private Color stroke;
    private int strokeWidth;
    private String path;
    private boolean invertX;
    private boolean invertY;
    private int arrowLength;
    private long lastUse;
    private int useCount;
    
    private CallBack specsChangesCallback;
    private CallBack displayChangesCallback;
    
    public VectorData(int width, int height, GraphicElement.RepeatMode repeatMode, GraphicElement.ResizeMode resizeMode,
                      boolean doFill, Color fill, Color stroke, int strokeWidth,
                      String path, boolean invertX, boolean invertY, int arrowLength, long lastUse, int useCount){
        
        this.width = width;
        this.height = height;
        this.repeatMode = repeatMode;
        this.resizeMode = resizeMode;
        this.doFill = doFill;
        this.fill = fill;
        this.stroke = stroke;
        this.strokeWidth = strokeWidth;
        this.path = path;
        this.invertX = invertX;
        this.invertY = invertY;
        this.arrowLength = arrowLength;
        this.lastUse = lastUse;
        this.useCount = useCount;
    }
    
    public boolean equals(VectorElement element){
        return doFill == element.isDoFill() && strokeWidth == element.getStrokeWidth() && repeatMode == element.getRepeatMode() && resizeMode == element.getResizeMode()
                && fill.toString().equals(element.getFill().toString()) && stroke.toString().equals(element.getStroke().toString()) && path.equals(element.getPath());
    }
    
    public VectorElement addToDocument(boolean link){
        PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
    
        VectorElement element = new VectorElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(), true,
                width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, arrowLength, link ? this : null);
        
        page.addElement(element, true, UType.UNDO);
        element.centerOnCoordinatesY();
        MainWindow.mainScreen.setSelected(element);
        
        incrementUsesAndLastUse();
        return element;
    }
    public void setAsToPlaceElement(boolean link){
        // if resizeMode == GraphicElement.ResizeMode.SIDE_EDGES, height = vectorData height
        VectorElement element = new VectorElement(0, 0, 0, false, 1, resizeMode == GraphicElement.ResizeMode.SIDE_EDGES ? height : 1,
                repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, arrowLength, link ? this : null);
        
        MainWindow.mainScreen.setToPlace(element);
    }
    
    public void incrementUsesAndLastUse(){
        lastUse = System.currentTimeMillis();
        useCount++;
        VectorGridCell.updateListsSort();
    }
    
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
        data.put("width", width);
        data.put("height", height);
        data.put("repeatMode", getRepeatMode().name());
        data.put("resizeMode", getResizeMode().name());
    
        data.put("doFill", doFill);
        data.put("fill", fill.toString());
        data.put("stroke", stroke.toString());
        data.put("strokeWidth", strokeWidth);
        data.put("path", path);
        data.put("invertX", invertX);
        data.put("invertY", invertY);
        
        data.put("arrowLength", arrowLength);
        
        data.put("lastUse", lastUse);
        data.put("useCount", useCount);
        
        return data;
    }
    
    public static VectorData readYAMLDataAndGive(HashMap<String, Object> data){
        
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        GraphicElement.RepeatMode repeatMode = GraphicElement.RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        GraphicElement.ResizeMode resizeMode = GraphicElement.ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        String imageId = Config.getString(data, "imageId");
        int useCount = (int) Config.getLong(data, "useCount");
        long lastUse = Config.getLong(data, "lastUse");
    
        boolean doFill = Config.getBoolean(data, "doFill");
        Color fill = Color.DARKGRAY;
        Color stroke = Color.BLACK;
        try{
            fill = Color.valueOf(Config.getString(data, "fill"));
            stroke = Color.valueOf(Config.getString(data, "stroke"));
        }catch(IllegalArgumentException e){
            Log.e("Unable to parse VectorElement color: " + e.getMessage());
        }
        int strokeWidth = (int) Config.getLong(data, "strokeWidth");
        String path = Config.getString(data, "path");
    
        int arrowLength = (int) Config.getLong(data, "arrowLength");
        
        boolean invertX = Config.getBoolean(data, "invertX");
        boolean invertY = Config.getBoolean(data, "invertY");
        
        return new VectorData(width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, arrowLength, lastUse, useCount);
    }
    
    public void resetUseData(){
        lastUse = System.currentTimeMillis();
        useCount = 0;
    }
    
    public void setSpecsChangesCallback(CallBack specsChangesCallback){
        this.specsChangesCallback = specsChangesCallback;
    }
    public void setDisplayChangesCallback(CallBack displayChangesCallback){
        this.displayChangesCallback = displayChangesCallback;
    }
    
    public int getWidth(){
        return width;
    }
    public void setWidth(int width){
        this.width = width;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    public int getHeight(){
        return height;
    }
    public void setHeight(int height){
        this.height = height;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    
    public GraphicElement.RepeatMode getRepeatMode(){
        return repeatMode;
    }
    public void setRepeatMode(GraphicElement.RepeatMode repeatMode){
        this.repeatMode = repeatMode;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    public GraphicElement.ResizeMode getResizeMode(){
        return resizeMode;
    }
    public void setResizeMode(GraphicElement.ResizeMode resizeMode){
        this.resizeMode = resizeMode;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    
    public boolean isDoFill(){
        return doFill;
    }
    public void setDoFill(boolean doFill){
        this.doFill = doFill;
        if(specsChangesCallback != null) specsChangesCallback.call();
    }
    public Color getFill(){
        return fill;
    }
    public void setFill(Color fill){
        this.fill = fill;
        if(specsChangesCallback != null) specsChangesCallback.call();
    }
    
    public Color getStroke(){
        return stroke;
    }
    public void setStroke(Color stroke){
        this.stroke = stroke;
        if(specsChangesCallback != null) specsChangesCallback.call();
    }
    public int getStrokeWidth(){
        return strokeWidth;
    }
    public void setStrokeWidth(int strokeWidth){
        this.strokeWidth = strokeWidth;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    
    public String getPath(){
        return path;
    }
    public void setPath(String path){
        this.path = path;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    
    public boolean isInvertX(){
        return invertX;
    }
    public void setInvertX(boolean invertX){
        this.invertX = invertX;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    public boolean isInvertY(){
        return invertY;
    }
    public void setInvertY(boolean invertY){
        this.invertY = invertY;
        if(displayChangesCallback != null) displayChangesCallback.call();
    }
    
    public int getArrowLength(){
        return arrowLength;
    }
    public void setArrowLength(int arrowLength){
        this.arrowLength = arrowLength;
        if(displayChangesCallback != null) displayChangesCallback.call();
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
    
    @Override
    public VectorData clone(){
        return new VectorData(width, height, repeatMode, resizeMode,
                doFill, fill, stroke, strokeWidth, path,
                invertX, invertY, arrowLength, lastUse, useCount);
    }
}
