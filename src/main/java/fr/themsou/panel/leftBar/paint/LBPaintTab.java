package fr.themsou.panel.leftBar.paint;

import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.utils.TextWrapper;
import fr.themsou.windows.MainWindow;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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
        setGraphic(Builders.buildImage(getClass().getResource("/img/paint.png")+"", 0, 25));
        MainWindow.leftBar.getTabs().add(3, this);

        setup();

        pane.getChildren().addAll(optionPane, treeView);

    }

    public void setup(){

        String infoTxt = TR.tr("Cette fonctionnalité est en cours de développement.");
        String infoTxt2 = TR.tr("Elle vous permettra de mettre des images, des icones/emojis, des rectangles, des cercles etc.");
        Label info = new Label();
        VBox.setMargin(info, new Insets(5, 5, 5, 5));

        pane.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            info.setText(new TextWrapper(infoTxt, null, (int) pane.getWidth()-10).wrap() + "\n\n" + new TextWrapper(infoTxt2, null, (int) pane.getWidth()-10).wrap());
        });

        pane.getChildren().add(info);


    }
}
