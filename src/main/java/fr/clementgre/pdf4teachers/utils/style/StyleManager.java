/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.style;

import com.jthemedetecor.OsThemeDetector;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.Objects;

public class StyleManager {
    
    public static jfxtras.styles.jmetro.Style DEFAULT_STYLE = jfxtras.styles.jmetro.Style.LIGHT;
    public static jfxtras.styles.jmetro.Style ACCENT_STYLE = jfxtras.styles.jmetro.Style.DARK;
    
    public static void setup(){
        
        if(OsThemeDetector.isSupported()){
            if(Main.settings.systemTheme.getValue()){
                Main.settings.darkTheme.setValue(OsThemeDetector.getDetector().isDark());
            }
            OsThemeDetector.getDetector().registerListener(isDark -> {
                if(Main.settings.systemTheme.getValue()) Main.settings.darkTheme.setValue(isDark);
            });
        }
        
        if(Main.settings.darkTheme.getValue()){
            DEFAULT_STYLE = jfxtras.styles.jmetro.Style.DARK;
        }
        
        Main.settings.darkTheme.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                DEFAULT_STYLE = jfxtras.styles.jmetro.Style.DARK;
            }else{
                DEFAULT_STYLE = jfxtras.styles.jmetro.Style.LIGHT;
            }
            putStylesAuto();
        });
    }
    
    public static JMetro putStyle(Scene scene, Style style, JMetro jMetro){
        jfxtras.styles.jmetro.Style toApplyStyle;
        if(style == Style.DEFAULT) toApplyStyle = DEFAULT_STYLE;
        else if(style == Style.ACCENT) toApplyStyle = ACCENT_STYLE;
        else toApplyStyle = DEFAULT_STYLE;
        
        if(jMetro == null) jMetro = new JMetro(scene, toApplyStyle);
        else jMetro.setStyle(toApplyStyle);
        
        putCustomStyle(scene, "base.css");
        if(toApplyStyle == jfxtras.styles.jmetro.Style.DARK) putCustomStyle(scene, "base-dark.css");
        else putCustomStyle(scene, "base-light.css");
        return jMetro;
    }
    public static void putStyle(Scene scene, Style style){
        jfxtras.styles.jmetro.Style toApplyStyle;
        if(style == Style.DEFAULT) toApplyStyle = DEFAULT_STYLE;
        else if(style == Style.ACCENT) toApplyStyle = ACCENT_STYLE;
        else toApplyStyle = DEFAULT_STYLE;
        
        new JMetro(scene, toApplyStyle);
        
        putCustomStyle(scene, "base.css");
        if(toApplyStyle == jfxtras.styles.jmetro.Style.DARK) putCustomStyle(scene, "base-dark.css");
        else putCustomStyle(scene, "base-light.css");
    }
    
    public static void putStyle(Parent parent, Style style){
        putStyle(parent, style, true);
    }
    
    public static JMetro putStyle(Parent parent, Style style, JMetro jMetro){
        jfxtras.styles.jmetro.Style toApplyStyle;
        if(style == Style.DEFAULT) toApplyStyle = DEFAULT_STYLE;
        else if(style == Style.ACCENT) toApplyStyle = ACCENT_STYLE;
        else toApplyStyle = DEFAULT_STYLE;
        
        if(jMetro == null){
            jMetro = new JMetro(parent, toApplyStyle);
            parent.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        }else jMetro.setStyle(toApplyStyle);
        
        putCustomStyle(parent, "base.css");
        if(toApplyStyle == jfxtras.styles.jmetro.Style.DARK) putCustomStyle(parent, "base-dark.css");
        else putCustomStyle(parent, "base-light.css");
        return jMetro;
    }
    public static void putStyle(Parent parent, Style style, boolean jMetro){
        jfxtras.styles.jmetro.Style toApplyStyle;
        if(style == Style.DEFAULT) toApplyStyle = DEFAULT_STYLE;
        else if(style == Style.ACCENT) toApplyStyle = ACCENT_STYLE;
        else toApplyStyle = DEFAULT_STYLE;
        
        if(jMetro){
            new JMetro(parent, toApplyStyle);
            parent.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        }
        
        putCustomStyle(parent, "base.css");
        if(toApplyStyle == jfxtras.styles.jmetro.Style.DARK) putCustomStyle(parent, "base-dark.css");
        else putCustomStyle(parent, "base-light.css");
    }
    
    public static void putCustomStyle(Scene scene, String name){
        String link = Objects.requireNonNull(StyleManager.class.getResource("/css/" + name)).toExternalForm();
        scene.getStylesheets().remove(link);
        scene.getStylesheets().add(link);
    }
    
    public static void putCustomStyle(Parent parent, String name){
        String link = Objects.requireNonNull(StyleManager.class.getResource("/css/" + name)).toExternalForm();
        parent.getStylesheets().remove(link);
        parent.getStylesheets().add(link);
    }
    
    private static void putStylesAuto(){
        if(MainWindow.paintTab.galleryWindow != null) MainWindow.paintTab.galleryWindow.updateStyle();
        Main.window.updateStyle();
        
        MainWindow.textTab.treeView.lastsSection.updateGraphics();
        MainWindow.textTab.treeView.favoritesSection.updateGraphics();
        MainWindow.textTab.treeView.onFileSection.updateGraphics();
        
        MainWindow.paintTab.favouriteVectors.updateGraphics();
        MainWindow.paintTab.favouriteImages.updateGraphics();
        MainWindow.paintTab.lastVectors.updateGraphics();
        MainWindow.paintTab.gallery.updateGraphics();
        
        MainWindow.filesTab.sortManager.updateGraphics();
        if(MainWindow.mainScreen.hasDocument(false)){
            MainWindow.mainScreen.document.updateBackgrounds();
        }
        
        MainWindow.paintTab.favouriteVectors.getList().onThemeChanged();
        MainWindow.paintTab.lastVectors.getList().onThemeChanged();
    }
    
    public static String getHexAccentColor(){
        if(DEFAULT_STYLE == jfxtras.styles.jmetro.Style.DARK){
            return "#484848";
        }
        return "#cccccc";
    }
    
    public static Color invertColorWithTheme(Color color){
        if(DEFAULT_STYLE == jfxtras.styles.jmetro.Style.DARK){
            if(color.getBrightness() <= 0.4){
                return Color.WHITE;
            }
            return color;
        }
        double targetBrightness = 0.8;
        
        if(color.getBrightness() >= 0.9){
            return Color.BLACK;
        }
        return color;
    }
    
    static int i;
    
    public static Color shiftColorWithTheme(Color color){
        return shiftColorWithTheme(color, 0.8, 0.2);
    }
    public static Color shiftColorWithTheme(Color color, double darkMinBrt, double lightMaxBrt){
/*
        if(DEFAULT_STYLE == jfxtras.styles.jmetro.Style.DARK){
            return Color.color(
                    StringUtils.clamp(0.6 + color.getRed()*0.4, 0, 1),
                    StringUtils.clamp(0.6 + color.getGreen()*0.4, 0, 1),
                    StringUtils.clamp(0.6 + color.getBlue()*0.4, 0, 1)
            );
        }else if(true){
            return Color.color(
                    StringUtils.clamp(color.getRed()*0.4, 0, 1),
                    StringUtils.clamp(color.getGreen()*0.4, 0, 1),
                    StringUtils.clamp(color.getBlue()*0.4, 0, 1)
            );
        }*/
        
        int r = Math.max((int) (color.getRed() * 255), 1);
        int g = Math.max((int) (color.getGreen() * 255), 1);
        int b = Math.max((int) (color.getBlue() * 255), 1);
        double brt = (r + g + b) / 3d;
        
        double nr;
        double ng;
        double nb;
        
        if(DEFAULT_STYLE == jfxtras.styles.jmetro.Style.DARK){
            
            double minBrt = (int) (255 * darkMinBrt);
            double keepRatioPerOne = .7;
            
            if(brt < minBrt){
                double difBrt = minBrt - brt;
                double rDifBrt = (difBrt * keepRatioPerOne) * r / ((r + g + b) / 3d);
                double gDifBrt = (difBrt * keepRatioPerOne) * g / ((r + g + b) / 3d);
                double bDifBrt = (difBrt * keepRatioPerOne) * b / ((r + g + b) / 3d);
                
                nr = r + rDifBrt + (difBrt * (1 - keepRatioPerOne));
                ng = g + gDifBrt + (difBrt * (1 - keepRatioPerOne));
                nb = b + bDifBrt + (difBrt * (1 - keepRatioPerOne));
            }else return color;
        }else{
            
            double maxBrt = (int) (255 * lightMaxBrt);
            double keepRatioPerOne = .1;
            
            if(brt > maxBrt){
                double difBrt = -maxBrt + brt;
                double rDifBrt = (difBrt * keepRatioPerOne) * r / ((r + g + b) / 3d);
                double gDifBrt = (difBrt * keepRatioPerOne) * g / ((r + g + b) / 3d);
                double bDifBrt = (difBrt * keepRatioPerOne) * b / ((r + g + b) / 3d);
                
                nr = r - rDifBrt - (difBrt * (1 - keepRatioPerOne));
                ng = g - gDifBrt - (difBrt * (1 - keepRatioPerOne));
                nb = b - bDifBrt - (difBrt * (1 - keepRatioPerOne));
            }else return color;
        }
        
        return Color.color(
                MathUtils.clamp(nr / 255d, 0, 1),
                MathUtils.clamp(ng / 255d, 0, 1),
                MathUtils.clamp(nb / 255d, 0, 1)
        );
    }
    
    public static java.awt.Color fxColorToAWT(Color color){
        return new java.awt.Color((float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue(),
                (float) color.getOpacity());
    }
}
