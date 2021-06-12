package fr.clementgre.pdf4teachers.components;

import javafx.scene.control.TextField;

public class UndoTextField extends TextField{
    
    public UndoTextField(){
        UndoTextArea.setupAntiCtrlZFilter(this);
    }
    public UndoTextField(String text){
        super(text);
        UndoTextArea.setupAntiCtrlZFilter(this);
    }
    
}
