package fr.clementgre.pdf4teachers.components;

import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SliderWithoutPopup extends Slider{
    
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
            if(e.getCode() == KeyCode.BEGIN || e.getCode() == KeyCode.HOME || e.getCode() == KeyCode.END){
                e.consume();
            }
        });
    }
}
