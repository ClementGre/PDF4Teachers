package fr.clementgre.pdf4teachers.utils.style;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class StyleManager {

    public static jfxtras.styles.jmetro.Style DEFAULT_STYLE = jfxtras.styles.jmetro.Style.LIGHT;
    public static jfxtras.styles.jmetro.Style ACCENT_STYLE = jfxtras.styles.jmetro.Style.DARK;

    public static void setup(){

        if(Main.settings.isDarkTheme()){
            DEFAULT_STYLE = jfxtras.styles.jmetro.Style.DARK;
        }
        Main.settings.darkThemeProperty().addListener((observable, oldValue, newValue) -> {
            if(Main.settings.isDarkTheme()){
                DEFAULT_STYLE = jfxtras.styles.jmetro.Style.DARK;
                putStylesAuto();
            }else{
                DEFAULT_STYLE = jfxtras.styles.jmetro.Style.LIGHT;
                putStylesAuto();
            }
        });
    }

    public static void putStyle(Scene scene, Style style){
        if(style == Style.DEFAULT){
            new JMetro(scene, DEFAULT_STYLE);
        }else if(style == Style.ACCENT){
            new JMetro(scene, ACCENT_STYLE);
        }
        scene.getStylesheets().add(StyleManager.class.getResource("/css/base.css").toExternalForm());
    }

    public static void putStyle(Parent parent, Style style){
        if(style == Style.DEFAULT){
            new JMetro(parent, DEFAULT_STYLE);
        }else if(style == Style.ACCENT){
            new JMetro(parent, ACCENT_STYLE);
        }
        parent.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        parent.getStylesheets().add(StyleManager.class.getResource("/css/base.css").toExternalForm());
    }

    public static void putCustomStyle(Scene scene, String name){
        scene.getStylesheets().add(StyleManager.class.getResource("/css/" + name).toExternalForm());
    }
    public static void putCustomStyle(Parent parent, String name){
        parent.getStylesheets().add(StyleManager.class.getResource("/css/" + name).toExternalForm());
    }

    private static void putStylesAuto(){
        new JMetro(MainWindow.root, DEFAULT_STYLE);

        MainWindow.textTab.treeView.lastsSection.updateGraphics();
        MainWindow.textTab.treeView.favoritesSection.updateGraphics();
        MainWindow.textTab.treeView.onFileSection.updateGraphics();
        MainWindow.filesTab.sortManager.updateGraphics();
        if(MainWindow.mainScreen.hasDocument(false)){
            MainWindow.mainScreen.document.updateBackgrounds();
        }
    }

    public static String getHexAccentColor(){
        if(DEFAULT_STYLE == jfxtras.styles.jmetro.Style.DARK){
            return "#484848";
        }else{
            return "#cccccc";
        }
    }
    public static Color convertColor(Color color){
        if(DEFAULT_STYLE == jfxtras.styles.jmetro.Style.DARK){
            if(color.getBrightness() <= 0.4){
                return Color.WHITE;
            }else return color;
        }else{
            if(color.getBrightness() >= 0.9){
                return Color.BLACK;
            }else return color;
        }
    }

}
