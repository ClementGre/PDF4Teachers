/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoEngine;
import fr.clementgre.pdf4teachers.interfaces.CopyPasteManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import fr.clementgre.pdf4teachers.utils.interfaces.NonLeakingEventHandler;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;

public class ShortcutsTextField extends TextField {
    
    public ShortcutsTextField(){
        setOnContextMenuRequested(Event::consume);
        registerNewInput(this);
    }
    public ShortcutsTextField(String text){
        super(text);
        setOnContextMenuRequested(Event::consume);
        registerNewInput(this);
    }
    
    public static void registerNewInput(Node node){
        node.addEventFilter(KeyEvent.ANY, new NonLeakingEventHandler<>(0, ShortcutsTextField::onKeyAction));
    }
    
    private static void onKeyAction(NonLeakingEventHandler.EventHandler<KeyEvent, Integer> event){

        KeyEvent e = event.getEvent();
        // Consume node event if event is eligible for being overridden by the others features of the app.
        // On OSX, consume is enough and the MenuBar event will be called,
        // but with JFX MenuBar, we consume the menu bar event at the same time.
        
        if(UndoEngine.KEY_COMB_UNDO.match(e)){ // UNDO
            e.consume();
            if(!MenuBar.isSystemMenuBarSupported()){
                if(e.getEventType() == KeyEvent.KEY_PRESSED) {
                    MainWindow.mainScreen.undo();
                }
            }
            
        }else if(UndoEngine.KEY_COMB_REDO.match(e)){ // REDO
            e.consume();
            if(!MenuBar.isSystemMenuBarSupported()){
                if(e.getEventType() == KeyEvent.KEY_PRESSED) {
                    MainWindow.mainScreen.redo();
                }
            }
            
        }else if(e.getSource() instanceof TextInputControl){ // Cut / Copy / Paste
            CopyPasteManager.CopyPasteType type = CopyPasteManager.getCopyPasteTypeByKeyEvent(e);
            if(type != null){
                e.consume();
                if(!MenuBar.isSystemMenuBarSupported() && e.getEventType() == KeyEvent.KEY_PRESSED){
                    CopyPasteManager.execute(type);
                }

            }
        }
    }
}
