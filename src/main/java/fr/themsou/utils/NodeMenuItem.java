package fr.themsou.utils;

import javafx.scene.Node;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class NodeMenuItem extends CustomMenuItem {

    private boolean fat;

    public NodeMenuItem(HBox node, String text, int width, boolean fat){
        super(node);
        this.fat = fat;
        setup(text, width);
    }
    public NodeMenuItem(HBox node, String text, int width, boolean fat, boolean hideOnClick){
        super(node, hideOnClick);
        this.fat = fat;
        setup(text, width);
    }

    private void setup(String text, int width){

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if(width != -1) getNode().setPrefWidth(width);
        getNode().getChildren().addAll(new Region(), new Region(), new Region(), spacer, new Region());

        setName(text);
    }

    public void setLeftData(Node data){
        Pane pane = new Pane();
        pane.setStyle("-fx-font-size: 13; -fx-padding: 0 10 0 0;"); // top - right - bottom - left
        pane.getChildren().add(data);
        getNode().getChildren().set(0, pane);
    }
    public void setFalseLeftData(double width){
        Region spacer = new Region();
        spacer.setPrefWidth(width);
        getNode().getChildren().set(0, spacer);
    }
    public void setImage(ImageView image){
        getNode().getChildren().set(1, image);
    }
    public void setName(String text){
        Label textLabel = new Label(text);
        if(fat) textLabel.setStyle("-fx-font-size: 13; -fx-padding: 2 0 2 10;"); // top - right - bottom - left
        else textLabel.setStyle("-fx-font-size: 13;");
        getNode().getChildren().set(2, textLabel);
    }

    public void setAccelerator(String accelerator){

        Label acceleratorLabel = new Label(accelerator);
        if(fat) acceleratorLabel.setStyle("-fx-font-size: 13; -fx-padding: 2 10 2 0;"); // top - right - bottom - left
        else acceleratorLabel.setStyle("-fx-font-size: 13;");
        getNode().getChildren().set(4, acceleratorLabel);

        setAccelerator(KeyCombination.keyCombination(accelerator));
    }

    public void setToolTip(String toolTip){
        Tooltip toolTipUI = new Tooltip(new TextWrapper(toolTip, null, 350).wrap());
        toolTipUI.setShowDuration(Duration.INDEFINITE);
        Tooltip.install(getContent(), toolTipUI);
    }

    public HBox getNode(){
        return (HBox) getContent();
    }
}
