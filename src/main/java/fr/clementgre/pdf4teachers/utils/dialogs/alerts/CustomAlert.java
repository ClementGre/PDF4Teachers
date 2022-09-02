/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StagesUtils;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.style.Style;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

import java.util.Optional;

public class CustomAlert extends Alert {
    
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
        if(Main.settings.zoom.getValue() != 1){
            getDialogPane().getScene().setFill(Color.web("#252525"));
            
            // Alert is only scaled the first time it shows up
            final boolean[] alreadyShown = {false};
            setOnShown((e) -> {
                if(alreadyShown[0]) return;
                alreadyShown[0] = true;
                StagesUtils.trysScaleAlertUntilDoable(this, getDialogPane().getScene(), () -> {
                    PaneUtils.setupScaling(getDialogPane(), true, false);
                    if(Main.window != null) Main.window.centerWindowIntoMe(getDialogPane().getScene().getWindow());
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
    public ButtonPosition getShowAndWaitGetButtonPosition(ButtonPosition defaultPosition){
        Optional<ButtonType> result = showAndWait();
        if(result.isEmpty()) return defaultPosition;
        ButtonPosition pos = buttonDataToPosition(result.get().getButtonData(), true);
        if(pos == null) return defaultPosition;
        else return pos;
    }
    // LINUX: NO - YES - BACK
    // WINDOWS: BACK - YES - NO
    // OSX: NO - YES - APPLY
    public ButtonPosition buttonDataToPosition(ButtonBar.ButtonData data, boolean acceptNull){
        if(data == ButtonBar.ButtonData.YES) return ButtonPosition.DEFAULT;
        else if(data == ButtonBar.ButtonData.NO) return ButtonPosition.CLOSE;
        else if(data == ButtonBar.ButtonData.LEFT) return ButtonPosition.OTHER_LEFT;
        else{
            if(!acceptNull) return ButtonPosition.OTHER_RIGHT;
            else if(PlatformUtils.isOSX() && data == ButtonBar.ButtonData.APPLY) return ButtonPosition.OTHER_RIGHT;
            else if(data == ButtonData.BACK_PREVIOUS) return ButtonPosition.OTHER_RIGHT;
        }
        return null;
    }
    public ButtonData buttonPositionToData(ButtonPosition pos){
        if(pos == ButtonPosition.DEFAULT) return ButtonBar.ButtonData.YES;
        else if(pos == ButtonPosition.CLOSE) return ButtonBar.ButtonData.NO;
        else if(pos == ButtonPosition.OTHER_LEFT) return ButtonBar.ButtonData.LEFT;
        else{ // OTHER_RIGHT
            if(PlatformUtils.isOSX()) return ButtonBar.ButtonData.APPLY;
            else return ButtonData.BACK_PREVIOUS;
        }
    }
    
    public boolean getShowAndWaitIsDefaultButton(){
        Optional<ButtonType> result = showAndWait();
        if(result.isEmpty()) return false;
        else return result.get().getButtonData().isDefaultButton();
    }
    public boolean getShowAndWaitIsCancelCloseButton(){
        Optional<ButtonType> result = showAndWait();
        if(result.isEmpty()) return true;
        else return result.get().getButtonData().isCancelButton();
    }
    public boolean getShowAndWaitIsCancelButton(){
        Optional<ButtonType> result = showAndWait();
        
        if(result.isEmpty()) return false;
        else return result.get().getButtonData().isCancelButton();
    }
    public void callBackShow(CallBackArg<ButtonType> callback){
        new Thread(() -> callback.call(getShowAndWait()), "CustomAlertThread").start();
    }
    
    // OTHER
    
    public void addButtonType(ButtonType buttonType){
        getButtonTypes().add(buttonType);
    }
    
    // BUTTONS PRESETS
    
    public String wrapTextForButton(String text){
        return new TextWrapper(text, FontUtils.getDefaultFont(false, false, 14), 200).wrap();
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
    
    public ButtonType addButton(String text, ButtonBar.ButtonData data){
        ButtonType button = new ButtonType(wrapTextForButton(text), data);
        addButtonType(button);
        return button;
    }
    public ButtonType addButton(String text, ButtonPosition pos){
        ButtonType button = getButton(text, pos);
        addButtonType(button);
        return button;
    }
    public ButtonType getButton(String text, ButtonPosition pos){
        return new ButtonType(wrapTextForButton(text), buttonPositionToData(pos));
    }
    public ButtonType addSmallSpace(){
        return addButton("", ButtonBar.ButtonData.SMALL_GAP);
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
    public ButtonType addContinueButton(ButtonPosition pos){
        return addButton(TR.tr("dialog.actionError.continue"), pos);
    }
    
}
