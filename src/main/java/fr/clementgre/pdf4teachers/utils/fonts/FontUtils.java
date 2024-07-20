/*
 * Copyright (c) 2020-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.MoreCollectors;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class FontUtils {
    
    private static final LinkedHashMap<String, Boolean> DEFAULT_FONTS = (LinkedHashMap<String, Boolean>) Stream.of(
                    "Open Sans", "Jost", "Lato Black", "Lato", "Lato Light", "Roboto Medium", "Roboto", "Roboto Light", "Hind Guntur", "Shanti",
                    "Karma", "Noto Serif", "Crimson Text", "Bitter",
                    "Ubuntu Condensed", "Bellota", "Balsamiq Sans", "MuseoModerno", "Averia Libre", "Indie Flower", "Sriracha", "Arrows")
            .collect(MoreCollectors.toLinkedMap(Function.identity(), (s) -> false));
    
    private static final SystemFontsMapper systemFontsMapper = new SystemFontsMapper();
    private static boolean loaded;
    
    public static void setup(){
        // This is not needed since system fonts are always loaded and available.
        //systemFontsMapper.loadFontsFromSystemFiles();
    }
    
    public static void fontsUpdated(){
        loaded = true;
        MainWindow.textTab.fontCombo.updateFonts();
    }
    
    public static ObservableList<String> getAllFonts(){
        ArrayList<String> defaultFonts = new ArrayList<>(DEFAULT_FONTS.keySet());
        defaultFonts.addAll(systemFontsMapper.getFonts().keySet().stream().sorted(String::compareToIgnoreCase).toList());
        return FXCollections.observableList(defaultFonts);
    }
    public static List<FontPaths> getSystemFonts(){
        return systemFontsMapper.getFonts().values().stream().toList();
    }
    
    public static SystemFontsMapper getSystemFontsMapper(){
        return systemFontsMapper;
    }
    
    /*
     * getFont is used to get a font securely: if the font isn't loaded, it will load it, and if it does not exist, it will use the default font.
     */
    public static Font getFont(String family, boolean italic, boolean bold, double size){
        if(isDefaultFont(family)){
            if(!isFontAlreadyLoaded(family)) loadFont(family);
            return initFont(family, italic, bold, size);
        }
        if(isSystemFont(family)){
            AutoTipsManager.showByAction("useSystemFont");
            return initFont(family, italic, bold, size);
        }
        return getDefaultFont(italic, bold, size);
    }
    public static Font getDefaultFont(boolean italic, boolean bold, double size){
        return getFont("Open Sans", italic, bold, size);
    }
    
    /*
     * initFont should be called only in this class.
     * It is a dangerous method since the font could be not loaded yet. This method just initialise a font object with the given parameters.
     */
    private static Font initFont(String family, boolean italic, boolean bold, double size){
        return Font.font(family, bold ? FontWeight.BOLD : FontWeight.NORMAL, italic ? FontPosture.ITALIC : FontPosture.REGULAR, size);
    }
    private static Font initDefaultFont(boolean italic, boolean bold, double size){
        return initFont("Open Sans", italic, bold, size);
    }
    
    /*
     * Load and check if a font is loaded (only for default fonts)
     */
    private static void loadFont(String family){
        if(isDefaultFont(family)){
            InputStream font = getDefaultFontFile(family, false, false);
            if(font != null) Font.loadFont(font, -1);
            font = getDefaultFontFile(family, true, false);
            if(font != null) Font.loadFont(font, -1);
            font = getDefaultFontFile(family, false, true);
            if(font != null) Font.loadFont(font, -1);
            font = getDefaultFontFile(family, true, true);
            if(font != null) Font.loadFont(font, -1);
            DEFAULT_FONTS.put(family, true); // mark the font as loaded.
        }
    }
    private static boolean isFontAlreadyLoaded(String family){
        if(isDefaultFont(family)){
            return DEFAULT_FONTS.get(family);
        }
        throw new RuntimeException("Font family " + family + " is not a default font");
    }
    public static boolean isDefaultFont(String family){
        return DEFAULT_FONTS.containsKey(family);
    }
    public static boolean isSystemFont(String family){
        if(!loaded) return true;
        return systemFontsMapper.hasFont(family);
    }
    
    /*
     * get a system/default font file.
     * This function shouldn't be called outside of this class, but the exporters classes need it to add the font file to the PDF.
     */
    public static InputStream getFontFile(String family, boolean italic, boolean bold) throws FileNotFoundException {
        
        if(isDefaultFont(family)){
            InputStream font = getDefaultFontFile(family, italic, bold);
            if(font != null){
                return font;
            }
            Log.e("Unable to load default font " + family + " italic: " + italic + " bold: " + bold + ". Returning system font or default font...");
        }
        if(isSystemFont(family)){
            return getSystemFontFiles(family, italic, bold);
        }
        
        return getDefaultFontFile("Open Sans", italic, bold);
    }
    private static InputStream getDefaultFontFile(String family, boolean italic, boolean bold){
        
        String fileFontName = getDefaultFontFileName(italic, bold);
        InputStream fontFile = TextElement.class.getResourceAsStream("/fonts/" + family + "/" + fileFontName + ".ttf");
        
        while(fontFile == null){
            if(fileFontName.equals("bold") || fileFontName.equals("italic")){
                fileFontName = "regular";
            }else if(fileFontName.equals("bolditalic")){
                if(TextElement.class.getResourceAsStream("/fonts/" + family + "/italic.ttf") != null)
                    fileFontName = "italic";
                else if(TextElement.class.getResourceAsStream("/fonts/" + family + "/bold.ttf") != null)
                    fileFontName = "bold";
                else fileFontName = "regular";
            }else{
                Log.e("Impossible de charger le font : " + family + " en bold=" + bold + " et italic=" + italic + " (fileFontName = " + fileFontName + " )");
                return null;
            }
            
            fontFile = TextElement.class.getResourceAsStream("/fonts/" + family + "/" + fileFontName + ".ttf");
        }
        
        return fontFile;
    }
    private static InputStream getSystemFontFiles(String family, boolean italic, boolean bold) throws FileNotFoundException{
        FontPaths paths = systemFontsMapper.getFontPathsFromName(family);
        
        if(italic && bold && paths.getBoldItalic() != null){
            return new FileInputStream(paths.getBoldItalic().getPath());
        }
        if(bold && paths.getBold() != null){
            return new FileInputStream(paths.getBold().getPath());
        }
        if(italic && paths.getItalic() != null){
            return new FileInputStream(paths.getItalic().getPath());
        }
        if(paths.getRegular() != null){
            return new FileInputStream(paths.getRegular().getPath());
        }
        return null;
    }
    
    
    public static boolean isFontsLoaded(){
        return loaded;
    }
    
    public static String getDefaultFontFileName(boolean italic, boolean bold){
        
        String fileName = "";
        if(bold) fileName += "bold";
        if(italic) fileName += "italic";
        if(fileName.isEmpty()) fileName = "regular";
        
        return fileName;
    }
    
    public static FontWeight getFontWeight(Font font){
        return getFontWeight(font, true);
    }
    public static FontWeight getFontWeight(Font font, boolean onlyOneBold){
        String[] style = font.getStyle().split(" ");
        
        if(style.length >= 1){
            String name = style[0].toLowerCase();
            name = name.replace("extralight", "Extra Light");
            name = name.replace("ultralight", "Ultra Light");
            name = name.replace("semibold", "Semi Bold");
            name = name.replace("demibold", "Demi Bold");
            name = name.replace("extrabold", "Extra Bold");
            name = name.replace("ultrabold", "Ultra Bold");
            name = name.replace("boldoblique", "Bold");
            
            FontWeight fontWeight = FontWeight.findByName(name);
            if(fontWeight != null && fontWeight != FontWeight.NORMAL){
                if(onlyOneBold) return FontWeight.BOLD;
                return fontWeight;
            }
        }
        return FontWeight.NORMAL;
    }
    
    public static FontPosture getFontPosture(Font font){
        String[] style = font.getStyle().split(" ");
        
        if(StringUtils.containsIgnoreCase(style, "Italic") || StringUtils.containsIgnoreCase(style, "Oblique")
                || StringUtils.containsIgnoreCase(style, "Slanted") || StringUtils.endsIn(style, "Oblique", false)){
            return FontPosture.ITALIC;
        }
        return FontPosture.REGULAR;
    }
}
