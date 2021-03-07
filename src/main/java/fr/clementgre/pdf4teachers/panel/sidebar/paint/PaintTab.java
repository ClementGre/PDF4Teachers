package fr.clementgre.pdf4teachers.panel.sidebar.paint;

import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToIntConverter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PaintTab extends SideTab {

    public VBox root;

    // actions buttons

    public Button newImage;
    public Button newVector;
    public Button delete;

    public Button vectorCreateCurve;
    public ToggleButton vectorStraightLineMode;
    public ToggleGroup vectorDrawMode;
    public ToggleButton vectorModeDraw;
    public ToggleButton vectorModePoint;

    // common settings

    public TextField path;
    public Button vectorUndoPath;
    public Button browsePath;

    // vector Settings

    public VBox vectorsOptionPane;

    public ToggleButton doFillButton;
    public SyncColorPicker vectorFillColor;
    public SyncColorPicker vectorStrokeColor;
    public Spinner<Integer> vectorStrokeWidth;

    // advanced Options

    public TitledPane advancedOptionsPane;

    public Label widthTitle;
    public Label heightTitle;

    public Spinner<Integer> spinnerX;
    public Spinner<Integer> spinnerY;
    public Spinner<Integer> spinnerWidth;
    public Spinner<Integer> spinnerHeight;

    public ComboBox<String> repeatMode;
    public ComboBox<String> resizeMode;
    public ComboBox<String> rotateMode;





    public PaintTab(){
        super("paint", SVGPathIcons.DRAW_POLYGON, 28, 30, null);
        MainWindow.paintTab = this;
    }

    @FXML
    public void initialize(){
        setContent(root);
        newImage.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.PICTURES, "darkgreen", 0, 22, 22, ImageUtils.defaultDarkColorAdjust));
        newVector.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.VECTOR_SQUARE, "darkblue", 0, 22, 22, ImageUtils.defaultDarkColorAdjust));
        delete.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.TRASH, "darkred", 0, 22, 22, ImageUtils.defaultDarkColorAdjust));

        vectorCreateCurve.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.CURVE, "black", 0, 22, 22, ImageUtils.defaultDarkColorAdjust));
        vectorStraightLineMode.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.RULES, "black", 0, 22, 22, ImageUtils.defaultDarkColorAdjust));
        vectorModeDraw.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.PAINT_BRUSH, "black", 0, 21, 21, ImageUtils.defaultDarkColorAdjust));
        vectorModePoint.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.PEN, "black", 0, 22, 22, ImageUtils.defaultDarkColorAdjust));

        vectorUndoPath.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.UNDO, "white", 0, 15, 15, ImageUtils.defaultWhiteColorAdjust));
        doFillButton.setGraphic(SVGPathIcons.generateImage(SVGPathIcons.FILL, "white", 0, 15, 15, ImageUtils.defaultWhiteColorAdjust));

        vectorStrokeWidth.getValueFactory().setConverter(new StringToIntConverter(0));

        spinnerX.getValueFactory().setConverter(new StringToIntConverter(0));
        spinnerY.getValueFactory().setConverter(new StringToIntConverter(0));
        spinnerWidth.getValueFactory().setConverter(new StringToIntConverter(0));
        spinnerHeight.getValueFactory().setConverter(new StringToIntConverter(0));

        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerX.getValueFactory()).setMax((int) Element.GRID_WIDTH);
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerY.getValueFactory()).setMax((int) Element.GRID_HEIGHT);
        //((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerWidth.getValueFactory()).maxProperty().bind();
        //((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerHeight.getValueFactory()).maxProperty().bind();

        repeatMode.setItems(FXCollections.observableArrayList(Arrays.stream(GraphicElement.RepeatMode.values())
                        .map((o) -> TR.tr(o.getKey())).collect(Collectors.toList())));
        repeatMode.getSelectionModel().select(0);

        resizeMode.setItems(FXCollections.observableArrayList(Arrays.stream(GraphicElement.ResizeMode.values())
                .map((o) -> TR.tr(o.getKey())).collect(Collectors.toList())));
        resizeMode.getSelectionModel().select(0);

        rotateMode.setItems(FXCollections.observableArrayList(Arrays.stream(GraphicElement.RotateMode.values())
                .map((o) -> TR.tr(o.getKey())).collect(Collectors.toList())));
        rotateMode.getSelectionModel().select(0);

        translate();

        setup();
    }

    public void translate(){
        advancedOptionsPane.setText(TR.tr("paint.title.advancedOptions"));
        widthTitle.setText(TR.tr("letter.width"));
        heightTitle.setText(TR.tr("letter.height"));
    }

    public void setup(){


    }
}
