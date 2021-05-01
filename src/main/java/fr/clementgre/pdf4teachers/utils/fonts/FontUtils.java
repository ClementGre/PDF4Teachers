package fr.clementgre.pdf4teachers.utils.fonts;

import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class FontUtils{
    
    private static final ObservableList<String> DEFAULT_FONTS = FXCollections.observableArrayList(
            "Open Sans", "Jost", "Lato Black", "Lato", "Lato Light", "Roboto Medium", "Roboto", "Roboto Light", "Hind Guntur", "Shanti",
            "Karma", "Noto Serif", "Crimson Text", "Bitter",
            "Ubuntu Condensed", "Bellota", "Balsamiq Sans", "MuseoModerno", "Averia Libre", "Indie Flower", "Sriracha", "Arrows");
    public static final ObservableList<Double> sizes = FXCollections.observableArrayList(6d, 8d, 9d, 10d, 11d, 12d, 13d, 14d, 15d, 16d, 17d, 18d, 20d, 22d, 24d, 26d, 28d, 30d, 34d, 38d, 42d, 46d, 50d);
    
    private static SystemFontsMapper systemFontsMapper = new SystemFontsMapper();
    
    public static void setup(){
        systemFontsMapper.updateFontsMap();
    }
    
    public static void fontsUpdated(){
        MainWindow.textTab.updateFonts(getAllFonts());
    }
    
    public static ObservableList<String> getAllFonts(){
        ArrayList<String> defaultFonts = new ArrayList<>(DEFAULT_FONTS);
        defaultFonts.addAll(systemFontsMapper.getFontsPackMap().keySet());
        return FXCollections.observableList(defaultFonts);
    }
    
    public static Font getFont(String family, boolean italic, boolean bold, double size){
        return Font.loadFont(getFontFile(family, italic, bold), size);
    }
    
    public static InputStream getFontFile(String family, boolean italic, boolean bold){
        
        if(DEFAULT_FONTS.contains(family)){
            InputStream font = getDefaultFontFile(family, bold, italic);
            if(font != null) return font;
            System.err.println("Error: Unable to load default font " + family + " italic: " + italic + " bold: " + bold + ". Returning system font or default font...");
        }
        if(systemFontsMapper.hasFontPackFromName(family)){
            InputStream font = null;
            try{
                FontPack pack = systemFontsMapper.getFontPackFromName(family);
                if(italic && bold && pack.isHasBoldItalic()){
                    font = new FileInputStream(pack.getBoldItalicPath());
                }else if(italic && pack.isHasItalic()){
                    font = new FileInputStream(pack.getItalicPath());
                }else if(bold && pack.isHasBold()){
                    font = new FileInputStream(pack.getBoldPath());
                }else{
                    font = new FileInputStream(pack.getPath());
                }
            }catch(FileNotFoundException e){ e.printStackTrace(); }
            
            if(font != null) return font;
            System.out.println("Error: Unable to load system font " + family + " italic: " + italic + " bold: " + bold + ". Returning default font...");
        }
        
        return getDefaultFont();
    }
    
    public static InputStream getDefaultFontFile(String family, boolean italic, boolean bold){
        
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
                System.err.println("Erreur : impossible de charger le font : " + family + " en bold=" + bold + " et italic=" + italic + " (fileFontName = " + fileFontName + " )");
                return null;
            }
            
            fontFile = TextElement.class.getResourceAsStream("/fonts/" + family + "/" + fileFontName + ".ttf");
        }
        
        return fontFile;
    }
    
    public static InputStream getDefaultFont(){
        return TextElement.class.getResourceAsStream("/fonts/Open Sans/regular.ttf");
    }
    
    public static String getDefaultFontFileName(boolean italic, boolean bold){
        
        String fileName = "";
        if(bold) fileName += "bold";
        if(italic) fileName += "italic";
        if(fileName.isEmpty()) fileName = "regular";
        
        return fileName;
    }
    
    public static FontWeight getFontWeight(Font font){
        
        String[] style = font.getStyle().split(" ");
        if(style.length >= 1){
            if(style[0].equals("Bold")){
                return FontWeight.BOLD;
            }
        }
        
        return FontWeight.NORMAL;
    }
    
    public static FontPosture getFontPosture(Font font){
        
        String[] style = font.getStyle().split(" ");
        if(style.length == 1){
            if(style[0].equals("Italic")){
                return FontPosture.ITALIC;
            }
        }else if(style.length == 2){
            if(style[1].equals("Italic")){
                return FontPosture.ITALIC;
            }
        }
        return FontPosture.REGULAR;
    }
}
