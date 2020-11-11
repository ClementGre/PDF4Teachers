package fr.themsou.document.editions.elements;

import fr.themsou.utils.TR;
import fr.themsou.utils.components.NodeMenuItem;
import fr.themsou.windows.MainWindow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;

import java.util.LinkedHashMap;

public abstract class GraphicElement extends Element{

    public GraphicElement(int x, int y, int pageNumber, boolean hasPage, int width, int height){
        super(x, y, pageNumber);

        setWidth(width);
        setHeight(height);
    }

    // SETUP / EVENT CALL BACK

    @Override
    protected void setupBindings(){
        /*this.text.fontProperty().addListener((observable, oldValue, newValue) -> {
            updateLaTeX();
        });*/
    }
    @Override
    protected void onMouseRelease(){

    }
    @Override
    protected void setupMenu(){

        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Supprimer"), false);
        item1.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        item1.setToolTip(TR.tr("Supprime cet élément. Il sera donc retiré de l'édition."));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Dupliquer"), false);
        item2.setToolTip(TR.tr("Crée un second élément identique à celui-ci."));
        menu.getItems().addAll(item1, item2);
        NodeMenuItem.setupMenu(menu);

        item1.setOnAction(e -> delete());
        item2.setOnAction(e -> cloneOnDocument());
    }

    // ACTIONS

    @Override
    public void select(){
        super.selectPartial();
        MainWindow.leftBar.getSelectionModel().select(3);

    }
    @Override
    public void doubleClick() {

    }

    // READERS AND WRITERS

    protected LinkedHashMap<Object, Object> getYAMLPartialData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("width", (int) getWidth());
        data.put("height", (int) getHeight());

        return data;
    }

}
