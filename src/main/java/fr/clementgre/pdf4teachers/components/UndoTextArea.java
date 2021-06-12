package fr.clementgre.pdf4teachers.components;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.interfaces.NonLeakingEventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class UndoTextArea extends TextArea{

    public static final KeyCombination keyCombUndo = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination keyCombRedo = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHIFT_DOWN, KeyCombination.SHORTCUT_DOWN);
    
    public UndoTextArea(){
        setupAntiCtrlZFilter(this);
    }
    public UndoTextArea(String text){
        super(text);
        setupAntiCtrlZFilter(this);
    }
    
    public static void setupAntiCtrlZFilter(Node node){
        node.addEventFilter(KeyEvent.ANY, new NonLeakingEventHandler<>(0, UndoTextArea::onKeyAction));
    }
    
    private static void onKeyAction(NonLeakingEventHandler.EventHandler<KeyEvent, Integer> event){
        KeyEvent e = event.getEvent();
        if(keyCombUndo.match(e)){
            e.consume();
            if(e.getEventType() == KeyEvent.KEY_PRESSED) MainWindow.mainScreen.undo();
        }else if(keyCombRedo.match(e)){
            e.consume();
            if(e.getEventType() == KeyEvent.KEY_PRESSED) MainWindow.mainScreen.redo();
        }
    }
    
}
