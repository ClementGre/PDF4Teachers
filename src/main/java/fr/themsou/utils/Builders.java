package fr.themsou.utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.control.*;
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

    public static void setMenuSize(Menu menu){

        for(MenuItem subMenu : menu.getItems()){
            subMenu.setStyle("-fx-font-size: 13;");
            if(subMenu instanceof Menu){
                setMenuSize((Menu) subMenu);
            }
        }
    }
    public static void setMenuSize(ContextMenu menu){

        for(MenuItem subMenu : menu.getItems()){
            subMenu.setStyle("-fx-font-size: 13;");
            if(subMenu instanceof Menu){
                setMenuSize((Menu) subMenu);
            }
        }
    }

    public static void secureAlert(Dialog alert){
        alert.setOnShowing(new EventHandler<DialogEvent>() {
            @Override public void handle(DialogEvent e) {
                new Thread(new Runnable() {
                    @Override  public void run() {

                        try{
                            Thread.sleep(400);
                        }catch(InterruptedException ex){ ex.printStackTrace();  }

                        Platform.runLater(new Runnable(){
                            @Override public void run(){
                                if(alert.isShowing()){
                                    if(alert.getDialogPane().getScene().getWindow().getWidth() < 100){
                                        alert.getDialogPane().getScene().getWindow().setWidth(500);
                                        alert.getDialogPane().getScene().getWindow().setHeight(200);
                                    }
                                }
                            }
                        });

                    }
                }, "AlertResizer").start();
            }
        });
    }


}
