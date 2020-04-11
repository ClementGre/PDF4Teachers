package fr.themsou.utils;

import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class AnimatedZoomOperator {

    private Timeline timeline;
    private boolean isPlaying = false;

    private Pane pane;

    public ScrollBar vScrollBar = new ScrollBar();
    public ScrollBar hScrollBar = new ScrollBar();

    public AnimatedZoomOperator(Pane pane, MainScreen mainScreen){

        this.pane = pane;

        this.timeline = new Timeline(60);
        timeline.setOnFinished((ActionEvent e) -> {
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                }catch(InterruptedException ex){ ex.printStackTrace(); }
                isPlaying = false;
            }).start();
        });

        vScrollBar.setOrientation(Orientation.VERTICAL);
        hScrollBar.setOrientation(Orientation.HORIZONTAL);

        vScrollBar.layoutXProperty().bind(mainScreen.widthProperty().subtract(vScrollBar.widthProperty()));
        hScrollBar.layoutYProperty().bind(mainScreen.heightProperty().subtract(hScrollBar.heightProperty()));

        vScrollBar.prefHeightProperty().bind(mainScreen.heightProperty());
        hScrollBar.prefWidthProperty().bind(mainScreen.widthProperty());

        vScrollBar.setMax(1);
        hScrollBar.setMax(1);

        vScrollBar.setVisible(false);
        hScrollBar.setVisible(false);

        mainScreen.getChildren().addAll(hScrollBar, vScrollBar);

    }

    public void zoom(double factor, double x, double y) {
        // determine scale
        double oldScale = pane.getScaleX();
        double scale = oldScale * factor;
        double f = (scale / oldScale) - 1;

        // determine offset that we will have to move the node
        Bounds bounds = pane.localToScene(pane.getBoundsInLocal());
        double dx = (x - (bounds.getWidth() / 2 + bounds.getMinX()));
        double dy = (y - (bounds.getHeight() / 2 + bounds.getMinY()));


        // timeline that scales and moves the node
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(isPlaying ? 100 : 200), new KeyValue(pane.translateXProperty(), pane.getTranslateX() - f * dx)),
                new KeyFrame(Duration.millis(isPlaying ? 100 : 200), new KeyValue(pane.translateYProperty(), pane.getTranslateY() - f * dy)),
                new KeyFrame(Duration.millis(isPlaying ? 100 : 200), new KeyValue(pane.scaleXProperty(), scale)),
                new KeyFrame(Duration.millis(isPlaying ? 100 : 200), new KeyValue(pane.scaleYProperty(), scale))
        );
        timeline.stop();
        isPlaying = true;
        timeline.play();
    }

    public void scrollDown(int factor){

        double newTranslateY = pane.getTranslateY() - factor;
        if(newTranslateY < -(pane.getHeight()*pane.getScaleX()-Main.mainScreen.getHeight())) newTranslateY = -(pane.getHeight()*pane.getScaleX()-Main.mainScreen.getHeight());

        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(isPlaying ? 70 : 200), new KeyValue(pane.translateYProperty(), newTranslateY))
        );
        timeline.stop();
        isPlaying = true;
        timeline.play();
    }

    public void scrollUp(int factor){

        double newTranslateY = pane.getTranslateY() + factor;
        if(newTranslateY > 0) newTranslateY = 0;

        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(isPlaying ? 70 : 200), new KeyValue(pane.translateYProperty(), newTranslateY))
        );
        timeline.stop();
        isPlaying = true;
        timeline.play();
    }

    public void setupDocument() {

        vScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {

            double paneHeight = pane.getHeight()*pane.getScaleX()- Main.mainScreen.getHeight();
            double translateY = -newValue.doubleValue() * paneHeight;

            if(translateY != pane.getTranslateY()){
                pane.setTranslateY(translateY);
            }
        });
        pane.translateYProperty().addListener((observable, oldValue, newValue) -> {
            double paneHeight = pane.getHeight()*pane.getScaleX()-Main.mainScreen.getHeight();

            if(paneHeight <= 0){
                vScrollBar.setVisible(false);
                centerPagesY();
            }else{
                vScrollBar.setVisible(true);
                double vValue = -newValue.doubleValue() / paneHeight;
                if(vValue != vScrollBar.getValue()){
                    vScrollBar.setValue(vValue);
                }
            }
        });

        hScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
            double paneWidth = pane.getWidth()*pane.getScaleX()-Main.mainScreen.getWidth();

            double translateX = -newValue.doubleValue() * paneWidth;
            if(translateX != pane.getTranslateX()){
                pane.setTranslateX(translateX);
            }
        });
        pane.translateYProperty().addListener((observable, oldValue, newValue) -> {
            double paneWidth = pane.getWidth()*pane.getScaleX()-Main.mainScreen.getWidth();

            if(paneWidth <= 0){
                hScrollBar.setVisible(false);
                centerPagesX();
            }else{
                hScrollBar.setVisible(true);
                double hValue = -newValue.doubleValue() / pane.getWidth();
                if(hValue != hScrollBar.getValue()){
                    hScrollBar.setValue(hValue);
                }
            }
        });
    }


    public void centerPagesY(){
        pane.setTranslateY((Main.mainScreen.getHeight()-pane.getHeight())/2);
    }
    public void centerPagesX(){
        pane.setTranslateX((Main.mainScreen.getWidth()-pane.getWidth())/2);
    }
}