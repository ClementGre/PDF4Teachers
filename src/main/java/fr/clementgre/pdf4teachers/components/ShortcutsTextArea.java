package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.MenuBar;
import fr.clementgre.pdf4teachers.utils.interfaces.NonLeakingEventHandler;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.*;

public class ShortcutsTextArea extends TextArea{

    public static final KeyCombination keyCombUndo = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination keyCombRedo = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN);
    
    public static final KeyCombination keyCombCut = new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination keyCombCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination keyCombPaste = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
    
    public ShortcutsTextArea(){
        setOnContextMenuRequested(Event::consume);
        setupFilter(this);
    }
    public ShortcutsTextArea(String text){
        super(text);
        setOnContextMenuRequested(Event::consume);
        setupFilter(this);
    }
    
    public static void setupFilter(Node node){
        node.addEventFilter(KeyEvent.ANY, new NonLeakingEventHandler<>(0, ShortcutsTextArea::onKeyAction));
    }
    
    private static void onKeyAction(NonLeakingEventHandler.EventHandler<KeyEvent, Integer> event){
        KeyEvent e = event.getEvent();
        if(keyCombUndo.match(e)){
            e.consume();
            // consume is enough only with OSX MenuBar
            if(!MenuBar.isSystemMenuBarSupported()){
                if(e.getEventType() == KeyEvent.KEY_PRESSED) MainWindow.mainScreen.undo();
            }
            
        }else if(keyCombRedo.match(e)){
            e.consume();
            // consume is enough only with OSX MenuBar
            if(!MenuBar.isSystemMenuBarSupported()){
                if(e.getEventType() == KeyEvent.KEY_PRESSED) MainWindow.mainScreen.redo();
            }
            
        }else if(e.getSource() instanceof Node node){ // Cut / Copy / Paste
            if(keyCombCut.match(e) && isNodeEligibleForCopyPasteOverride(node, false)){
                e.consume();
                // consume is enough only with OSX MenuBar
                if(!MenuBar.isSystemMenuBarSupported() && e.getEventType() == KeyEvent.KEY_PRESSED) MainWindow.menuBar.edit3Cut.fire();
            }else if(keyCombCopy.match(e) && isNodeEligibleForCopyPasteOverride(node, false)){
                e.consume();
                // consume is enough only with OSX MenuBar
                if(!MenuBar.isSystemMenuBarSupported() && e.getEventType() == KeyEvent.KEY_PRESSED) MainWindow.menuBar.edit4Copy.fire();
            }else if(keyCombPaste.match(e) && isNodeEligibleForCopyPasteOverride(node, true)){
                e.consume();
                // consume is enough only with OSX MenuBar
                if(!MenuBar.isSystemMenuBarSupported() && e.getEventType() == KeyEvent.KEY_PRESSED){
                    MainWindow.menuBar.edit5Paste.fire();
                    // When source is a Spinner, the event is called twice, this is an unresolved issue since the event will be called twice.
                }
            }
        }
    }
    
    private static boolean isNodeEligibleForCopyPasteOverride(Node node, boolean paste){
        if(paste){
            return !Clipboard.getSystemClipboard().hasString();
        }
        
        if(node instanceof TextInputControl field){
            return isTextInputControlEligibleForCopyOverride(field);
            
        }else if(node instanceof Spinner<?> spinner){
            if(!spinner.isEditable()) return false; // Anyway, the spinner will not consume the KeyEvent
            return isTextInputControlEligibleForCopyOverride(spinner.getEditor());
        }
        
        return false;
    }
    
    private static boolean isTextInputControlEligibleForCopyOverride(TextInputControl field){
        // There is nothing to copy if there is no selection
        return field.getSelection().getLength() == 0;
    }
    
}
