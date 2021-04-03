package fr.clementgre.pdf4teachers.components;

import javafx.scene.control.Slider;

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
    }
}
