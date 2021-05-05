package fr.clementgre.pdf4teachers.utils.dialog;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialog.alerts.CustomAlert;
import fr.clementgre.pdf4teachers.utils.dialog.alerts.OKAlert;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.io.File;
import java.util.List;

public class DialogBuilder{
    
    /* OPEN DIR ALERT */
    
    public static void showAlertWithOpenDirButton(String title, String header, String details, File dirToBrowse){
        showAlertWithOpenDirButton(title, header, details, dirToBrowse.getAbsolutePath());
    }
    public static void showAlertWithOpenDirButton(String title, String header, String details, String pathToBrowse){
        CustomAlert alert = new CustomAlert(Alert.AlertType.INFORMATION, title, header, details);
        alert.addOKButton(ButtonPosition.CLOSE);
        alert.addButton(TR.tr("dialog.file.openFolderButton"), ButtonPosition.DEFAULT);
        
        if(alert.getShowAndWaitIsDefaultButton()) PlatformUtils.openDirectory(pathToBrowse);
    }
    
    /* TO REMOVE */
    
    public static <T> ChoiceDialog<T> getChoiceDialog(T selected, List<T> values){
        ChoiceDialog<T> alert = new ChoiceDialog<T>(selected, values);
        
        if(Main.window != null){
            if(Main.window.getScene() != null) alert.initOwner(Main.window);
        }
        
        setupDialog(alert);
        return alert;
    }
    
    public static void setupDialog(Dialog<?> dialog){
        
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(new Image(PaneUtils.class.getResource("/logo.png") + ""));
        StyleManager.putStyle(dialog.getDialogPane().getScene(), Style.DEFAULT);
        dialog.getDialogPane().getStyleClass().add(JMetroStyleClass.BACKGROUND);
        
        if(dialog.getDialogPane().getScene().getRoot() instanceof DialogPane pane){
            PaneUtils.setupScaling(pane, true, false);
            pane.widthProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane, dialog.getDialogPane().getScene()));
            pane.heightProperty().addListener((observable, oldValue, newValue) -> updateScalePadding(pane, dialog.getDialogPane().getScene()));
            
            pane.setMaxWidth(700*MainWindow.TEMP_SCALE);
            pane.setMaxHeight(Double.MAX_VALUE);
            
        }else throw new RuntimeException("Dialog Parent is not an instance of DialogPane, can't apply scaling... (class: " + dialog.getDialogPane().getScene().getRoot() + ")");
        
        // prevent being too small on some linux distributions AND when scaling dialog
        if(Main.DEBUG) dialog.setResizable(true);
        dialog.setOnShowing(e -> new Thread(() -> {
            while(!dialog.isShowing()){
                try{ Thread.sleep(10); }catch(InterruptedException ex){ ex.printStackTrace(); }
            }
            Platform.runLater(() -> {
                dialog.getDialogPane().getScene().getWindow().setWidth(pane.getLayoutBounds().getWidth() + 30*MainWindow.TEMP_SCALE);
                dialog.getDialogPane().getScene().getWindow().setHeight(pane.getLayoutBounds().getHeight() + 40*MainWindow.TEMP_SCALE);
            });

        }, "AlertResizer").start());
    }
    
    public static void updateScalePadding(Region pane, Scene scene){
        scene.setFill(pane.getBackground().getFills().get(0).getFill());
    }
}
