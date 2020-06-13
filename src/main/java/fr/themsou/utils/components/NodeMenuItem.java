package fr.themsou.utils.components;

import fr.themsou.utils.Builders;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class NodeMenuItem extends CustomMenuItem {

    private boolean fat;
    private HBox node;

    // Default text Height is 16
    // text top/bottom margin = 2*5 = 10
    // total height is 26

    // Images is 20*20
    // 26 - 20 = 6 and 6/2 = 3
    // So images has 3px top/bottom margin

    /////////// FAT MODE ////////////////
    // Default text Height is 16
    // text top/bottom margin = 2*9 = 18
    // total height is 34

    // Images is 20*20
    // 34 - 20 = 14 and 14/2 = 7
    // So images has 7px top/bottom margin
    public NodeMenuItem(HBox node, String text, boolean fat){
        super(new Pane());
        this.fat = fat;
        this.node = node;
        ((Pane) getContent()).getChildren().add(node);
        setup(text);
    }
    public NodeMenuItem(HBox node, String text, boolean fat, boolean hideOnClick){
        super(new Pane(), hideOnClick);
        this.fat = fat;
        this.node = node;
        ((Pane) getContent()).getChildren().add(node);
        setup(text);
    }

    private void setup(String text){
        
        getContent().setStyle("-fx-padding: 0 -24 -14 0"); // 12 left/right margin and 7 top/bottom margin

        node.setTranslateX(-12); node.setTranslateY(-7);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getNode().getChildren().addAll(new Region(), new Region(), new Region(), spacer, new Region());

        setName(text);
    }

    public void setLeftData(Node data){
        Pane pane = new Pane();
        if(fat){
            pane.setStyle("-fx-font-size: 13; -fx-padding: 7 0 7 10;"); // top - right - bottom - left
            data.setTranslateY(7);
        }else{
            pane.setStyle("-fx-font-size: 13; -fx-padding: 3 0 3 10;"); // top - right - bottom - left
            data.setTranslateY(3);
        } data.setTranslateX(10);

        pane.getChildren().add(data);
        getNode().getChildren().set(0, pane);
    }
    public void setFalseLeftData(){
        Region spacer = new Region();
        spacer.setPrefWidth(30);
        getNode().getChildren().set(0, spacer);
    }
    public void setImage(ImageView image){
        Pane pane = new Pane();
        if(fat){
            pane.setStyle("-fx-padding: 7 0 7 10;"); // top - right - bottom - left
            image.setTranslateY(7);
        }else{ pane.setStyle("-fx-padding: 3 0 3 10;"); // top - right - bottom - left
            image.setTranslateY(3);
        } image.setTranslateX(10);

        pane.getChildren().add(image);
        getNode().getChildren().set(1, pane);
    }
    public void setName(String text){
        Label textLabel = new Label(text);
        if(fat) textLabel.setStyle("-fx-font-size: 13; -fx-padding: 9 10 9 10;"); // top - right - bottom - left
        else textLabel.setStyle("-fx-font-size: 13; -fx-padding: 5 10 5 10;"); // top - right - bottom - left
        getNode().getChildren().set(2, textLabel);
    }

    public void setKeyCombinaison(KeyCombination keyCombinaison){

        Label acceleratorLabel = new Label(keyCombinaison.getDisplayText());
        if(fat) acceleratorLabel.setStyle("-fx-font-size: 13; -fx-padding: 9 10 9 10;"); // top - right - bottom - left
        else acceleratorLabel.setStyle("-fx-font-size: 13; -fx-padding: 5 10 5 10;");  // top - right - bottom - left
        getNode().getChildren().set(4, acceleratorLabel);

        setAccelerator(keyCombinaison);
    }

    public void setToolTip(String toolTip){
        Tooltip toolTipUI = Builders.genToolTip(toolTip);
        toolTipUI.setShowDuration(Duration.INDEFINITE);
        Tooltip.install(node, toolTipUI);
    }

    // GETTERS / SETTERS

    public HBox getNode(){
        return node;
    }

    // STATIC UTILS

    public static void setupMenu(Menu menu){

        AtomicBoolean firstRun = new AtomicBoolean(true);
        menu.setOnShown(e -> {
            if(firstRun.get()){
                double maxWidth = 0;
                int extra = 0;
                for(MenuItem item : menu.getItems()){
                    if(item instanceof NodeMenuItem){
                        if(((NodeMenuItem) item).getNode().getWidth() > maxWidth) maxWidth = ((NodeMenuItem) item).getNode().getWidth();
                    }else if(item instanceof Menu){
                        extra = 28; // Menus has a little Arrow, this add's 25px
                    }
                }
                for(MenuItem item : menu.getItems()){
                    if(item instanceof NodeMenuItem){
                        ((NodeMenuItem) item).getNode().setPrefWidth(maxWidth+extra);
                    }
                }
                firstRun.set(false);
            }
        });
    }
    public static void setupMenu(ContextMenu menu){

        menu.setOnShown(e -> {
            double maxWidth = 0;
            int extra = 0;
            for(MenuItem item : menu.getItems()){
                if(item instanceof NodeMenuItem){
                    if(((NodeMenuItem) item).getNode().getWidth() > maxWidth) maxWidth = ((NodeMenuItem) item).getNode().getWidth();
                }
            }
            for(MenuItem item : menu.getItems()){
                if(item instanceof NodeMenuItem){
                    ((NodeMenuItem) item).getNode().setPrefWidth(maxWidth+extra);
                }
            }
        });
    }
}
