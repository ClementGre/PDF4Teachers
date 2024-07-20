/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.*;

public class CopyPasteManager {
    
    public static final KeyCombination KEY_COMB_CUT = new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination KEY_COMB_COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination KEY_COMB_PASTE = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
    
    public enum CopyPasteType {CUT, COPY, PASTE}
    
    public static CopyPasteType getCopyPasteTypeByKeyEvent(KeyEvent e){
        if(KEY_COMB_CUT.match(e)) return CopyPasteType.CUT;
        if(KEY_COMB_COPY.match(e)) return CopyPasteType.COPY;
        if(KEY_COMB_PASTE.match(e)) return CopyPasteType.PASTE;
        return null;
    }
    
    public static void execute(CopyPasteType type){
        if(!Main.window.isFocused()) return;
        // Field Action
        if(doNodeCanPerformAction(Main.window.getScene().getFocusOwner(), type)){
            if(executeOnNode(Main.window.getScene().getFocusOwner(), type)) return;
        }
        // App Feature Action
        executeOnAppFeatures(type);
    }
    
    public static void executeOnAppFeatures(CopyPasteType type){
        switch(type){
            case CUT, COPY -> {
                if(MainWindow.mainScreen.hasDocument(false) && MainWindow.mainScreen.getSelected() != null){
                    Element.copy(MainWindow.mainScreen.getSelected());
                    if(type == CopyPasteType.CUT) MainWindow.mainScreen.getSelected().delete(true, UType.ELEMENT);
                }
            }
            case PASTE -> {
                // Element paste
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                if(Element.ELEMENT_CLIPBOARD_KEY.equals(clipboard.getContent(Main.INTERNAL_FORMAT)) && Element.elementClipboard != null){
                    if(Element.paste()) return;
                }
                
                // New TextElement paste
                String string = clipboard.getString();
                if(string == null) string = clipboard.getRtf();
                if(string == null) string = clipboard.getUrl();
                if(string == null) string = clipboard.getHtml();
                if(string != null){
                    MainWindow.mainScreen.pasteText(string);
                }
            }
        }
    }
    
    public static boolean executeOnNode(Node node, CopyPasteType type){
        TextInputControl field = null;
        if(node instanceof TextInputControl textInputControl){
            field = textInputControl;
        }else if(node instanceof Spinner<?> spinner){
            if(spinner.isEditable()) field = spinner.getEditor();
        }
        
        if(field != null){
            switch(type){
                case CUT -> field.cut();
                case COPY -> field.copy();
                case PASTE -> field.paste();
            }
            return true;
        }
        return false;
    }
    
    
    public static boolean doNodeCanPerformAction(Node node, CopyPasteType type){
        if(type == CopyPasteType.PASTE){
            return Clipboard.getSystemClipboard().hasString();
        }
        
        if(node instanceof TextInputControl field){
            return field.getSelection().getLength() != 0;
            
        }
        if(node instanceof Spinner<?> spinner){
            if(!spinner.isEditable()) return true;
            return spinner.getEditor().getSelection().getLength() != 0;
        }
        
        return false;
    }
    
}
