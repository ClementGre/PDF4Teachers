/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import javafx.scene.text.Font;

import java.util.HashMap;

public class AppFontsLoader{
    
    private static final HashMap<String, String> loadedFonts = new HashMap<>();
    
    public static final String LATO_BOLD = "Lato-Bold.ttf";
    public static final String LATO = "Lato-Regular.ttf";
    public static final String COURIER_PRIME_REGULAR = "CourierPrime-Regular.ttf";
    public static final String ARIAL_ROUNDED_MT_BOLD = "ArialRoundedMT-Bold.ttf";
    public static final String OPEN_SANS = "/fonts/Open Sans/regular.ttf";
    
    public static void loadAppFonts(){
        loadFont(LATO);
        loadFont(LATO_BOLD);
        loadFont(COURIER_PRIME_REGULAR);
        loadFont(ARIAL_ROUNDED_MT_BOLD);
        
        loadFontPath(OPEN_SANS);
    }
    
    public static Font getFont(String name, double size){
        if(!loadedFonts.containsKey(name)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream("/appFonts/" + name), size);
            if(font == null){
                Log.w("Font " + "/appFonts/" + name + " is null...");
                return Font.font(size);
            }
            loadedFonts.put(name, font.getFamily());
            return font;
        }
        return Font.font(loadedFonts.get(name), size);
    }
    public static Font getFontPath(String path, double size){
        if(!loadedFonts.containsKey(path)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream("/appFonts/" + path), size);
            if(font == null){
                Log.w("Font " + path + " is null...");
                return Font.font(size);
            }
            loadedFonts.put(path, font.getFamily());
            return font;
        }
        return Font.font(loadedFonts.get(path), size);
    }
    
    public static void loadFont(String name){
        if(!loadedFonts.containsKey(name)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream("/appFonts/" + name), -1);
            if(font == null){
                Log.w("Font " + "/appFonts/" + name + " is null...");
            }else{
                loadedFonts.put(name, font.getFamily());
            }
        }
    }
    public static void loadFontPath(String path){
        if(!loadedFonts.containsKey(path)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream(path), -1);
            if(font == null){
                Log.w("Font " + path + " is null...");
            }else{
                loadedFonts.put(path, font.getFamily());
            }
        }
    }
    
}
