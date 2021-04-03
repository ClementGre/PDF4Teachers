package fr.clementgre.pdf4teachers.components;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HBoxSpacer extends Region{
    public HBoxSpacer(){
        HBox.setHgrow(this, Priority.ALWAYS);
        HBox.setMargin(this, new Insets(0, -10, 0, -10));
        setMaxWidth(Double.MAX_VALUE);
    }
}
