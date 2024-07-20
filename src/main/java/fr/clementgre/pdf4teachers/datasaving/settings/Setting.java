/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.datasaving.settings;

import fr.clementgre.pdf4teachers.utils.interfaces.ReturnCallBack;
import javafx.scene.layout.HBox;

public abstract class Setting<T> {
    
    private String path;
    String icon;
    String title;
    String description;
    
    private boolean hasEditPane;
    private ReturnCallBack<HBox> getEditPaneCallback;
    
    public Setting(boolean hasEditPane, String icon, String path, String title, String description){
        this.icon = icon;
        this.path = path;
        this.title = title;
        this.description = description;
        this.hasEditPane = hasEditPane;
    }
    
    protected abstract HBox getDefaultEditPane();
    public HBox getCustomEditPane(){
        if(hasEditPane) return getDefaultEditPane();
        if(getEditPaneCallback != null) return getEditPaneCallback.call();
        return new HBox();
    }
    
    public abstract T getValue();
    
    public abstract void setValue(T value);
    
    public String getIcon(){
        return icon;
    }
    
    public void setIcon(String icon){
        this.icon = icon;
    }
    
    public String getPath(){
        return path;
    }
    
    public void setPath(String path){
        this.path = path;
    }
    
    public String getTitle(){
        return title;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public boolean isHasEditPane(){
        return hasEditPane;
    }
    
    public void setHasEditPane(boolean hasEditPane){
        this.hasEditPane = hasEditPane;
    }
    
    public ReturnCallBack<HBox> getGetEditPaneCallback(){
        return getEditPaneCallback;
    }
    
    public void setGetEditPaneCallback(ReturnCallBack<HBox> getEditPaneCallback){
        this.getEditPaneCallback = getEditPaneCallback;
    }
}
