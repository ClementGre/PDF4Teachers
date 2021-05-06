package fr.clementgre.pdf4teachers.components;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HBoxSpacer extends Region{
    public HBoxSpacer(){
        HBox.setHgrow(this, Priority.ALWAYS);
        setMaxWidth(Double.MAX_VALUE);
        setMinWidth(0);
    }
}
