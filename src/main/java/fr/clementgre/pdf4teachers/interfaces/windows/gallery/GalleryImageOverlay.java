package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class GalleryImageOverlay extends StackPane{
    
    private final Rectangle overlayClip = new Rectangle();
    private final VBox overlay = new VBox();
    private final Label name = new Label();
    
    private final DropShadow shadow = new DropShadow();
    private boolean hover = false;
    
    private final Timeline timeline = new Timeline(60);
    
    public GalleryImageOverlay(Node content){
        super();
        getChildren().addAll(content, overlay);
    
        shadow.setColor(Color.web("#0078d7"));
        shadow.setSpread(.90);
        shadow.setOffsetY(0);
        shadow.setOffsetX(0);
        shadow.setRadius(0);
        setEffect(shadow);
        
        overlayClip.widthProperty().bind(widthProperty().subtract(2 * GalleryWindow.ImageGridCell.PADDING));
        overlay.setTranslateX(GalleryWindow.ImageGridCell.PADDING);
        overlay.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, .7), CornerRadii.EMPTY, Insets.EMPTY)));
        overlay.getChildren().add(name);
        overlay.setPadding(new Insets(5));
        overlay.setClip(overlayClip);
        
        name.setStyle("-fx-text-fill: white;");
    
        
        heightProperty().addListener((observable, oldValue, newValue) -> {
            updateOverlayDimensions();
        });
        setOnMouseEntered((e) -> {
            shadow.setRadius(2);
            hover = true;
            updateOverlayDimensions();
        });
        setOnMouseExited((e) -> {
            shadow.setRadius(0);
            hover = false;
            updateOverlayDimensions();
        });
    }
    
    private void updateOverlayDimensions(){
        timeline.stop();
        timeline.getKeyFrames().clear();
        if(hover){
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(overlay.translateYProperty(), getHeight() - 50)),
                    new KeyFrame(Duration.millis(200), new KeyValue(overlayClip.heightProperty(), 50))
            );
        }else{
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(overlay.translateYProperty(), getHeight() - 20)),
                    new KeyFrame(Duration.millis(200), new KeyValue(overlayClip.heightProperty(), 20))
            );
        }
        timeline.play();
    }
    
    public void setText(String text){
        name.setText(text);
    }
}
