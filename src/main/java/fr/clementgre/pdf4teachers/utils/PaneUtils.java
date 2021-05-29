package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class PaneUtils{
    
    public static void printParentStructure(Parent parent, int depth){
        for(Node children : parent.getChildrenUnmodifiable()){
            
            System.out.println("   " + "|   ".repeat(Math.max(0, depth-1)) + (depth == 0 ? "-" : "|- ")  +
                    children.getClass().getSimpleName() + " [" + children.getStyleClass().toString() + "]");
            
            if(children instanceof Parent newParent){
                printParentStructure(newParent, depth+1);
            }
        }
    }
    
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
    
    public static Tooltip genWrappedToolTip(String text){
        return genToolTip(new TextWrapper(text, new Font(14*Main.settings.zoom.getValue()), (int) (350*Main.settings.zoom.getValue())).wrap());
    }
    public static Tooltip genToolTip(String text){
        Tooltip tooltip = new Tooltip(text);
        tooltip.setStyle("-fx-font-size: " + (14*Main.settings.zoom.getValue()) + ";");
        return tooltip;
    }
    public static void setupScaling(Region pane, boolean listeners, boolean bind){
        setupScaling(pane, listeners, bind, true, true);
    }
    public static void setupScalingWithoutPadding(Region pane, boolean bind){
        setupScaling(pane, false, bind, false, true);
    }
    public static void setupScaling(Region pane, boolean listeners, boolean bind, boolean updatePadding, boolean paddingAround){
        if(Main.settings.zoom.getValue() == 1 && !bind) return;
        double nonBindScaleValue = Main.settings.zoom.getValue();
        
        pane.setScaleX(Main.settings.zoom.getValue());
        pane.setScaleY(Main.settings.zoom.getValue());
        if(updatePadding){
            updateScalePadding(pane, paddingAround, bind ? Main.settings.zoom.getValue() : nonBindScaleValue);
            if(listeners){
                pane.widthProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane, paddingAround, bind ? Main.settings.zoom.getValue() : nonBindScaleValue));
                pane.heightProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane, paddingAround, bind ? Main.settings.zoom.getValue() : nonBindScaleValue));
                pane.scaleYProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane, paddingAround, bind ? Main.settings.zoom.getValue() : nonBindScaleValue));
            }
        }
        if(bind){
            Main.settings.zoom.valueProperty().addListener((o, oldValue, newValue) -> {
                pane.setScaleX(Main.settings.zoom.getValue());
                pane.setScaleY(Main.settings.zoom.getValue());
                updateScalePadding(pane, paddingAround, Main.settings.zoom.getValue());
            });
        }
    }
    public static void updateScalePadding(Region pane, boolean paddingAround, double scale){
        if(Main.settings.zoom.getValue() == 0){
            pane.setPadding(Insets.EMPTY);
            return;
        }
        
        // -1 to avoid small white borders on sides sometimes
        // Calcul: (ScaledWidth - ShouldBeVisibleWidth)/2 - 1
        double horizontal = (pane.getWidth() - pane.getWidth()/scale)/2  -1;
        double vertical = (pane.getHeight() - pane.getHeight()/scale)/2  -1;
        if(paddingAround){
            pane.setPadding(new Insets(vertical, horizontal, vertical, horizontal));
        }else{
            pane.setPadding(new Insets(vertical*2, 0, 0, horizontal*2));
        }
        
    }
    
    
}
