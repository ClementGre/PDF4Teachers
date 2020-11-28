package fr.clementgre.pdf4teachers.panel.leftBar.paint;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

@SuppressWarnings("serial")
public class PaintTab extends Tab {

    public VBox pane = new VBox();
    public VBox optionPane = new VBox();

    // TREE VIEW
    public TreeView treeView = new TreeView<>();
    public TreeItem<String> treeViewRoot = new TreeItem<>();

    public PaintTab(){

        setClosable(false);
        setContent(pane);
        setGraphic(SVGPathIcons.generateImage(SVGPathIcons.DRAW_POLYGON, "#aaaaaa", 2, 0, 30, 0, SVGPathIcons.DRAW_POLYGON_RATIO));
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
