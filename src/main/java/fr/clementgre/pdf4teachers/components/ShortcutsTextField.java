package fr.clementgre.pdf4teachers.components;

import javafx.event.Event;
import javafx.scene.control.TextField;

public class ShortcutsTextField extends TextField{
    
    public ShortcutsTextField(){
        setOnContextMenuRequested(Event::consume);
        ShortcutsTextArea.setupFilter(this);
    }
    public ShortcutsTextField(String text){
        super(text);
        setOnContextMenuRequested(Event::consume);
        ShortcutsTextArea.setupFilter(this);
    }
    
}
