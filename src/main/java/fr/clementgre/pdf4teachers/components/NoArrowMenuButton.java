package fr.clementgre.pdf4teachers.components;

import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class NoArrowMenuButton extends MenuButton{
    
    private static final String STYLE_CLASS = "noArrowMenuButton";
    
    public NoArrowMenuButton(){
        getStyleClass().add(STYLE_CLASS);
    }
    
    public NoArrowMenuButton(String text){
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
    
    public NoArrowMenuButton(String text, Node graphic){
        super(text, graphic);
        getStyleClass().add(STYLE_CLASS);
    }
    
    public NoArrowMenuButton(String text, Node graphic, MenuItem... items){
        super(text, graphic, items);
        getStyleClass().add(STYLE_CLASS);
    }
}
