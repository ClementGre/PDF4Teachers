package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.panel.MainScreen;
import fr.themsou.panel.leftBar.notes.export.NoteExportWindow;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class LBNoteTab extends Tab {

    public VBox pane = new VBox();
    public HBox optionPane = new HBox();

    public NoteTreeView treeView;

    public static HashMap<Integer, Map.Entry<Font, Map.Entry<Color, Boolean>>> fontTiers = new HashMap<>();

    private Button settings = new Button();
    private ToggleButton lockRatingScale = new ToggleButton();
    private Button link = new Button();
    private Button export = new Button();

    public LBNoteTab(){
        setClosable(false);
        setContent(pane);
        setGraphic(Builders.buildImage(getClass().getResource("/img/Note.png")+"", 0, 25));
        Main.leftBar.getTabs().add(2, this);

        setup();
    }

    public void setup(){

        fontTiers.put(0, Map.entry(Font.loadFont(Element.getFontFile("Arial", false, false), 34), Map.entry(Color.RED, true)));
        fontTiers.put(1, Map.entry(Font.loadFont(Element.getFontFile("Arial", false, false), 22), Map.entry(Color.RED, false)));
        fontTiers.put(2, Map.entry(Font.loadFont(Element.getFontFile("Arial", false, false), 18), Map.entry(Color.RED, false)));
        fontTiers.put(3, Map.entry(Font.loadFont(Element.getFontFile("Arial", false, false), 18), Map.entry(Color.RED, false)));
        fontTiers.put(4, Map.entry(Font.loadFont(Element.getFontFile("Arial", false, false), 18), Map.entry(Color.RED, false)));
        fontTiers.put(5, Map.entry(Font.loadFont(Element.getFontFile("Arial", false, false), 18), Map.entry(Color.RED, false)));

        Builders.setHBoxPosition(settings, 45, 35, 0);
        settings.setCursor(Cursor.HAND);
        settings.setGraphic(Builders.buildImage(getClass().getResource("/img/NoteTab/engrenage.png")+"", 0, 0));
        settings.setOnAction((e) -> new NoteSettingsWindow());

        Builders.setHBoxPosition(lockRatingScale, 45, 35, 0);
        lockRatingScale.setCursor(Cursor.HAND);
        lockRatingScale.setGraphic(Builders.buildImage(getClass().getResource("/img/NoteTab/cadenas.png")+"", 0, 0));

        Builders.setHBoxPosition(link, 45, 35, 0);
        link.setCursor(Cursor.HAND);
        link.setGraphic(Builders.buildImage(getClass().getResource("/img/NoteTab/link.png")+"", 0, 0));
        link.disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        link.setOnAction((e) -> new NoteCopyRatingScaleDialog());

        Builders.setHBoxPosition(export, 45, 35, 0);
        export.setCursor(Cursor.HAND);
        export.setGraphic(Builders.buildImage(getClass().getResource("/img/NoteTab/exporter.png")+"", 0, 0));
        export.disableProperty().bind(Main.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        export.setOnAction((e) -> new NoteExportWindow());

        optionPane.setStyle("-fx-padding: 5 0;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        optionPane.getChildren().addAll(spacer, settings, lockRatingScale, link, export);

        treeView = new NoteTreeView(this);
        pane.getChildren().addAll(optionPane, treeView);

    }

    public NoteElement newNoteElementAuto(NoteTreeItem parent){

        PageRenderer page = Main.mainScreen.document.pages.get(0);
        if(Main.mainScreen.document.getCurrentPage() != -1) page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

        Main.mainScreen.setSelected(null);

        NoteElement current = new NoteElement(30, (int) (page.mouseY * Element.GRID_HEIGHT / page.getHeight()),
                TR.tr("Nouvelle note"), -1, 0, parent.getChildren().size(), NoteTreeView.getElementPath(parent), page.getPage(), page, "newNoteElementAuto");

        page.addElement(current, true);
        Main.mainScreen.setSelected(current);

        return current;
    }

    public NoteElement newNoteElement(String name, double value, double total, int index, String parentPath){

        PageRenderer page = Main.mainScreen.document.pages.get(0);
        if(Main.mainScreen.document.getCurrentPage() != -1) page = Main.mainScreen.document.pages.get(Main.mainScreen.document.getCurrentPage());

        Main.mainScreen.setSelected(null);

        NoteElement current = new NoteElement(30, (int) (page.mouseY * Element.GRID_HEIGHT / page.getHeight()),
                name, value, total, index, parentPath, page.getPage(), page, "newNoteElement");

        page.addElement(current, true);
        Main.mainScreen.setSelected(current);

        return current;
    }

    public void updateElementsFont(){
        if(treeView.getRoot() != null){
            NoteTreeItem root = ((NoteTreeItem) treeView.getRoot());
            if(root.hasSubNote()) updateElementFont(root);
            root.getCore().updateFont();
        }
    }
    private void updateElementFont(NoteTreeItem parent){

        for(int i = 0; i < parent.getChildren().size(); i++){
            NoteTreeItem children = (NoteTreeItem) parent.getChildren().get(i);

            children.getCore().updateFont();
            if(children.hasSubNote()) updateElementFont(children);
        }
    }

    public static Font getTierFont(int index){
        return fontTiers.get(index).getKey();
    }
    public static Color getTierColor(int index){
        return fontTiers.get(index).getValue().getKey();
    }
    public static boolean getTierShowName(int index){
        return fontTiers.get(index).getValue().getValue();
    }

    public BooleanProperty isLockRatingScaleProperty(){
        return lockRatingScale.selectedProperty();
    }
}
