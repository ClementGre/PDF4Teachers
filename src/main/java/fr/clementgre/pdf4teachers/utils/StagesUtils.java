package fr.clementgre.pdf4teachers.utils;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class StagesUtils{
    
    public static void resizeStageAccordingToAppScale(Stage stage, Scene scene){
        double horizontalShift = stage.getWidth() - scene.getWidth();
        double verticalShift = stage.getHeight() - scene.getHeight();
        stage.setWidth((stage.getWidth()-horizontalShift) * MainWindow.TEMP_SCALE + horizontalShift);
        stage.setHeight((stage.getHeight()-verticalShift) * MainWindow.TEMP_SCALE + verticalShift);
    }
    public static void resizeStageAccordingToAppScale(Alert stage, Scene scene){
        double horizontalShift = stage.getWidth() - scene.getWidth();
        double verticalShift = stage.getHeight() - scene.getHeight();
        stage.setWidth((stage.getWidth()-horizontalShift) * MainWindow.TEMP_SCALE + horizontalShift);
        stage.setHeight((stage.getHeight()-verticalShift) * MainWindow.TEMP_SCALE + verticalShift);
    }
    
}
