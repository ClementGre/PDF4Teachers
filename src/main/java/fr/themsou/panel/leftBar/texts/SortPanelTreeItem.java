package fr.themsou.panel.leftBar.texts;

import fr.themsou.utils.style.StyleManager;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;

public class SortPanelTreeItem extends TreeItem {

    public GridPane pane = new GridPane();

    public SortPanelTreeItem(){

    }

    public void updateCell(TreeCell cell) {
        cell.setContextMenu(null);
        cell.setOnMouseClicked(null);
        cell.setStyle("-fx-padding: 0 0 0 -40; -fx-margin: 0; -fx-background-color: " + StyleManager.getHexAccentColor() + ";");
        cell.setGraphic(pane);
    }
}
