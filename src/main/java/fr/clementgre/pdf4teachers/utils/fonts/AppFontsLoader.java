package fr.clementgre.pdf4teachers.utils.fonts;

import javafx.scene.text.Font;
import java.util.HashMap;

public class AppFontsLoader{
    
    private static final HashMap<String, String> loadedFonts = new HashMap<>();
    
    public static final String LATO = "/fonts/Lato/regular.ttf";
    public static final String LATO_BOLD = "/fonts/Lato/bold.ttf";
    public static final String OPEN_SANS = "/fonts/Open Sans/regular.ttf";
    
    public static Font getFont(String name, double size){
        if(!loadedFonts.containsKey(name)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream("/appFonts/" + name), size);
            if(font == null){
                System.out.println("Font " + "/appFonts/" + name + " is null...");
                return Font.font(size);
            }else{
                loadedFonts.put(name, font.getFamily());
                return font;
            }
        }else{
            return Font.font(loadedFonts.get(name), size);
        }
    }
    public static Font getFontPath(String path, double size){
        if(!loadedFonts.containsKey(path)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream("/appFonts/" + path), size);
            if(font == null){
                System.out.println("Font " + path + " is null...");
                return Font.font(size);
            }else{
                loadedFonts.put(path, font.getFamily());
                return font;
            }
        }else{
            return Font.font(loadedFonts.get(path), size);
        }
    }
    
    public static void loadFont(String name){
        if(!loadedFonts.containsKey(name)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream("/appFonts/" + name), -1);
            if(font == null){
                System.out.println("Font " + "/appFonts/" + name + " is null...");
            }else{
                loadedFonts.put(name, font.getFamily());
            }
        }
    }
    public static void loadFontPath(String path){
        if(!loadedFonts.containsKey(path)){
            Font font = Font.loadFont(AppFontsLoader.class.getResourceAsStream(path), -1);
            if(font == null){
                System.out.println("Font " + path + " is null...");
            }else{
                loadedFonts.put(path, font.getFamily());
            }
        }
    }
    
}
