/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.components;

import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.stream.Stream;

public class SliderWithoutPopup extends Slider {
    
    public SliderWithoutPopup(){
        setup();
    }
    
    public SliderWithoutPopup(double min, double max, double value){
        super(min, max, value);
        setup();
    }
    
    private void setup(){
        getStyleClass().add("slider-without-popup");
        
        // Prevent sliders (footer slider) to move while using these specials keys.
        addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if(Stream.of(KeyCode.BEGIN, KeyCode.HOME, KeyCode.END).anyMatch(keyCode -> e.getCode() == keyCode)){
                e.consume();
            }
        });
    }
}
