package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.texts.LBTextTab;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.utils.TextWrapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LBNoteTab extends Tab {

    public VBox pane = new VBox();
    public VBox optionPane = new VBox();

    public NoteTreeView treeView;

    public static InputStream fontFile = Element.getFontFile("Arial", false, false);
    public static HashMap<Integer, Map.Entry<Integer, Color>> fontTiers = new HashMap<>();

    public static BooleanProperty lockRatingScale = new SimpleBooleanProperty(false);

    public LBNoteTab(){
        setClosable(false);
        setContent(pane);
        setGraphic(Builders.buildImage(getClass().getResource("/img/Note.png")+"", 0, 25));
        Main.leftBar.getTabs().add(2, this);

        setup();
    }

    public void setup(){

        // DEBUG
        fontTiers.put(0, Map.entry(22, Color.RED));
        fontTiers.put(1, Map.entry(16, Color.RED));
        fontTiers.put(2, Map.entry(16, Color.BLUE));
        fontTiers.put(3, Map.entry(10, Color.BLUE));
        fontTiers.put(4, Map.entry(10, Color.GREEN));
        fontTiers.put(5, Map.entry(6, Color.GREEN));
        // -----

        treeView = new NoteTreeView(this);
        pane.getChildren().addAll(optionPane, treeView);

    }

    public NoteElement newNoteElementAuto(NoteTreeItem parent){

        PageRenderer page = Main.mainScreen.document.pages.get(0);
        if(Main.mainScreen.document.getCurrentPage() != -1) page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

        Main.mainScreen.setSelected(null);

        NoteElement current = new NoteElement(30, (int) (page.mouseY * Element.GRID_HEIGHT / page.getHeight()),
                "Nouvelle note", -1, 0, parent.getChildren().size(), NoteTreeView.getElementPath(parent), page);

        page.addElement(current, true);
        Main.mainScreen.setSelected(current);

        return current;
    }

    public NoteElement newNoteElement(String name, double value, double total, int index, String parentPath){

        PageRenderer page = Main.mainScreen.document.pages.get(0);
        if(Main.mainScreen.document.getCurrentPage() != -1) page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

        Main.mainScreen.setSelected(null);

        NoteElement current = new NoteElement(30, (int) (page.mouseY * Element.GRID_HEIGHT / page.getHeight()),
                name, value, total, index, parentPath, page);

        page.addElement(current, true);
        Main.mainScreen.setSelected(current);

        return current;
    }

    public static Font getTierFont(int index){
        return Font.loadFont(fontFile, fontTiers.get(index).getKey());
    }
    public static Color getTierColor(int index){
        return fontTiers.get(index).getValue();
    }

}
