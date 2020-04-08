package fr.themsou.panel.LeftBar;

import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.utils.TextWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

@SuppressWarnings("serial")
public class LBNoteTab extends Tab {

    public VBox pane = new VBox();
    public VBox optionPane = new VBox();

    // TREE VIEW
    public TreeView treeView = new TreeView<>();
    public TreeItem<String> treeViewRoot = new TreeItem<>();

    public LBNoteTab(){
        setClosable(false);
        setContent(pane);
        setGraphic(Builders.buildImage(getClass().getResource("/img/Note.png")+"", 0, 25));
        Main.leftBar.getTabs().add(2, this);

        setup();

        pane.getChildren().addAll(optionPane, treeView);

    }

    public void setup(){

        String infoTxt = TR.tr("Cette fonctionnalité est en cours de développement.");
        String infoTxt2 = TR.tr("Elle vous permettra de mettre des notes à vos élèves, de faire la somme automatiquement et d'exporter le tout sous format CSV.");
        Label info = new Label();
        VBox.setMargin(info, new Insets(5, 5, 5, 5));

        pane.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            info.setText(new TextWrapper(infoTxt, null, (int) pane.getWidth()-10).wrap() + "\n\n" + new TextWrapper(infoTxt2, null, (int) pane.getWidth()-10).wrap());
        });

        pane.getChildren().add(info);


    }

}
