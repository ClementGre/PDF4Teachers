package fr.clementgre.pdf4teachers.components;

import javafx.event.Event;
import javafx.scene.control.TextField;

public class UndoTextField extends TextField{
    
    public UndoTextField(){
        setOnContextMenuRequested(Event::consume);
        UndoTextArea.setupAntiCtrlZFilter(this);
    }
    public UndoTextField(String text){
        super(text);
        setOnContextMenuRequested(Event::consume);
        UndoTextArea.setupAntiCtrlZFilter(this);
    }
    
}
