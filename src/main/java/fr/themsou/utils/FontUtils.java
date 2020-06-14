package fr.themsou.utils;

import fr.themsou.document.editions.elements.TextElement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.InputStream;

public class FontUtils {
    public static final ObservableList<String> fonts = FXCollections.observableArrayList(
            "Open Sans", "Jost", "Lato Black", "Lato", "Lato Light", "Roboto Medium", "Roboto", "Roboto Light", "Hind Guntur", "Shanti",
            "Karma", "Noto Serif", "Crimson Text", "Bitter",
            "Ubuntu Condensed", "Bellota", "Balsamiq Sans", "MuseoModerno",  "Averia Libre", "Indie Flower", "Sriracha", "Arrows");
    public static final ObservableList<Integer> sizes = FXCollections.observableArrayList(6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30, 34, 38, 42, 46, 50);

    public static Font getFont(String family, boolean italic, boolean bold, double size){

        return Font.loadFont(getFontFile(family, italic, bold), size);
    }

    public static InputStream getFontFile(String family, boolean italic, boolean bold){

        String fileFontName = getFontFileName(italic, bold);

        InputStream fontFile = TextElement.class.getResourceAsStream("/fonts/" + family + "/" + fileFontName + ".ttf");

        while(fontFile == null){
            if(fileFontName.equals("bold") || fileFontName.equals("italic")){
                fileFontName = "regular";
            }else if(fileFontName.equals("bolditalic")){
                if(TextElement.class.getResourceAsStream("/fonts/" + family + "/italic.ttf") != null) fileFontName = "italic";
                else if(TextElement.class.getResourceAsStream("/fonts/" + family + "/bold.ttf") != null) fileFontName = "bold";
                else fileFontName = "regular";
            }else{
                System.err.println("Erreur : impossible de charger le font : " + family + " en bold=" + bold + " et italic=" + italic + " (fileFontName = " + fileFontName + " ) : Chargement du Font Open Sans classique");
                return TextElement.class.getResourceAsStream("/fonts/Open Sans/regular.ttf");
            }

            fontFile = TextElement.class.getResourceAsStream("/fonts/" + family + "/" + fileFontName + ".ttf");
        }

        return fontFile;
    }

    public static String getFontFileName(boolean italic, boolean bold){

        String fileName = "";
        if(bold) fileName += "bold";
        if(italic) fileName += "italic";
        if(fileName.isEmpty()) fileName = "regular";

        return fileName;
    }

    public static FontWeight getFontWeight(Font font) {

        String[] style = font.getStyle().split(" ");
        if(style.length >= 1){
            if(style[0].equals("Bold")){
                return FontWeight.BOLD;
            }
        }

        return FontWeight.NORMAL;
    }

    public static FontPosture getFontPosture(Font font) {

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
