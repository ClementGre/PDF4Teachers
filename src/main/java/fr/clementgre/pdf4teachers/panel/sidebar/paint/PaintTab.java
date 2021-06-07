package fr.clementgre.pdf4teachers.panel.sidebar.paint;

import fr.clementgre.pdf4teachers.components.NoArrowMenuButton;
import fr.clementgre.pdf4teachers.components.ScaledComboBox;
import fr.clementgre.pdf4teachers.components.SyncColorPicker;
import fr.clementgre.pdf4teachers.utils.dialogs.FIlesChooserManager;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.*;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.gallery.GalleryWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageListPane;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorListPane;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.*;
import fr.clementgre.pdf4teachers.utils.exceptions.PathParseException;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;
import fr.clementgre.pdf4teachers.utils.panes.PressAndHoldManager;
import fr.clementgre.pdf4teachers.utils.svg.SVGFileParser;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.svg.SVGUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToIntConverter;
import javafx.animation.PauseTransition;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PaintTab extends SideTab{

    public VBox root;

    // actions buttons
    public HBox commonActionButtons;
    
    public MenuButton newImage;
    public NoArrowMenuButton newVector;
    public Button delete;
    
    public HBox vectorsActonButtons;
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
    public VBox advancedOptionsContent;

    
    public Spinner<Integer> spinnerX;
    public Spinner<Integer> spinnerY;
    public Label widthTitle;
    public Label heightTitle;
    public Spinner<Integer> spinnerWidth;
    public Spinner<Integer> spinnerHeight;
    
    public Label repeatModeLabel;
    public ScaledComboBox<String> repeatMode;
    public Label resizeModeLabel;
    public ScaledComboBox<String> resizeMode;
    
    public HBox vectorsAdvancedOptions;
    public Label arrowLengthTitle;
    public Spinner<Integer> spinnerArrowLength;
    
    // Lists
    
    public VectorListPane favouriteVectors;
    public ImageListPane favouriteImages;
    public VectorListPane lastVectors;
    public ImageListPane gallery;
    
    // WINDOWS
    
    public GalleryWindow galleryWindow = null;
    
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

        PaneUtils.setPosition(spinnerX, 0, 0, -1, 26, true);
        PaneUtils.setPosition(spinnerY, 0, 0, -1, 26, true);
        PaneUtils.setPosition(spinnerWidth, 0, 0, -1, 26, true);
        PaneUtils.setPosition(spinnerHeight, 0, 0, -1, 26, true);

        spinnerX.getValueFactory().setConverter(new StringToIntConverter(0));
        spinnerY.getValueFactory().setConverter(new StringToIntConverter(0));
        spinnerWidth.getValueFactory().setConverter(new StringToIntConverter(0));
        spinnerHeight.getValueFactory().setConverter(new StringToIntConverter(0));

        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerX.getValueFactory()).setMax((int) Element.GRID_WIDTH);
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerY.getValueFactory()).setMax(Integer.MAX_VALUE);
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerY.getValueFactory()).setMin(Integer.MIN_VALUE);
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerWidth.getValueFactory()).setMax((int) Element.GRID_WIDTH);
        ((SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerHeight.getValueFactory()).setMax((int) Element.GRID_HEIGHT);

        PaneUtils.setHBoxPosition(path, 0, 30, 0);
        PaneUtils.setHBoxPosition(vectorUndoPath, 30, 30, 0);
        PaneUtils.setHBoxPosition(browsePath, 0, 30, 0);
        
        repeatMode.setItems(FXCollections.observableArrayList(Arrays.stream(GraphicElement.RepeatMode.values())
                .map((o) -> TR.tr(o.getKey())).collect(Collectors.toList())));
        repeatMode.getSelectionModel().select(0);
    
        resizeMode.setItems(FXCollections.observableArrayList(Arrays.stream(GraphicElement.ResizeMode.values())
                .map((o) -> TR.tr(o.getKey())).collect(Collectors.toList())));
        resizeMode.getSelectionModel().select(0);
    
        PaneUtils.setPosition(spinnerArrowLength, 0, 0, 75, 26, true);
        
        translate();
        setup();
    }
    
    public void translate(){
        // Advanced options
        advancedOptionsPane.setText(TR.tr("paintTab.advancedOptions"));
        repeatModeLabel.setText(TR.tr("paintTab.advancedOptions.repeatMode"));
        resizeModeLabel.setText(TR.tr("paintTab.advancedOptions.resizeMode"));
        widthTitle.setText(TR.tr("letter.width"));
        heightTitle.setText(TR.tr("letter.height"));
        arrowLengthTitle.setText(TR.tr("paintTab.advancedOptions.arrowLength"));
    }

    public void setup(){
        
        // Advances options listeners / updaters
        spinnerX.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.mainScreen.getSelected() instanceof GraphicElement element){
                if(element.getRealX() != newValue) element.setRealX(newValue);
            }
        });
        spinnerY.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.mainScreen.getSelected() instanceof GraphicElement element){
                if(element.getRealY() != newValue) element.setRealY(newValue);
            }
        });
        spinnerWidth.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.mainScreen.getSelected() instanceof GraphicElement element){
                if(element.getRealWidth() != newValue) element.setRealWidth(newValue);
            }
        });
        spinnerHeight.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.mainScreen.getSelected() instanceof GraphicElement element){
                if(element.getRealHeight() != newValue) element.setRealHeight(newValue);
            }
        });
        repeatMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.mainScreen.getSelected() instanceof GraphicElement element){
                for(GraphicElement.RepeatMode mode : GraphicElement.RepeatMode.values()){
                    if(TR.tr(mode.getKey()).equals(newValue)){
                        element.setRepeatMode(mode);
                        break;
                    }
                }
            }
        });
        resizeMode.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.mainScreen.getSelected() instanceof GraphicElement element){
                for(GraphicElement.ResizeMode mode : GraphicElement.ResizeMode.values()){
                    if(TR.tr(mode.getKey()).equals(newValue)){
                        element.setResizeMode(mode);
                        break;
                    }
                }
            }
        });
        spinnerArrowLength.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(MainWindow.mainScreen.getSelected() instanceof VectorElement element){
                if(element.getArrowLength() != newValue) element.setArrowLength(newValue);
            }
        });
    
        delete.setOnAction(e -> deleteSelected());
    
        vectorFillColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            doFillButton.setSelected(true);
            MainWindow.userData.vectorsLastFill = newValue;
        });
        doFillButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            MainWindow.userData.vectorsLastDoFIll = newValue;
        });
        vectorStrokeColor.valueProperty().addListener((observable, oldValue, newValue) -> {
            MainWindow.userData.vectorsLastStroke = newValue;
        });
        vectorStrokeWidth.valueProperty().addListener((observable, oldValue, newValue) -> {
            MainWindow.userData.vectorsLastStrokeWidth = newValue;
        });
    
        path.setContextMenu(null);
        
        ////////// New Image menu //////////
        
        
        NodeMenuItem browseImage = new NodeMenuItem(TR.tr("file.browse"), true);
        NodeMenuItem newImageEmpty = new NodeMenuItem(TR.tr("actions.new.image"), true);
        NodeMenuItem openGallery = new NodeMenuItem(TR.tr("paintTab.gallery.openGallery"), true);
        newImage.getItems().addAll(browseImage, newImageEmpty, openGallery);
        //NodeMenuItem.setupMenu(newImage);
            
        browseImage.setOnAction(ae -> {
            browseImagePath(path -> {
                PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();

                ImageElement element = new ImageElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(), true,
                        0, 0, GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS, path);

                page.addElement(element, true);
                element.centerOnCoordinatesY();
                MainWindow.mainScreen.setSelected(element);
            });
        });
        newImageEmpty.setOnAction(ae -> {
            PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();

            ImageElement element = new ImageElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(), true,
                    page.toGridX(100), page.toGridY(100), GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS, "");

            page.addElement(element, true);
            element.centerOnCoordinatesY();
            MainWindow.mainScreen.setSelected(element);
        });
        openGallery.setOnAction(ae -> {
            openGallery();
        });
    
        ////////// New Vector menu //////////
        
        NodeMenuItem newVectorDrawing = new NodeMenuItem(TR.tr("paintTab.vectorElements.newDrawing"), true);
        NodeMenuItem newVectorEmpty = new NodeMenuItem(TR.tr("paintTab.vectorElements.newEmpty"), true);
        NodeMenuItem browseVector = new NodeMenuItem(TR.tr("paintTab.vectorElements.browseSVG"), true);
        newVector.getItems().addAll(newVectorDrawing, newVectorEmpty, browseVector);
        //NodeMenuItem.setupMenu(menu);
    
        newVectorDrawing.setOnAction(ae -> {
            PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
    
            VectorElement element = new VectorElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(), true,
                    page.toGridX(100), page.toGridY(100), GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS,
                    false, MainWindow.userData.vectorsLastFill, MainWindow.userData.vectorsLastStroke, (int) MainWindow.userData.vectorsLastStrokeWidth == 0 ? 4 : (int) MainWindow.userData.vectorsLastStrokeWidth,
                    "", false, false, 0);
    
            
            page.addElement(element, true);
            element.centerOnCoordinatesY();
            element.enterEditMode();
            MainWindow.mainScreen.setSelected(element);
            element.setLinkedVectorData(VectorListPane.addLastVector(element));
        });
        newVectorEmpty.setOnAction(ae -> {
            PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
    
            VectorElement element = new VectorElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(), true,
                    page.toGridX(100), page.toGridY(100), GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS,
                    MainWindow.userData.vectorsLastDoFIll, MainWindow.userData.vectorsLastFill, MainWindow.userData.vectorsLastStroke, (int) MainWindow.userData.vectorsLastStrokeWidth,
                    "", false, false, 0);
    
            page.addElement(element, true);
            element.centerOnCoordinatesY();
            MainWindow.mainScreen.setSelected(element);
            element.setLinkedVectorData(VectorListPane.addLastVector(element));
        });
        browseVector.setOnAction(ae -> browseSVGPath(null));
        
        browsePath.setOnMouseClicked(e -> {
            if(MainWindow.mainScreen.getSelected() instanceof ImageElement){
                browseImagePath(null);
            }else if(MainWindow.mainScreen.getSelected() instanceof VectorElement element){ // VECTOR
                ContextMenu menu = new ContextMenu();
                NodeMenuItem rotate = new NodeMenuItem(TR.tr("paintTab.vectorElements.rotate"), false);
                NodeMenuItem browse = new NodeMenuItem(TR.tr("paintTab.vectorElements.browseSVG"), false);
                menu.getItems().addAll(rotate, browse);
                NodeMenuItem.setupMenu(menu);
    
                rotate.setOnAction(ae -> {
                    DoubleInputAlert inputAlert = new DoubleInputAlert(-360, 360, 90, 90,
                            TR.tr("paintTab.vectorElements.rotate"), TR.tr("paintTab.vectorElements.rotate.dialog.header"), TR.tr("paintTab.vectorElements.rotate.dialog.details"));
                    inputAlert.addApplyButton(ButtonPosition.DEFAULT);
                    inputAlert.addCancelButton(ButtonPosition.CLOSE);
    
                    if(inputAlert.getShowAndWaitIsDefaultButton() && inputAlert.getValue() != null && inputAlert.getValue() != 0){
                        try{
                            path.setText(SVGUtils.rotatePath(element.getPath(), (float) ((double) inputAlert.getValue()), 4));
                        }catch(PathParseException ex){
                            System.err.println(ex.getMessage());
                        }
                    }
                });
                browse.setOnAction(ae -> browseSVGPath(path::setText));
                menu.show(browsePath, e.getScreenX(), e.getScreenY());
            }
        });
        
        // VectorsButtons
        
        vectorCreateCurve.setOnAction((e) -> {
            if(MainWindow.mainScreen.getSelected() instanceof VectorElement vectorElement){
                vectorElement.getPage().getVectorElementPageDrawer().onCreateCurve();
            }
        });
        
        vectorDrawMode.selectedToggleProperty().addListener(this::vectorDrawModeChanged);
        vectorCreateCurve.disableProperty().bind(vectorModePoint.selectedProperty().not());
        vectorStraightLineMode.disableProperty().bind(vectorDrawMode.selectedToggleProperty().isNull());
        vectorUndoPath.setOnAction((e) -> {
            if(MainWindow.mainScreen.getSelected() instanceof VectorElement vectorElement) vectorElement.undoAuto();
        });
        new PressAndHoldManager(vectorUndoPath, 30, () -> {
            if(MainWindow.mainScreen.getSelected() instanceof VectorElement vectorElement) vectorElement.undoAuto();
        });
        
        
        // Listeners
        
        MainWindow.mainScreen.selectedProperty().addListener(this::updateSelected);
        MainWindow.mainScreen.statusProperty().addListener(this::updateDocumentStatus);
        updateSelected(null, null, null);
        
        
    }
    
    private void deleteSelected(){
        Element element = MainWindow.mainScreen.getSelected();
        if(element != null){
            element.delete(true);
        }
    }
    
    private void browseImagePath(CallBackArg<String> callBack){
        File file = FIlesChooserManager.showFileDialog(FIlesChooserManager.SyncVar.LAST_GALLERY_OPEN_DIR, TR.tr("dialog.file.extensionType.image"),
                ImageUtils.ACCEPTED_EXTENSIONS.stream().map((s) -> "*." + s).toList().toArray(new String[0]));
        if(file != null){
            if(callBack == null) path.setText(file.getAbsolutePath());
            else callBack.call(file.getAbsolutePath());
        }
    }
    private void browseSVGPath(CallBackArg<String> callBack){
        File file = FIlesChooserManager.showFileDialog(FIlesChooserManager.SyncVar.LAST_OPEN_DIR, TR.tr("dialog.file.extensionType.svg"),
                SVGUtils.ACCEPTED_EXTENSIONS.stream().map((s) -> "*." + s).toList().toArray(new String[0]));
        if(file != null){
            try{
                SVGFileParser parser = new SVGFileParser(file);
                parser.load();
                
                if(callBack != null){
                    callBack.call(parser.getPath());
                    return;
                }
                
                Color fillColor = parser.getFillColor();
                Color strokeColor = parser.getStrokeColor();
                int strokeWidth = parser.getStrokeWidth();
                if((strokeWidth == 0 || strokeColor == null) && fillColor == null) fillColor = MainWindow.userData.vectorsLastFill;
                
                PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
        
                VectorElement element = new VectorElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(), true,
                        0, 0, GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS,
                        fillColor != null, fillColor == null ? MainWindow.userData.vectorsLastFill : fillColor, strokeColor == null ? MainWindow.userData.vectorsLastStroke : strokeColor, strokeWidth,
                        parser.getPath(), false, false, 0);
        
                page.addElement(element, true);
                element.centerOnCoordinatesY();
                MainWindow.mainScreen.setSelected(element);
                element.setLinkedVectorData(VectorListPane.addLastVector(element));
        
            }catch(ParserConfigurationException | XPathExpressionException | IOException | SAXException ex){
                new ErrorAlert(TR.tr("paintTab.vectorElements.browseSVG.error"), ex.getMessage(), false).show();
                ex.printStackTrace();
            }
        }
    }
    
    private void updateDocumentStatus(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue){
        if(newValue.intValue() == MainScreen.Status.OPEN){
            setAllDisable(false);
        }else{
            setAllDisable(true);
        }
    }
    public void updateSelected(ObservableValue<? extends Element> observable, Element oldValue, Element newValue){
        // Old element
        if(oldValue instanceof GraphicElement gElement){
            if(oldValue instanceof VectorElement element){ // Vector
                element.pathProperty().unbindBidirectional(path.textProperty());
                element.fillProperty().unbind();
                element.strokeProperty().unbind();
                element.doFillProperty().unbind();
                element.strokeWidthProperty().unbind();
                element.arrowLengthProperty().unbind();
            }else if(oldValue instanceof ImageElement element){ // Image
                element.imageIdProperty().unbind();
            }
            gElement.realXProperty().removeListener(this::editSpinXEvent);
            gElement.realYProperty().removeListener(this::editSpinYEvent);
            gElement.realWidthProperty().removeListener(this::editSpinWidthEvent);
            gElement.realHeightProperty().removeListener(this::editSpinHeightEvent);
        }
        
        // Disable/Enable nodes
        if(newValue instanceof GraphicElement gElement){
            setGlobalDisable(false);
    
            if(newValue instanceof VectorElement element){ // Vector
                setVectorsDisable(false);
                
                path.setText(element.getPath());
                vectorStrokeColor.setValue(element.getStroke());
                vectorFillColor.setValue(element.getFill());
                vectorStrokeWidth.getValueFactory().setValue(element.getStrokeWidth());
                doFillButton.setSelected(element.isDoFill());
                spinnerArrowLength.getValueFactory().setValue(element.getArrowLength());
                
                element.pathProperty().bindBidirectional(path.textProperty());
                element.strokeProperty().bind(vectorStrokeColor.valueProperty());
                element.fillProperty().bind(vectorFillColor.valueProperty());
                element.doFillProperty().bind(doFillButton.selectedProperty());
                element.strokeWidthProperty().bind(vectorStrokeWidth.valueProperty());
                element.arrowLengthProperty().bind(spinnerArrowLength.valueProperty());
                
            }else if(newValue instanceof ImageElement element){ // Image
                setVectorsDisable(true);
                path.setText(element.getImageId());
                element.imageIdProperty().bind(path.textProperty());
            }
            
            path.positionCaret(path.getText().length());
            gElement.realXProperty().addListener(this::editSpinXEvent);
            gElement.realYProperty().addListener(this::editSpinYEvent);
            gElement.realWidthProperty().addListener(this::editSpinWidthEvent);
            gElement.realHeightProperty().addListener(this::editSpinHeightEvent);
            spinnerX.getValueFactory().setValue(gElement.getRealX());
            spinnerY.getValueFactory().setValue(gElement.getRealY());
            spinnerWidth.getValueFactory().setValue(gElement.getRealWidth());
            spinnerHeight.getValueFactory().setValue(gElement.getRealHeight());
            repeatMode.getSelectionModel().select(TR.tr(gElement.getRepeatMode().getKey()));
            resizeMode.getSelectionModel().select(TR.tr(gElement.getResizeMode().getKey()));
            
        }else{
            setGlobalDisable(true);
            setVectorsDisable(true);
        }
    }
    
    // Advanced options spinners listeners
    public void editSpinXEvent(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        if(!spinnerX.getValue().equals(newValue)) spinnerX.getValueFactory().setValue(newValue.intValue());
    }
    public void editSpinYEvent(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        if(!spinnerY.getValue().equals(newValue)) spinnerY.getValueFactory().setValue(newValue.intValue());
    }
    public void editSpinWidthEvent(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        if(!spinnerWidth.getValue().equals(newValue)) spinnerWidth.getValueFactory().setValue(newValue.intValue());
    }
    public void editSpinHeightEvent(ObservableValue<? extends Number> observable, Number oldValue, Number newValue){
        if(!spinnerHeight.getValue().equals(newValue)) spinnerHeight.getValueFactory().setValue(newValue.intValue());
    }
    
    private void vectorDrawModeChanged(ObservableValue<? extends Toggle> o, Toggle oldToggle, Toggle newToggle){
        if(MainWindow.mainScreen.getSelected() instanceof VectorElement vectorElement){
            if(newToggle != null){
                if(oldToggle == null) vectorElement.enterEditMode();
            }else{
                vectorElement.quitEditMode();
            }
        }
    }
    
    public void setVectorsDisable(boolean disable){
        vectorUndoPath.setDisable(disable);
        setVectorActionButtonsVisible(!disable);
        setVectorOptionPaneVisible(!disable);
        setVectorAdvancedOptionVisible(!disable);
    }
    public void setVectorActionButtonsVisible(boolean visible){
        if(!visible){ // REMOVE
            commonActionButtons.getChildren().remove(vectorsActonButtons);
            
        }else if(!commonActionButtons.getChildren().contains(vectorsActonButtons)){ // ADD
            commonActionButtons.getChildren().add(vectorsActonButtons);
        }
    }
    public void setVectorOptionPaneVisible(boolean visible){
        if(!visible){ // REMOVE
            root.getChildren().remove(vectorsOptionPane);
        }else if(!root.getChildren().contains(vectorsOptionPane)){ // ADD
            root.getChildren().add(1, vectorsOptionPane);
        }
    }
    public void setVectorAdvancedOptionVisible(boolean visible){
        if(!visible){ // REMOVE
            advancedOptionsContent.getChildren().remove(vectorsAdvancedOptions);
        }else if(!advancedOptionsContent.getChildren().contains(vectorsAdvancedOptions)){ // ADD
            advancedOptionsContent.getChildren().add(vectorsAdvancedOptions);
        }
    }
    private boolean advancedOptionsPaneWasOpen = false;
    public void setGlobalDisable(boolean disable){
        if(disable){
            advancedOptionsPaneWasOpen = advancedOptionsPane.isExpanded();
            advancedOptionsPane.setExpanded(false);
        }else{
            advancedOptionsPane.setExpanded(advancedOptionsPaneWasOpen);
        }
        advancedOptionsPane.setDisable(disable);
        delete.setDisable(disable);
        path.setDisable(disable);
        browsePath.setDisable(disable);
    }
    public void setAllDisable(boolean disable){
        if(disable) setGlobalDisable(true);
        newImage.setDisable(disable);
        newVector.setDisable(disable);
    }
    
    public void openGallery(){
        if(galleryWindow != null){
            galleryWindow.setIconified(false);
            galleryWindow.requestFocus();
        }else{
            galleryWindow = new GalleryWindow();
        }
    }
}
