package fr.clementgre.pdf4teachers.components;

import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class VBoxSpacer extends Region{
    public VBoxSpacer(){
        VBox.setVgrow(this, Priority.ALWAYS);
        setMaxHeight(Double.MAX_VALUE);
    }
}
