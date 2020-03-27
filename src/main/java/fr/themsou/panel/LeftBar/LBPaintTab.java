package fr.themsou.panel.LeftBar;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TextWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

@SuppressWarnings("serial")
public class LBPaintTab extends Tab {

    public VBox pane = new VBox();
    public VBox optionPane = new VBox();

    // TREE VIEW
    public TreeView treeView = new TreeView<>();
    public TreeItem<String> treeViewRoot = new TreeItem<>();

    public LBPaintTab(){

        setClosable(false);
        setContent(pane);
        setGraphic(Builders.buildImage(getClass().getResource("/img/Paint.png")+"", 0, 25));
        Main.leftBar.getTabs().add(3, this);

        setup();

        pane.getChildren().addAll(optionPane, treeView);

    }

    public void setup(){

        String infoTxt = "Cette fonctionnalité est en cours de développement.";
        String infoTxt2 = "Elle vous permettra de mettre des images, des icones/emojis, des rectangles, des cercles etc.";
        Label info = new Label();
        VBox.setMargin(info, new Insets(5, 5, 5, 5));

        pane.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            info.setText(new TextWrapper(infoTxt, null, (int) pane.getWidth()-10).wrap() + "\n\n" + new TextWrapper(infoTxt2, null, (int) pane.getWidth()-10).wrap());
        });

        pane.getChildren().add(info);


    }
}
