package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class PaneUtils{
    
    public static void setPosition(Region element, double x, double y, double width, double height, boolean force){
        
        if(x >= 0){
            element.setLayoutX(x);
        }
        if(y >= 0){
            element.setLayoutY(y);
        }
        element.setPrefSize(width, height);
        
        if(force){
            element.setStyle("-fx-min-width: " + width + ";");
            element.setStyle("-fx-min-height: " + height + ";");
            element.minWidthProperty().bind(new SimpleDoubleProperty(width));
            element.minHeightProperty().bind(new SimpleDoubleProperty(height));
        }
    }
    
    public static void setHBoxPosition(Region element, double width, double height, Insets margin){
        
        if(width == -1){
            HBox.setHgrow(element, Priority.ALWAYS);
            element.setMaxWidth(Double.MAX_VALUE);
        }else if(width != 0){
            element.setPrefWidth(width);
            element.minWidthProperty().bind(new SimpleDoubleProperty(width));
        }
        if(height == -1){
            VBox.setVgrow(element, Priority.ALWAYS);
        }else if(height != 0){
            element.setPrefHeight(height);
            element.minHeightProperty().bind(new SimpleDoubleProperty(height));
        }
        HBox.setMargin(element, margin);
    }
    
    public static void setHBoxPosition(Region element, double width, double height, double margin){
        setHBoxPosition(element, width, height, new Insets(margin, margin, margin, margin));
    }
    
    public static void setHBoxPosition(Region element, double width, double height, double marginLeftRight, double marginTopBottom){
        setHBoxPosition(element, width, height, new Insets(marginTopBottom, marginLeftRight, marginTopBottom, marginLeftRight));
    }
    
    public static void setVBoxPosition(Region element, double width, double height, Insets margin){
        
        if(width == -1){
            HBox.setHgrow(element, Priority.ALWAYS);
            element.setMaxWidth(Double.MAX_VALUE);
        }else if(width != 0){
            element.setPrefWidth(width);
            element.minWidthProperty().bind(new SimpleDoubleProperty(width));
        }
        if(height == -1){
            VBox.setVgrow(element, Priority.ALWAYS);
        }else if(height != 0){
            element.setPrefHeight(height);
            element.minHeightProperty().bind(new SimpleDoubleProperty(height));
        }
        VBox.setMargin(element, margin);
    }
    
    public static void setVBoxPosition(Region element, double width, double height, double margin){
        setVBoxPosition(element, width, height, new Insets(margin, margin, margin, margin));
    }
    
    public static void setVBoxPosition(Region element, double width, double height, double marginLeftRight, double marginTopBottom){
        setVBoxPosition(element, width, height, new Insets(marginTopBottom, marginLeftRight, marginTopBottom, marginLeftRight));
    }
    
    public static void setMenuSize(Menu menu){
        
        for(MenuItem subMenu : menu.getItems()){
            subMenu.setStyle("-fx-font-size: 13;");
            if(subMenu instanceof Menu){
                setMenuSize((Menu) subMenu);
            }
        }
    }
    
    public static Tooltip genWrappedToolTip(String text){
        return genToolTip(new TextWrapper(text, new Font(14*MainWindow.TEMP_SCALE), (int) (350*MainWindow.TEMP_SCALE)).wrap());
    }
    public static Tooltip genToolTip(String text){
        Tooltip tooltip = new Tooltip(text);
        tooltip.setStyle("-fx-font-size: " + (14*MainWindow.TEMP_SCALE) + ";");
        return tooltip;
    }
    
    public static void setupScaling(Region pane){
        if(MainWindow.TEMP_SCALE == 1) return;
        pane.setScaleX(MainWindow.TEMP_SCALE);
        pane.setScaleY(MainWindow.TEMP_SCALE);
        updateScalePadding(pane);
        pane.widthProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane));
        pane.heightProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane));
        pane.scaleYProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane));
    }
    public static void updateScalePadding(Region pane){
        // -1 to avoid small white borders on sides sometimes
        // Calcul: (ScaledWidth - ShouldBeVisibleWidth)/2 - 1
        double horizontal = (pane.getWidth() - pane.getWidth()/MainWindow.TEMP_SCALE)/2  -1;
        double vertical = (pane.getHeight() - pane.getHeight()/MainWindow.TEMP_SCALE)/2  -1;
    
        pane.setPadding(new Insets(vertical, horizontal, vertical, horizontal));
    }
    
    
}
