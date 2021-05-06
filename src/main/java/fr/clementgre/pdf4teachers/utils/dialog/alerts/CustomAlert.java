package fr.clementgre.pdf4teachers.utils.dialog.alerts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.StagesUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.Optional;

public class CustomAlert extends Alert{
    
    public CustomAlert(AlertType type, String title, String header, String details){
        super(type);
        setTitle(title);
        setHeaderText(header);
        if(details != null) setContentText(details);
        getButtonTypes().clear();
    
        // OWNER
        if(Main.window != null){
            if(Main.window.getScene() != null) initOwner(Main.window);
            if(MainWindow.paintTab.galleryWindow != null){
                if(MainWindow.paintTab.galleryWindow.isFocused()) initOwner(MainWindow.paintTab.galleryWindow);
            }
        }
        
        // STYLE
        ((Stage) getDialogPane().getScene().getWindow()).getIcons().add(new Image(PaneUtils.class.getResource("/logo.png") + ""));
        StyleManager.putStyle(getDialogPane().getScene(), Style.DEFAULT);
        getDialogPane().getStyleClass().add(JMetroStyleClass.BACKGROUND);
    
        // SCALING
        if(MainWindow.TEMP_SCALE != 1){
            getDialogPane().getScene().setFill(Color.web("#252525"));
            setOnShown((e) -> {
                Platform.runLater(() -> {
                    StagesUtils.resizeStageAccordingToAppScale(this, getDialogPane().getScene());
                    PaneUtils.setupScaling(getDialogPane(), true, false);
                });
            });
        }
        
        
    }
    public CustomAlert(AlertType type, String title, String header){
        this(type, title, header, null);
    }
    public CustomAlert(AlertType type, String title){
        this(type, title, "", null);
    }
    public CustomAlert(AlertType type){
        this(type, "", "", null);
    }
    
    public ButtonType getShowAndWait(){
        Optional<ButtonType> result = showAndWait();
        if(result.isEmpty()) return null;
        else return result.get();
    }
    public void callBackShow(CallBackArg<ButtonType> callback){
        new Thread(() -> callback.call(getShowAndWait()), "CustomAlertThread").start();
    }
    
    // OTHER
    
    public void addButtonType(ButtonType buttonType){
        getButtonTypes().add(buttonType);
    }
    
    // BUTTONS PRESETS
    
    public ButtonType addCustomButton(String text, ButtonBar.ButtonData data){
        ButtonType button = new ButtonType(text, data);
        addButtonType(button);
        return button;
    }
    public ButtonType addLeftButton(String text){
        return addButton(text, ButtonPosition.OTHER_LEFT);
    }
    public ButtonType addRightButton(String text){
        return addButton(text, ButtonPosition.OTHER_RIGHT);
    }
    public ButtonType addDefaultButton(String text){
        return addButton(text, ButtonPosition.DEFAULT);
    }
    public ButtonType addCloseButton(String text){
        return addButton(text, ButtonPosition.CLOSE);
    }
    
    public ButtonType addButton(String text, ButtonPosition pos){
        if(pos == ButtonPosition.DEFAULT){
            return addCustomButton(text, ButtonBar.ButtonData.OK_DONE);
        }else if(pos == ButtonPosition.CLOSE){
            return addCustomButton(text, ButtonBar.ButtonData.NO);
        }else if(pos == ButtonPosition.OTHER_LEFT){
            return addCustomButton(text, ButtonBar.ButtonData.LEFT);
        }else{ // ButtonPosition.OTHER_RIGHT
            return addCustomButton(text, ButtonBar.ButtonData.APPLY);
        }
    }
    public ButtonType addSmallSpace(){
        return addCustomButton("", ButtonBar.ButtonData.SMALL_GAP);
    }
    
    public ButtonType addOKButton(ButtonPosition pos){
        return addButton(TR.tr("actions.ok"), pos);
    }
    public ButtonType addCancelButton(ButtonPosition pos){
        return addButton(TR.tr("actions.cancel"), pos);
    }
    public ButtonType addYesButton(ButtonPosition pos){
        return addButton(TR.tr("actions.yes"), pos);
    }
    public ButtonType addNoButton(ButtonPosition pos){
        return addButton(TR.tr("actions.no"), pos);
    }
    public ButtonType addIgnoreButton(ButtonPosition pos){
        return addButton(TR.tr("actions.ignore"), pos);
    }
    public ButtonType addOpenButton(ButtonPosition pos){
        return addButton(TR.tr("actions.open"), pos);
    }
    public ButtonType addApplyButton(ButtonPosition pos){
        return addButton(TR.tr("actions.apply"), pos);
    }
    public ButtonType addDeleteButton(ButtonPosition pos){
        return addButton(TR.tr("actions.delete"), pos);
    }
    public ButtonType addDenyButton(ButtonPosition pos){
        return addButton(TR.tr("actions.deny"), pos);
    }
    public ButtonType addConfirmButton(ButtonPosition pos){
        return addButton(TR.tr("actions.confirm"), pos);
    }
    
    public enum ButtonPosition{
        DEFAULT,
        CLOSE,
        OTHER_RIGHT,
        OTHER_LEFT
    }
    
}
