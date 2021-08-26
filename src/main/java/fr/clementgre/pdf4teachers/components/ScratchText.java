/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import javafx.scene.text.Text;

public class ScratchText extends Text {
    
    public ScratchText(){
    }
    
    public ScratchText(String text){
        super(text);
    }
    
    public ScratchText(double x, double y, String text){
        super(x, y, text);
    }
    
}
