/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class StagesUtils {
    
    public static void scaleStage(Stage stage, Scene scene){
        double horizontalShift = stage.getWidth() - scene.getWidth();
        double verticalShift = stage.getHeight() - scene.getHeight();
        
        stage.setMaxWidth(stage.getMaxWidth() * Main.settings.zoom.getValue());
        stage.setMaxHeight(stage.getMaxHeight() * Main.settings.zoom.getValue());
    
        stage.setWidth((stage.getWidth() - horizontalShift) * Main.settings.zoom.getValue() + horizontalShift);
        stage.setHeight((stage.getHeight() - verticalShift) * Main.settings.zoom.getValue() + verticalShift);
    
    }
    
    
    public static void scaleAlert(Alert alert, Scene scene){
        alert.getDialogPane().setMinSize(alert.getDialogPane().getWidth() * Main.settings.zoom.getValue(), alert.getDialogPane().getHeight() * Main.settings.zoom.getValue());
        
        double horizontalShift = alert.getWidth() - scene.getWidth();
        double verticalShift = alert.getHeight() - scene.getHeight();
        alert.setWidth((alert.getWidth() - horizontalShift) * Main.settings.zoom.getValue() + horizontalShift);
        alert.setHeight((alert.getHeight() - verticalShift) * Main.settings.zoom.getValue() + verticalShift);
    }
    public static void scaleAndCenterAlert(Alert alert, Scene scene){
    
        final boolean[] sceneFirstDimensionUpdated = {false};
        InvalidationListener sceneListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable o){
                o.removeListener(this);
                
                if(!sceneFirstDimensionUpdated[0]){
                    sceneFirstDimensionUpdated[0] = true;
                }else{
                    scaleAlert(alert, scene);
                    PaneUtils.setupScaling(alert.getDialogPane(), true, false);
                }
            }
        };
        scene.widthProperty().addListener(sceneListener);
        scene.heightProperty().addListener(sceneListener);
    
    
        if(Main.window == null) {
            return;
        }
    
        final boolean[] stageFirstDimensionUpdated = {false};
        InvalidationListener stageListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable o){
                o.removeListener(this);
            
                if(!stageFirstDimensionUpdated[0]){
                    stageFirstDimensionUpdated[0] = true;
                }else{
                    Main.window.centerWindowIntoMe(alert.getOwner());
                }
            }
        };
        alert.getOwner().widthProperty().addListener(stageListener);
        alert.getOwner().heightProperty().addListener(stageListener);
        
    }
    
}
