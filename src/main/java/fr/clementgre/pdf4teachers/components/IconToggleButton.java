/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.ToggleButton;

public class IconToggleButton extends ToggleButton {
    
    public IconToggleButton(String svgPath){
        this(svgPath, svgPath, null, null, false);
    }
    public IconToggleButton(String svgPath, String tooltip){
        this(svgPath, svgPath, tooltip, null, false);
    }
    public IconToggleButton(String svgPath, EventHandler<ActionEvent> onAction){
        this(svgPath, svgPath, null, onAction, false);
    }
    public IconToggleButton(String svgPath, String tooltip, EventHandler<ActionEvent> onAction){
        this(svgPath, svgPath, tooltip, onAction, false);
        
        setPadding(new Insets(3));
        PaneUtils.setHBoxPosition(this, 30, 30, 0);
        setCursor(Cursor.HAND);
        setGraphic(SVGPathIcons.generateImage(svgPath, "black", 0, 0, ImageUtils.defaultDarkColorAdjust));
        if(tooltip != null) setTooltip(PaneUtils.genWrappedToolTip(tooltip));
        if(onAction != null) setOnAction(onAction);
        
    }
    
    private final String svgPathSelected;
    private final String svgPathNotSelected;
    private final int size;
    public IconToggleButton(String svgPathSelected, String svgPathNotSelected, String tooltip, EventHandler<ActionEvent> onAction, boolean big){
        super();
        
        this.svgPathSelected = svgPathSelected;
        this.svgPathNotSelected = svgPathNotSelected;
        this.size = big ? 24 : 20;
    
        if(big) PaneUtils.setHBoxPosition(this, 40, 35, 0);
        else PaneUtils.setHBoxPosition(this, 30, 30, 0);
        
        if(tooltip != null) setTooltip(PaneUtils.genWrappedToolTip(tooltip));
        if(onAction != null) setOnAction(onAction);
    
        setCursor(Cursor.HAND);
        updateIcon();
        selectedProperty().addListener(o -> updateIcon());
    }
    
    private void updateIcon(){
        if(isSelected()) setGraphic(SVGPathIcons.generateImage(svgPathSelected, "black", 0, size, ImageUtils.defaultDarkColorAdjust));
        else setGraphic(SVGPathIcons.generateImage(svgPathNotSelected, "black", 0, size, ImageUtils.defaultDarkColorAdjust));
    }
    
}
