package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.dialog.AlertIconType;
import org.w3c.dom.Text;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Originally created by azakhary on 4/24/2015
 * see https://github.com/UnderwaterApps/overlap2d/blob/master/overlap2d/src/com/uwsoft/editor/proxy/FontManager.java
 * (Apache 2.0 license)
 * Edited by Clement Grennerat
 */
public class SystemFontsMapper{

    private HashMap<String, FontPack> systemFontMap = new HashMap<>();
    
    public static String[] getSystemFontNames() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }
    
    public String[] getSystemFontsPaths() {
        String[] result;
        if (Main.isWindows()){
            result = new String[1];
            String path = System.getenv("WINDIR");
            result[0] = path + "\\" + "Fonts";
            return result;
            
        }else if (Main.isOSX()){
            result = new String[3];
            result[0] = System.getProperty("user.home") + File.separator + "Library/Fonts";
            result[1] = "/Library/Fonts";
            result[2] = "/System/Library/Fonts";
            return result;
            
        }else if (Main.isLinux()){
            String[] pathsToCheck = {
                    System.getProperty("user.home") + File.separator + ".fonts",
                    "/usr/share/fonts/truetype",
                    "/usr/share/fonts/TTF"
            };
            ArrayList<String> resultList = new ArrayList<>();
            
            for (int i = pathsToCheck.length - 1; i >= 0; i--) {
                String path = pathsToCheck[i];
                File tmp = new File(path);
                if (tmp.exists() && tmp.isDirectory() && tmp.canRead()) {
                    resultList.add(path);
                }
            }
            
            if(resultList.isEmpty()){
                MainWindow.showNotification(AlertIconType.WARNING, "Error: unable to load system fonts, no directory is readable", 30);
                result = new String[0];
            }else{
                result = new String[resultList.size()];
                result = resultList.toArray(result);
            }
            return result;
        }
        return null;
    }
    
    public List<File> getSystemFontFiles() {
        // only retrieving ttf files
        String[] extensions = new String[]{"ttf", "otf", "ttc", "dfont"};
        String[] paths = getSystemFontsPaths();
        ArrayList<File> files = new ArrayList<>();
    
        for(String path : paths){
            File fontDirectory = new File(path);
            if(fontDirectory.exists()) files.addAll(FilesUtils.listFiles(fontDirectory, extensions, true));
        }
        return files;
    }
    
    public void updateFontsMap(){
        new Thread(() -> {
            long time = System.currentTimeMillis();
            for(File file : getSystemFontFiles()){
                try{
                    Font[] fonts = Font.createFonts(new FileInputStream(file.getAbsolutePath()));
                    for(Font font : fonts){
                        addFontToMap(font, file.getAbsolutePath());
                    }
                }catch(FontFormatException | IOException e){ e.printStackTrace(); }
            }
            double ping = (System.currentTimeMillis() - time) / 1000d;
            System.out.println("Loaded " + systemFontMap.size() + " fonts in " + ping + "s");
            FontUtils.fontsUpdated();
        }, "System fonts loader").start();
    }
    
    private void addFontToMap(Font font, String path){
        FontPack fontPack;
        String family = font.getFamily();
        float weight = getWeight(font);
        
        if(weight == TextAttribute.WEIGHT_REGULAR || weight == TextAttribute.WEIGHT_BOLD){
            if(systemFontMap.containsKey(family)){
                fontPack = getFontPackFromName(family);
                if(isItalic(font) && weight == TextAttribute.WEIGHT_BOLD){
                    fontPack.setHasBoldItalic(true);
                    fontPack.setBoldItalicPath(path);
                }else if(isItalic(font)){
                    fontPack.setHasItalic(true);
                    fontPack.setItalicPath(path);
                }else if(weight == TextAttribute.WEIGHT_BOLD){
                    fontPack.setHasBold(true);
                    fontPack.setBoldPath(path);
                }else fontPack.setPath(path);
        
            }else{
                fontPack = new FontPack(family, path);
                if(isItalic(font) && weight == TextAttribute.WEIGHT_BOLD){
                    fontPack.setHasBoldItalic(true);
                    fontPack.setBoldItalicPath(path);
                }else if(isItalic(font)){
                    fontPack.setHasItalic(true);
                    fontPack.setItalicPath(path);
                }else if(weight == TextAttribute.WEIGHT_BOLD){
                    fontPack.setHasBold(true);
                    fontPack.setBoldPath(path);
                }
            }
            
        }else{ // Font with special weight --> apart
            family = font.getFamily() + " " + getWeightNameFromValue(weight);
            if(systemFontMap.containsKey(font.getFamily())){
                fontPack = getFontPackFromName(family);
                if(isItalic(font)){
                    fontPack.setHasItalic(true);
                    fontPack.setItalicPath(path);
                }else fontPack.setPath(path);
            }else{
                fontPack = new FontPack(family, path);
                if(isItalic(font)){
                    fontPack.setHasItalic(true);
                    fontPack.setItalicPath(path);
                }
            }
        }
        System.out.println("Indexing font " + family + " ("
                + (fontPack.isHasItalic() ? " +Italic" : "") + (fontPack.isHasBold() ? " +Bold" : "") + (fontPack.isHasBoldItalic() ? " +BoldItalic" : "") + ")");
        systemFontMap.put(family, fontPack);
    }
    
    private float getWeight(Font font){
        if(font.getAttributes().get(TextAttribute.WEIGHT) instanceof Number value){
            return value.floatValue();
        }
        return TextAttribute.WEIGHT_REGULAR;
    }
    private boolean isItalic(Font font){
        if(font.getAttributes().get(TextAttribute.POSTURE) instanceof Number value){
            return value.floatValue() == TextAttribute.POSTURE_OBLIQUE;
        }
        return false;
    }
    private String getWeightNameFromValue(float value){
        if(value == 0.5f){
            return "Extra Light";
        }else if(value == 0.75f){
            return "Light";
        }else if(value == 0.875f){
            return "Demi Light";
        }else if(value == 1f){
            return "Regular";
        }else if(value == 1.25f){
            return "Semi Bold";
        }else if(value == 1.5f){
            return "Medium";
        }else if(value == 1.75f){
            return "Demi Bold";
        }else if(value == 2f){
            return "Bold";
        }else if(value == 2.25f){
            return "Heavy";
        }else if(value == 2.55f){
            return "Extra Bold";
        }else if(value == 2.75f){
            return "Ultra Bold";
        }
        return "Regular";
    }
    
    public HashMap<String, FontPack> getFontsPackMap() {
        return systemFontMap;
    }
    public FontPack getFontPackFromName(String fontFamily) {
        return systemFontMap.get(fontFamily);
    }
    public boolean hasFontPackFromName(String fontFamily) {
        return systemFontMap.containsKey(fontFamily);
    }
}
