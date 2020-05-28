package fr.themsou.utils.components;

import javafx.scene.text.Text;

public class ScratchText extends Text {

    public ScratchText(){
    }
    public ScratchText(String text) {
        super(text);
    }
    public ScratchText(double x, double y, String text) {
        super(x, y, text);
    }

}
