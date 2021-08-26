/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import javafx.event.Event;
import javafx.scene.control.TextArea;

public class ShortcutsTextArea extends TextArea {
    
    public ShortcutsTextArea(){
        setOnContextMenuRequested(Event::consume);
        ShortcutsTextField.registerNewInput(this);
    }
    public ShortcutsTextArea(String text){
        super(text);
        setOnContextMenuRequested(Event::consume);
        ShortcutsTextField.registerNewInput(this);
    }
    
    
}
