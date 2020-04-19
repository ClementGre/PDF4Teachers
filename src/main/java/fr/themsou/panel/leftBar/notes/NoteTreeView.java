package fr.themsou.panel.leftBar.notes;

import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen;
import javafx.geometry.Insets;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class NoteTreeView extends TreeView<String> {

    public NoteTreeItem total = new NoteTreeItem();

    public NoteTreeView(LBNoteTab noteTab){
        setRoot(total);
        total.setExpanded(true);
        disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        setEditable(true);
        setBackground(new Background(new BackgroundFill(Color.rgb(244, 244, 244), CornerRadii.EMPTY, Insets.EMPTY)));
        prefHeightProperty().bind(noteTab.pane.heightProperty().subtract(layoutYProperty()));
        prefWidthProperty().bind(noteTab.pane.widthProperty());

        setCellFactory(new Callback<>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                return new TreeCell<>() {
                    @Override protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);

                        // Null
                        if(empty){
                            setGraphic(null);
                            setStyle(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                            return;
                        }
                        // String Data
                        if(item != null){
                            setGraphic(null);
                            setStyle(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                            return;
                        }
                        // TreeNoteData
                        if(getTreeItem() instanceof NoteTreeItem){
                            ((NoteTreeItem) getTreeItem()).updateCell(this);
                            return;
                        }

                        // Other
                        setStyle(null);
                        setGraphic(null);
                        setContextMenu(null);
                        setOnMouseClicked(null);

                    }
                };
            }
        });
    }

}
