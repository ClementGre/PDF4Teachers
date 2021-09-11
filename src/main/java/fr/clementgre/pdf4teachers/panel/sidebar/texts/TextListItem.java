/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TextListItem {
    
    private Font font;
    private String text;
    private Color color;
    // Must be between 0 and 100 in percents.
    private final DoubleProperty maxWidth = new SimpleDoubleProperty();
    
    private long uses;
    private long creationDate;
    
    public TextListItem(Font font, String text, Color color, double maxWidth, long uses, long creationDate){
        this.font = font;
        this.text = text;
        this.color = color;
        this.uses = uses;
        this.creationDate = creationDate;
        this.maxWidth.set(maxWidth == 0 ? Main.settings.defaultMaxWidth.getValue() : maxWidth);
    }
    
    public TextTreeItem toTextTreeItem(int type){
        return new TextTreeItem(font, text, color, getMaxWidth(), type, uses, creationDate);
    }
    
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
        data.put("color", color.toString());
        data.put("font", font.getFamily());
        data.put("size", font.getSize());
        data.put("bold", FontUtils.getFontWeight(font) == FontWeight.BOLD);
        data.put("italic", FontUtils.getFontPosture(font) == FontPosture.ITALIC);
        data.put("uses", uses);
        data.put("date", creationDate);
        data.put("text", text);
        data.put("maxWidth", getMaxWidth());
        
        return data;
    }
    
    public static TextListItem readYAMLDataAndGive(HashMap<String, Object> data){
        
        double fontSize = Config.getDouble(data, "size");
        boolean isBold = Config.getBoolean(data, "bold");
        boolean isItalic = Config.getBoolean(data, "italic");
        String fontName = Config.getString(data, "font");
        Color color = Color.valueOf(Config.getString(data, "color"));
        long uses = Config.getLong(data, "uses");
        long creationDate = Config.getLong(data, "date");
        String text = Config.getString(data, "text");
        double maxWidth = Config.getDouble(data, "maxWidth");
        
        Font font = FontUtils.getFont(fontName, isItalic, isBold, (int) fontSize);
        
        return new TextListItem(font, text, color, maxWidth, uses, creationDate);
    }
    
    public Font getFont(){
        return font;
    }
    public void setFont(Font font){
        this.font = font;
    }
    public String getText(){
        return text;
    }
    public void setText(String text){
        this.text = text;
    }
    public Color getColor(){
        return color;
    }
    public void setColor(Color color){
        this.color = color;
    }
    public long getUses(){
        return uses;
    }
    public void setUses(long uses){
        this.uses = uses;
    }
    public long getCreationDate(){
        return creationDate;
    }
    public void setCreationDate(long creationDate){
        this.creationDate = creationDate;
    }
    public double getMaxWidth(){
        return maxWidth.get();
    }
    public DoubleProperty maxWidthProperty(){
        return maxWidth;
    }
    public void setMaxWidth(double maxWidth){
        this.maxWidth.set(maxWidth);
    }
}
