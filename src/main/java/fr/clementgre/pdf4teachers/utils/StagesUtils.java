/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.application.Platform;
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
    public static boolean scaleAlert(Alert stage, Scene scene){
        if(Double.isNaN(stage.getWidth()) || Double.isNaN(stage.getHeight()) || Double.isNaN(scene.getWidth()) || Double.isNaN(scene.getHeight())) return false;
        
        double horizontalShift = stage.getWidth() - scene.getWidth();
        double verticalShift = stage.getHeight() - scene.getHeight();
    
        stage.setWidth((stage.getWidth() - horizontalShift) * Main.settings.zoom.getValue() + horizontalShift);
        stage.setHeight((stage.getHeight() - verticalShift) * Main.settings.zoom.getValue() + verticalShift);
        return true;
    }
    public static void trysScaleAlertUntilDoable(Alert stage, Scene scene, CallBack onScaled){
        trysScaleAlertUntilDoable(stage, scene, onScaled, 0);
    }
    public static void trysScaleAlertUntilDoable(Alert stage, Scene scene, CallBack onScaled, int depth){
        
        if(!scaleAlert(stage, scene)){
            if(depth <= 10){
                Platform.runLater(() -> {
                    trysScaleAlertUntilDoable(stage, scene, onScaled, depth+1);
                });
            }else if(depth <= 30){
                PlatformUtils.runLaterOnUIThread(200, () -> {
                    trysScaleAlertUntilDoable(stage, scene, onScaled, depth+1);
                });
            }
        }else onScaled.call();
    }
    
}
