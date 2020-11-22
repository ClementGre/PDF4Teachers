package fr.themsou.panel.leftBar.grades;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.panel.MainScreen.MainScreen;
import fr.themsou.panel.leftBar.grades.export.GradeExportWindow;
import fr.themsou.utils.*;
import fr.themsou.utils.image.ImageUtils;
import fr.themsou.utils.image.SVGPathIcons;
import fr.themsou.interfaces.windows.MainWindow;
import fr.themsou.interfaces.windows.language.TR;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
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
public class GradeTab extends Tab {

    public VBox pane = new VBox();
    public HBox optionPane = new HBox();

    public GradeTreeView treeView;

    public static HashMap<Integer, Map.Entry<Font, Map.Entry<Color, Boolean>>> fontTiers = new HashMap<>();

    public ToggleButton lockRatingPotitions = new ToggleButton();
    public ToggleButton lockGradeScale = new ToggleButton();
    private Button settings = new Button();
    private Button link = new Button();
    private Button export = new Button();

    public GradeTab(){
        setClosable(false);
        setContent(pane);
        setGraphic(SVGPathIcons.generateImage(SVGPathIcons.PERCENTAGE, "#aaaaaa", 2, 0, 30, 0, SVGPathIcons.PERCENTAGE_RATIO));
        MainWindow.leftBar.getTabs().add(2, this);

        setup();
    }

    public void setup(){

        fontTiers.put(0, Map.entry(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 28), Map.entry(Color.valueOf("#990000"), true)));
        fontTiers.put(1, Map.entry(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 24), Map.entry(Color.valueOf("#b31a1a"), false)));
        fontTiers.put(2, Map.entry(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 18), Map.entry(Color.valueOf("#cc3333"), false)));
        fontTiers.put(3, Map.entry(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 18), Map.entry(Color.valueOf("#e64d4d"), false)));
        fontTiers.put(4, Map.entry(Font.loadFont(FontUtils.getFontFile("Open Sans", false, false), 18), Map.entry(Color.valueOf("#ff6666"), false)));

        lockRatingPotitions.setSelected(false);

        PaneUtils.setHBoxPosition(lockGradeScale, 45, 35, 0);
        lockGradeScale.setCursor(Cursor.HAND);
        lockGradeScale.setSelected(false);
        lockGradeScale.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/cadenas.png") + "", 0, 0));
        lockGradeScale.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if(newValue) lockGradeScale.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/cadenas-ferme.png") + "", 0, 0));
            else lockGradeScale.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/cadenas.png") + "", 0, 0));

            // Update the selected cell
            if(treeView.getSelectionModel().getSelectedItem() != null){
                int selected = treeView.getSelectionModel().getSelectedIndex();
                treeView.getSelectionModel().select(null);
                treeView.getSelectionModel().select(selected);
            }
        });
        lockGradeScale.setTooltip(PaneUtils.genToolTip(TR.tr("Vérouiller le barème, il ne pourra plus être modifié.")));

        PaneUtils.setHBoxPosition(settings, 45, 35, 0);
        settings.setCursor(Cursor.HAND);
        settings.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/engrenage.png")+"", 0, 0));
        settings.setOnAction((e) -> new GradeSettingsWindow());
        settings.setTooltip(PaneUtils.genToolTip(TR.tr("Modifier les polices, couleurs et préfixe de chaque niveau de grades.")));

        PaneUtils.setHBoxPosition(link, 45, 35, 0);
        link.setCursor(Cursor.HAND);
        link.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/link.png")+"", 0, 0));
        link.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        link.setOnAction((e) -> new GradeCopyGradeScaleDialog());
        link.setTooltip(PaneUtils.genToolTip(TR.tr("Envoyer le barème sur d'autres éditions.")));

        PaneUtils.setHBoxPosition(export, 45, 35, 0);
        export.setCursor(Cursor.HAND);
        export.setGraphic(ImageUtils.buildImage(getClass().getResource("/img/GradesTab/exporter.png")+"", 0, 0));
        export.disableProperty().bind(MainWindow.mainScreen.statusProperty().isNotEqualTo(MainScreen.Status.OPEN));
        export.setOnAction((e) -> new GradeExportWindow());
        export.setTooltip(PaneUtils.genToolTip(TR.tr("Exporter les notes d'une ou plusieurs copies, dans un ou plusieurs fichier CSV. Ceci permet ensuite d'importer les notes dans un logiciel tableur")));

        optionPane.setStyle("-fx-padding: 5 0 5 0;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        optionPane.getChildren().addAll(spacer, lockGradeScale, settings, link, export);

        treeView = new GradeTreeView(this);
        pane.getChildren().addAll(optionPane, treeView);

    }

    public GradeElement newGradeElementAuto(GradeTreeItem parent){

        PageRenderer page = MainWindow.mainScreen.document.pages.get(0);
        if(MainWindow.mainScreen.document.getCurrentPage() != -1) page = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

        MainWindow.mainScreen.setSelected(null);

        GradeElement current = new GradeElement((int) (60 * Element.GRID_WIDTH / page.getWidth()), (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight()), page.getPage(),
                true, -1, 0, parent.getChildren().size(), GradeTreeView.getElementPath(parent), TR.tr("Nouvelle note"));

        page.addElement(current, true);
        MainWindow.mainScreen.setSelected(current);

        return current;
    }

    public GradeElement newGradeElement(String name, double value, double total, int index, String parentPath, boolean update){

        PageRenderer page = MainWindow.mainScreen.document.pages.get(0);
        if(MainWindow.mainScreen.document.getCurrentPage() != -1) page = MainWindow.mainScreen.document.pages.get(MainWindow.mainScreen.document.getCurrentPage());

        if(update) MainWindow.mainScreen.setSelected(null);

        GradeElement current = new GradeElement((int) (60 * Element.GRID_WIDTH / page.getWidth()), (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight()), page.getPage(),
                true, value, total, index, parentPath, name);

        page.addElement(current, update);
        if(update) MainWindow.mainScreen.setSelected(current);

        return current;
    }

    public void updateElementsFont(){
        if(treeView.getRoot() != null){
            GradeTreeItem root = ((GradeTreeItem) treeView.getRoot());
            if(root.hasSubGrade()) updateElementFont(root);
            root.getCore().updateFont();
        }
    }
    private void updateElementFont(GradeTreeItem parent){

        for(int i = 0; i < parent.getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) parent.getChildren().get(i);

            children.getCore().updateFont();
            if(children.hasSubGrade()) updateElementFont(children);
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

    public BooleanProperty isLockGradeScaleProperty(){
        return lockGradeScale.selectedProperty();
    }
}
