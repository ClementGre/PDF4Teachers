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
import javafx.scene.Cursor;
import javafx.scene.control.Button;

public class IconButton extends Button {
    
    public IconButton(String svgPath){
        this(svgPath, null, null, false);
    }
    public IconButton(String svgPath, String tooltip){
        this(svgPath, tooltip, null, false);
    }
    public IconButton(String svgPath, EventHandler<ActionEvent> onAction){
        this(svgPath, null, onAction, false);
    }
    public IconButton(String svgPath, String tooltip, EventHandler<ActionEvent> onAction){
        this(svgPath, tooltip, onAction, false);
    }
    public IconButton(String svgPath, String tooltip, EventHandler<ActionEvent> onAction, boolean big){
        super();
    
        int size = 20;
        if(big){
            PaneUtils.setHBoxPosition(this, 40, 35, 0);
            size = 24;
        }else{
            PaneUtils.setHBoxPosition(this, 30, 30, 0);
        }
        setGraphic(SVGPathIcons.generateImage(svgPath, "black", 0, size, ImageUtils.defaultDarkColorAdjust));
        
        setCursor(Cursor.HAND);
        
        
        if(tooltip != null) {
            setTooltip(PaneUtils.genWrappedToolTip(tooltip));
        }
        if(onAction != null) {
            setOnAction(onAction);
        }
        
    }
    
}
