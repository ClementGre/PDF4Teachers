package fr.clementgre.pdf4teachers.document.editions.elements;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class GrabPoint extends Region{
    
    public static final int POINT_WIDTH = 6;
    public static final int POINT_OUTER = 3;
    
    public GrabPoint(){
        setBackground(new Background(new BackgroundFill(Color.color(0 / 255.0, 100 / 255.0, 255 / 255.0, .8), new CornerRadii(1), Insets.EMPTY)));
        setPrefWidth(POINT_WIDTH);
        setPrefHeight(POINT_WIDTH);
    }
    
}
