package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.LinkedHashMap;

public abstract class GraphicElement extends Element{
    
    public enum RepeatMode{
        KEEP_RATIO("paintTab.repeatMode.keepRatio"),
        STRETCH("paintTab.repeatMode.stretch"),
        CROP("paintTab.repeatMode.crop"),
        MULTIPLY("paintTab.repeatMode.multiply");
        
        private final String key;
        
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
        
        private final String key;
        
        ResizeMode(String key){
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
    
    public GraphicElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode){
        super(x, y, pageNumber);
        
        this.repeatMode.set(repeatMode);
        this.resizeMode.set(resizeMode);
        this.realWidth.set(width);
        this.realHeight.set(height);
    }
    
    private Cursor dragType = Cursor.MOVE;
    protected double shiftXFromEnd = 0;
    protected double shiftYFromEnd = 0;
    protected double originWidth = 0;
    protected double originHeight = 0;
    protected double originX = 0;
    protected double originY = 0;
    protected double ratio = 0;
    
    public abstract void initializePage(int page, double x, double y);
    
    protected void setupGeneral(Node... components){
        super.setupGeneral(false, components);
    
        prefWidthProperty().bind(getPage().widthProperty().multiply(realWidth.divide(Element.GRID_WIDTH)));
        prefHeightProperty().bind(getPage().heightProperty().multiply(realHeight.divide(Element.GRID_HEIGHT)));
        Platform.runLater(() -> checkLocation(false));
        
        MainWindow.mainScreen.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(oldValue == this && newValue != this){
                updateGrabIndicators(false);
                menu.hide();
            }else if(oldValue != this && newValue == this){
                updateGrabIndicators(true);
            }
        });
        
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE){
                delete();
                e.consume();
            }
        });
        
        setOnMouseMoved(e -> {
            setCursor(getDragCursorType(e.getX(), e.getY()));
        });
    
        setOnMousePressed(e -> {
            e.consume();
            requestFocus();
            setupMousePressVars(e, null);
            if(e.getButton() == MouseButton.SECONDARY){
                menu.show(getPage(), e.getScreenX(), e.getScreenY());
            }
        });
        
        setOnMouseDragged(e -> {
            if(dragType == Cursor.MOVE){
                double itemX = getLayoutX() + e.getX() - shiftX;
                double itemY = getLayoutY() + e.getY() - shiftY;
                checkLocation(itemX, itemY, true);
            }else{
                //
                //              |
                //            - +
                if(dragType == Cursor.SE_RESIZE){
                    double width = e.getX() + shiftXFromEnd;
                    double height = e.getY() + shiftYFromEnd;

                    if(width < 0) setupMousePressVars(e, Cursor.SW_RESIZE);
                    else if(height < 0) setupMousePressVars(e, Cursor.NE_RESIZE);
                    else{
                        if(doKeepRatio(e)){
                            double requestedRatio = width / height;
                            if(requestedRatio >= ratio) height = width / ratio;
                            else width = height * ratio;
                        }
                        checkLocation(getLayoutX(), getLayoutY(), width, height, false);
                    }

                }else if(dragType == Cursor.S_RESIZE){
                    double height = e.getY() + shiftYFromEnd;

                    if(doKeepRatio(e)){
                        originX = originX + (originWidth - height*ratio)/2;
                        originWidth = height * ratio;
                    }

                    if(height < 0) setupMousePressVars(e, Cursor.N_RESIZE);
                    else checkLocation(originX, getLayoutY(), originWidth, height, false);

                }else if(dragType == Cursor.E_RESIZE){
                    double width = e.getX() + shiftXFromEnd;

                    if(doKeepRatio(e)){
                        originY = originY + (originHeight - width/ratio)/2;
                        originHeight = width / ratio;
                    }

                    if(width < 0) setupMousePressVars(e, Cursor.W_RESIZE);
                    else checkLocation(getLayoutX(), originY, width, originHeight, false);
                }
                //               +
                //
                //          +
                else if(dragType == Cursor.NE_RESIZE){
                    double width = e.getX() + shiftXFromEnd;
                    double newY = getLayoutY() + e.getY() - shiftY;
                    double height = originHeight + (originY - newY);

                    if(width < 0) setupMousePressVars(e, Cursor.NW_RESIZE);
                    else if(height < 0) setupMousePressVars(e, Cursor.SE_RESIZE);
                    else{
                        if(doKeepRatio(e)){
                            double requestedRatio = width / height;
                            if(requestedRatio >= ratio){
                                height = width / ratio;
                                newY = originHeight + originY - height;
                            }
                            else width = height * ratio;
                        }
                        checkLocation(getLayoutX(), newY, width, height, false);
                    }

                }else if(dragType == Cursor.SW_RESIZE){
                    double height = e.getY() + shiftYFromEnd;
                    double newX = getLayoutX() + e.getX() - shiftX;
                    double width = originWidth + (originX - newX);
    
                    if(width < 0) setupMousePressVars(e, Cursor.SE_RESIZE);
                    else if(height < 0) setupMousePressVars(e, Cursor.NW_RESIZE);
                    else{
                        if(doKeepRatio(e)){
                            double requestedRatio = width / height;
                            if(requestedRatio >= ratio){
                                height = width / ratio;
                            }else{
                                width = height * ratio;
                                newX = originWidth + originX - width;
                            }
                        }
                        checkLocation(newX, getLayoutY(), width, height, false);
                    }

                }
                //          + -
                //          |
                //
                else if(dragType == Cursor.NW_RESIZE){
                    double newX = getLayoutX() + e.getX() - shiftX;
                    double width = originWidth + (originX - newX);
                    double newY = getLayoutY() + e.getY() - shiftY;
                    double height = originHeight + (originY - newY);
    
                    if(width < 0) setupMousePressVars(e, Cursor.NE_RESIZE);
                    else if(height < 0) setupMousePressVars(e, Cursor.SW_RESIZE);
                    else{
                        if(doKeepRatio(e)){
                            double requestedRatio = width / height;
                            if(requestedRatio >= ratio){
                                height = width / ratio;
                                newY = originHeight + originY - height;
                            }else{
                                width = height * ratio;
                                newX = originWidth + originX - width;
                            }
                        }
                        checkLocation(newX, newY, width, height, false);
                    }
                }else if(dragType == Cursor.N_RESIZE){
                    double newY = getLayoutY() + e.getY() - shiftY;
                    double height = originHeight + (originY - newY);
    
                    if(doKeepRatio(e)){
                        originX = originX + (originWidth - height*ratio)/2;
                        originWidth = height * ratio;
                    }
                    
                    if(height < 0) setupMousePressVars(e, Cursor.S_RESIZE);
                    else checkLocation(originX, newY, originWidth, height, false);

                }else if(dragType == Cursor.W_RESIZE){
                    double newX = getLayoutX() + e.getX() - shiftX;
                    double width = originWidth + (originX - newX);
    
                    if(doKeepRatio(e)){
                        originY = originY + (originHeight - width/ratio)/2;
                        originHeight = width/ratio;
                    }
                    
                    if(width < 0) setupMousePressVars(e, Cursor.E_RESIZE);
                    else checkLocation(newX, originY, width, originHeight, false);

                }
            }
            
        });

        setOnMouseReleased(e -> {
            Edition.setUnsave();
            if(dragType == Cursor.MOVE){
                double itemX = getLayoutX() + e.getX() - shiftX;
                double itemY = getLayoutY() + e.getY() - shiftY;

                checkLocation(itemX, itemY, true);
                PageRenderer newPage = MainWindow.mainScreen.document.getPreciseMouseCurrentPage();
                if(newPage != null){
                    if(newPage.getPage() != getPageNumber()){
                        MainWindow.mainScreen.setSelected(null);

                        switchPage(newPage.getPage());
                        itemY = newPage.getPreciseMouseY() - shiftY;
                        checkLocation(itemX, itemY, true);

                        MainWindow.mainScreen.setSelected(this);
                    }
                }
                checkLocation(false);
                onMouseRelease();
            }else{
                checkLocation(false);
                if(getWidth() < 20 || getHeight() < 20){
                    checkLocation(getLayoutX(), getLayoutY(),
                            StringUtils.clamp(getWidth(), 10, (int) GRID_WIDTH), StringUtils.clamp(getHeight(), 10, (int) GRID_HEIGHT), false);
                }
            }
        });
    }
    
    private void setupMousePressVars(MouseEvent e, Cursor forceDragType){
        shiftX = (int) e.getX();
        shiftY = (int) e.getY();
        shiftXFromEnd = (getWidth() - e.getX());
        shiftYFromEnd = (getHeight() - e.getY());
        originWidth = getWidth();
        originHeight = getHeight();
        originX = getLayoutX();
        originY = getLayoutY();
        menu.hide(); select();
        
        if(forceDragType != null){
            dragType = forceDragType;
            shiftX = 0;
            shiftY = 0;
        }else{
            dragType = getDragCursorType(e.getX(), e.getY());
            ratio = originWidth / originHeight;
        }
        setCursor(dragType);
        
    }
    
    public Cursor getDragCursorType(double x, double y){
        int grabSize = (int) (10 * (1/MainWindow.mainScreen.getCurrentPaneScale()));
        
        // RESIZE
        if(x < grabSize){ // Left Side
            if(y < grabSize){ // Top Left
                return Cursor.NW_RESIZE;
            }else if(y > getHeight()-grabSize){ // Bottom Left
                return Cursor.SW_RESIZE;
            }else{ // Left only
                return Cursor.W_RESIZE;
            }
        }
        if(x > getWidth()-grabSize){ // Right Side
            if(y < grabSize){ // Top Right
                return Cursor.NE_RESIZE;
            }else if(y > getHeight()-grabSize){ // Bottom Right
                return Cursor.SE_RESIZE;
            }else{ // Right only
                return Cursor.E_RESIZE;
            }
        }
    
        if(y < grabSize){ // Top only
            return Cursor.N_RESIZE;
        }
        if(y > getHeight()-grabSize){ // Bottom only
            return Cursor.S_RESIZE;
        }
        return Cursor.MOVE;
    }
    
    protected static BorderStroke STROKE_SIDE_EDGES = new BorderStroke(Color.color(0 / 255.0, 100 / 255.0, 255 / 255.0),
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 2, 0, 2));
    public void updateGrabIndicators(boolean selected){
        if(!selected){
            setBorder(null);
            getChildren().clear();
        }
        switch(getResizeMode()){
            case CORNERS -> {
                if(selected){
                    setBorder(new Border(STROKE_DEFAULT));
                    getChildren().setAll(getPoint(true, true), getPoint(true, false), getPoint(false, true), getPoint(false, false));
                }
            }
            case SIDE_EDGES -> {
                if(selected){
                    setBorder(new Border(STROKE_DEFAULT, STROKE_SIDE_EDGES));
                    getChildren().clear();
                }
            }
            case OPPOSITE_CORNERS -> {
                if(selected){
                    setBorder(null);
                    getChildren().setAll(getPoint(false, true), getPoint(true, false));
                }
            }
        }
    }
    
    private static final int POINT_WIDTH = 6;
    private static final int POINT_OUTER = 2;
    private Region getPoint(boolean top, boolean left){
        Region region = new Region();
        region.setPrefWidth(POINT_WIDTH);
        region.setPrefHeight(POINT_WIDTH);
        region.setBackground(new Background(new BackgroundFill(Color.color(0 / 255.0, 100 / 255.0, 255 / 255.0), new CornerRadii(1), Insets.EMPTY)));

        if(top) region.setLayoutY(0d - POINT_OUTER);
        else region.layoutYProperty().bind(heightProperty().subtract(POINT_WIDTH - POINT_OUTER));
        if(left) region.setLayoutX(0d - POINT_OUTER);
        else region.layoutXProperty().bind(widthProperty().subtract(POINT_WIDTH - POINT_OUTER));
        
        return region;
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        resizeMode.addListener((observable, oldValue, newValue) -> {
            updateGrabIndicators(true);
        });
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
    public void doubleClick(){
    
    }
    
    // READERS AND WRITERS
    
    protected LinkedHashMap<Object, Object> getYAMLPartialData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("width", getRealWidth());
        data.put("height", getRealHeight());
        data.put("repeatMode", getRepeatMode().name());
        data.put("resizeMode", getResizeMode().name());
        
        return data;
    }
    
    // GETTER AND SETTER

    public boolean doKeepRatio(MouseEvent e){
        if(e == null){
            return getRepeatMode() == RepeatMode.KEEP_RATIO;
        }else{
            if(getRepeatMode() == RepeatMode.KEEP_RATIO) return !e.isShiftDown();
            else return e.isShiftDown();
        }
    }
    
    @Override
    public float getAlwaysHeight(){
        throw new RuntimeException("Unable to getAlwaysHeight on GraphicElement, use getRealHeight instead.");
    }
    public int getRealWidth(){
        return realWidth.get();
    }
    public IntegerProperty realWidthProperty(){
        return realWidth;
    }
    public void setRealWidth(int realWidth){
        this.realWidth.set(realWidth);
    }
    @Override
    public int getRealHeight(){
        return realHeight.get();
    }
    public IntegerProperty realHeightProperty(){
        return realHeight;
    }
    public void setRealHeight(int realHeight){
        this.realHeight.set(realHeight);
    }
    public RepeatMode getRepeatMode(){
        return repeatMode.get();
    }
    public ObjectProperty<RepeatMode> repeatModeProperty(){
        return repeatMode;
    }
    public void setRepeatMode(RepeatMode repeatMode){
        this.repeatMode.set(repeatMode);
    }
    public ResizeMode getResizeMode(){
        return resizeMode.get();
    }
    public ObjectProperty<ResizeMode> resizeModeProperty(){
        return resizeMode;
    }
    public void setResizeMode(ResizeMode resizeMode){
        this.resizeMode.set(resizeMode);
    }
    
}