package fr.themsou.utils;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Builders {

    public static ImageView buildImage(String imgPatch, int width, int height) {
        ImageView imageView = new ImageView(new Image(imgPatch));

        if(width == 0 && height == 0) return imageView;

        if(width == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }

    public static void setPosition(Control element, double x, double y, double width, double height, boolean force){

        element.setLayoutX(x);
        element.setLayoutY(y);
        element.setPrefSize(width, height);

        if(force){
            element.setStyle("-fx-min-width: " + width + ";");
            element.setStyle("-fx-min-height: " + height + ";");
            element.minWidthProperty().bind(new SimpleDoubleProperty(width));
            element.minHeightProperty().bind(new SimpleDoubleProperty(height));
        }
    }


}
