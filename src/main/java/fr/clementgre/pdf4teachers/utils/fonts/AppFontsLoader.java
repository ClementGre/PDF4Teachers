package fr.clementgre.pdf4teachers.utils.fonts;

import javafx.scene.text.Font;
import java.util.HashMap;

public class AppFontsLoader{
    
    private static HashMap<String, String> loadedFonts = new HashMap<>();
    
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
    
}
