package fr.clementgre.pdf4teachers.components;

import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class KeyableHBox extends HBox{
    public KeyableHBox(){
        super();
    }
    
    public KeyableHBox(double spacing){
        super(spacing);
    }
    
    public KeyableHBox(Node... children){
        super(children);
    }
    
    public KeyableHBox(double spacing, Node... children){
        super(spacing, children);
    }
}
