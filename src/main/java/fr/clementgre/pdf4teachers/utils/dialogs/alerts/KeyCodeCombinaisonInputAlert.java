/*
 * Copyright (c) 2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;

import java.util.stream.Stream;

public class KeyCodeCombinaisonInputAlert extends TextInputAlert {
    
    private KeyCharacterCombination combinaison;
    private final KeyCodeCombination defaultCombinaison;
    private KeyCode keyCode;
    private boolean hasReleased = true;
    private final boolean requireShortcutKey;
    
    private final Label warningLabel = new Label();
    
    public enum Result {
        CANCEL, DELETE, VALIDATE
    }
    
    public KeyCodeCombinaisonInputAlert(String title, String header, KeyCodeCombination defaultCombinaison, boolean requireShortcutKey, boolean removeShortcutOption){
        super(title, header, TR.tr("dialogs.keyCodeCombinaisonInput.details"));
        this.defaultCombinaison = defaultCombinaison;
        this.requireShortcutKey = requireShortcutKey;
        
        if(defaultCombinaison == null) combinaison = new KeyCharacterCombination("");
        else{
            keyCode = defaultCombinaison.getCode();
            combinaison = new KeyCharacterCombination("", defaultCombinaison.getShift(), defaultCombinaison.getControl(),
                    defaultCombinaison.getAlt(), defaultCombinaison.getMeta(), defaultCombinaison.getShortcut());
        }
        warningLabel.setWrapText(true);
        PaneUtils.setVBoxPosition(warningLabel, 0, 75, new Insets(0, 0, 5, 0));
        
        VBox contentVBox = new VBox();
        contentVBox.getChildren().setAll(super.contentHBox, warningLabel);
        
        getDialogPane().setContent(contentVBox);
        
        updateText();
        
        super.input.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if(hasReleased){
                // Reset the combinaison
                hasReleased = false;
                if(e.getCode() != keyCode){ // Events repeats automatically
                    combinaison = new KeyCharacterCombination("");
                    keyCode = null;
                }
            }
            
            
            if(Stream.of(KeyCode.UNDEFINED, KeyCode.SHIFT, KeyCode.CONTROL, KeyCode.ALT, KeyCode.META, KeyCode.COMMAND, KeyCode.SHORTCUT).allMatch(code -> e.getCode() != code)){
                keyCode = e.getCode();
            }else if(e.isShiftDown() && combinaison.getShift() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", KeyCombination.ModifierValue.DOWN, combinaison.getControl(),
                        combinaison.getAlt(), combinaison.getMeta(), combinaison.getShortcut());
            }else if(e.isControlDown() && combinaison.getControl() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), KeyCombination.ModifierValue.DOWN,
                        combinaison.getAlt(), combinaison.getMeta(), combinaison.getShortcut());
            }else if(e.isAltDown() && combinaison.getAlt() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), combinaison.getControl(),
                        KeyCombination.ModifierValue.DOWN, combinaison.getMeta(), combinaison.getShortcut());
            }else if(e.isMetaDown() && combinaison.getMeta() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), combinaison.getControl(),
                        combinaison.getAlt(), KeyCombination.ModifierValue.DOWN, combinaison.getShortcut());
            }else if(e.isShortcutDown() && combinaison.getShortcut() != KeyCombination.ModifierValue.DOWN){
                combinaison = new KeyCharacterCombination("", combinaison.getShift(), combinaison.getControl(),
                        combinaison.getAlt(), combinaison.getMeta(), KeyCombination.ModifierValue.DOWN);
            }
            
            updateText();
            e.consume();
        });
        super.input.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            hasReleased = true;
            e.consume();
        });
        super.input.addEventFilter(KeyEvent.KEY_TYPED, Event::consume);
        super.input.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!oldValue.isEmpty() && newValue.isEmpty()){
                keyCode = null;
                combinaison = new KeyCharacterCombination("");
            }
            updateText();
        });
        
        if(removeShortcutOption){
            addButton(TR.tr("dialogs.keyCodeCombinaisonInput.deleteCombinaison"), ButtonPosition.OTHER_LEFT);
        }
        
        Platform.runLater(() -> {
            super.input.deselect();
            super.input.end();
        });
    }
    
    private void updateText(){
        super.input.setText(combinaison.getDisplayText() + (keyCode == null ? "" : keyCode.getChar()));
        super.input.deselect();
        super.input.end();
        
        KeyCodeCombination combination = getKeyCodeCombinaison();
        if(combination != null){
            String alreadyExistingShortcutName = MainWindow.keyboardShortcuts.getShortcutNameIfExists(combination);
            if(!combination.equals(defaultCombinaison) && alreadyExistingShortcutName != null){
                warningLabel.setStyle("-fx-text-fill: #f83c3c;");
                warningLabel.setText(TR.tr("shortcuts.dialog.alreadyExistingCombinaison", alreadyExistingShortcutName));
                return;
            }
        }
        warningLabel.setStyle("");
        warningLabel.setText(TR.tr("dialogs.keyCodeCombinaisonInput.info"));
    }
    
    public KeyCodeCombination getKeyCodeCombinaison(){
        if(keyCode == null) return null;
        return new KeyCodeCombination(keyCode, combinaison.getShift(), combinaison.getControl(),
                combinaison.getAlt(), combinaison.getMeta(), combinaison.getShortcut());
    }
    public boolean isCombinaisonContainingAnyShortcutKey(){
        return combinaison.getControl() == KeyCombination.ModifierValue.DOWN || combinaison.getAlt() == KeyCombination.ModifierValue.DOWN
                || combinaison.getMeta() == KeyCombination.ModifierValue.DOWN || combinaison.getShortcut() == KeyCombination.ModifierValue.DOWN;
    }
    
    public Result showAndWaitGetResult(){
        ButtonPosition button = super.getShowAndWaitGetButtonPosition(ButtonPosition.CLOSE);
        if(button == ButtonPosition.DEFAULT){
            if(keyCode != null && (requireShortcutKey && !isCombinaisonContainingAnyShortcutKey())){
                new WrongAlert(TR.tr("dialogs.keyCodeCombinaisonInput.error.noCombinaison.header"),
                        TR.tr("dialogs.keyCodeCombinaisonInput.error.noCombinaison.details"), false).showAndWait();
                return showAndWaitGetResult();
            }
            return Result.VALIDATE;
        }
        if(button == ButtonPosition.OTHER_LEFT){
            return Result.DELETE;
        }
        return Result.CANCEL;
    }
    
    
    
}
