/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TiersFont {
    
    private Font font;
    private Color color;
    private boolean showName;
    private boolean hide;
    private boolean hideWhenAllPoints;
    
    public TiersFont(Font font, Color color, boolean showName, boolean hide, boolean hideWhenAllPoints){
        this.font = font;
        this.color = color;
        this.showName = showName;
        this.hide = hide;
        this.hideWhenAllPoints = hideWhenAllPoints;
    }
    
    public static TiersFont getInstance(HashMap<String, Object> data){
        return new TiersFont(
                FontUtils.getFont(Config.getString(data, "font"), Config.getBoolean(data, "italic"), Config.getBoolean(data, "bold"), Config.getDouble(data, "size")),
                Color.valueOf(Config.getString(data, "color")),
                Config.getBoolean(data, "showName"),
                Config.getBoolean(data, "hide"),
                Config.getBoolean(data, "hideWhenAllPoints"));
    }
    
    public LinkedHashMap<String, Object> getData(){
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("font", font.getFamily());
        data.put("italic", FontUtils.getFontPosture(font) == FontPosture.ITALIC);
        data.put("bold", FontUtils.getFontWeight(font) == FontWeight.BOLD);
        data.put("size", font.getSize());
        data.put("color", color.toString());
        data.put("showName", showName);
        data.put("hide", hide);
        data.put("hideWhenAllPoints", hideWhenAllPoints);
        return data;
    }
    
    public Font getFont(){
        return font;
    }
    
    public void setFont(Font font){
        this.font = font;
    }
    
    public Color getColor(){
        return color;
    }
    
    public void setColor(Color color){
        this.color = color;
    }
    
    public boolean isShowName(){
        return showName;
    }
    
    public void setShowName(boolean showName){
        this.showName = showName;
    }
    
    public boolean isHide(){
        return hide;
    }
    
    public void setHide(boolean hide){
        this.hide = hide;
    }
    
    public boolean isHideWhenAllPoints(){
        return hideWhenAllPoints;
    }
    
    public void setHideWhenAllPoints(boolean hideWhenAllPoints){
        this.hideWhenAllPoints = hideWhenAllPoints;
    }
}
