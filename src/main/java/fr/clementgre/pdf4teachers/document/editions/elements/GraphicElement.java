package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;

import java.util.LinkedHashMap;

public abstract class GraphicElement extends Element{

    public enum RepeatMode{
        AUTO("classics.automatic.abbreviated"),
        STRETCH("paintTab.repeatMode.stretch"),
        KEEP_RATIO("paintTab.repeatMode.keepRatio"),
        CROP("paintTab.repeatMode.crop"),
        MULTIPLY_X("paintTab.repeatMode.multiplyX"),
        MULTIPLY_Y("paintTab.repeatMode.multiplyY");

        private String key;
        RepeatMode(String key){
            this.key = key;
        }
        public String getKey(){
            return key;
        }
    }
    public enum ResizeMode{
        CORNERS("paintTab.resizeMode.corners"),
        OPPOSITE_CORNERS("paintTab.resizeMode.oppositeCorners"),
        SIDE_EDGES("paintTab.resizeMode.sideEdges");

        private String key;
        ResizeMode(String key){
            this.key = key;
        }
        public String getKey(){
            return key;
        }
    }
    public enum RotateMode{
        NEAR_CORNERS("paintTab.resizeMode.nearCorners"),
        NONE("classics.none.feminine");

        private String key;
        RotateMode(String key){
            this.key = key;
        }
        public String getKey(){
            return key;
        }
    }

    protected IntegerProperty realWidth = new SimpleIntegerProperty();
    protected IntegerProperty realHeight = new SimpleIntegerProperty();

    protected ObjectProperty<RepeatMode> repeatMode = new SimpleObjectProperty<>();
    protected ObjectProperty<ResizeMode> resizeMode = new SimpleObjectProperty<>();
    protected ObjectProperty<RotateMode> rotateMode = new SimpleObjectProperty<>();

    public GraphicElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode, RotateMode rotateMode){
        super(x, y, pageNumber);

        this.repeatMode.set(repeatMode);
        this.resizeMode.set(resizeMode);
        this.rotateMode.set(rotateMode);

        prefWidthProperty().bind(getPage().widthProperty().multiply(realWidth.divide(Element.GRID_WIDTH)));
        prefHeightProperty().bind(getPage().heightProperty().multiply(realHeight.divide(Element.GRID_HEIGHT)));

        setRealWidth(width);
        setRealHeight(height);
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

        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("actions.delete"), false);
        item1.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        item1.setToolTip(TR.tr("elements.delete.tooltip"));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("actions.duplicate"), false);
        item2.setToolTip(TR.tr("elements.duplicate.tooltip"));
        menu.getItems().addAll(item1, item2);
        NodeMenuItem.setupMenu(menu);

        item1.setOnAction(e -> delete());
        item2.setOnAction(e -> cloneOnDocument());
    }

    // ACTIONS

    @Override
    public void select(){
        super.selectPartial();
        SideBar.selectTab(MainWindow.paintTab);

    }
    @Override
    public void doubleClick() {

    }

    // READERS AND WRITERS

    protected LinkedHashMap<Object, Object> getYAMLPartialData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("width", getRealWidth());
        data.put("height", getRealHeight());
        data.put("repeatMode", getRepeatMode().name());
        data.put("resizeMode", getResizeMode().name());
        data.put("rotateMode", getRotateMode().name());


        return data;
    }

    // GETTER AND SETTER


    public int getRealWidth() {
        return realWidth.get();
    }
    public IntegerProperty realWidthProperty() {
        return realWidth;
    }
    public void setRealWidth(int realWidth) {
        this.realWidth.set(realWidth);
    }
    @Override
    public int getRealHeight() {
        return realHeight.get();
    }
    public IntegerProperty realHeightProperty() {
        return realHeight;
    }
    public void setRealHeight(int realHeight) {
        this.realHeight.set(realHeight);
    }

    public RepeatMode getRepeatMode() {
        return repeatMode.get();
    }
    public ObjectProperty<RepeatMode> repeatModeProperty() {
        return repeatMode;
    }
    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode.set(repeatMode);
    }
    public ResizeMode getResizeMode() {
        return resizeMode.get();
    }
    public ObjectProperty<ResizeMode> resizeModeProperty() {
        return resizeMode;
    }
    public void setResizeMode(ResizeMode resizeMode) {
        this.resizeMode.set(resizeMode);
    }
    public RotateMode getRotateMode() {
        return rotateMode.get();
    }
    public ObjectProperty<RotateMode> rotateModeProperty() {
        return rotateMode;
    }
    public void setRotateMode(RotateMode rotateMode) {
        this.rotateMode.set(rotateMode);
    }
}
