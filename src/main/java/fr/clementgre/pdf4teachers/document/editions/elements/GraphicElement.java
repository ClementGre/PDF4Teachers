/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.MoveUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.ResizeUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;

public abstract class GraphicElement extends Element {
    
    public enum RepeatMode {
        AUTO("status.auto"),
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
    
    public enum ResizeMode {
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
    
    protected boolean allowShiftToInvertResizeMode = true;
    
    protected IntegerProperty realWidth = new SimpleIntegerProperty();
    protected IntegerProperty realHeight = new SimpleIntegerProperty();
    
    protected ObjectProperty<RepeatMode> repeatMode = new SimpleObjectProperty<>();
    protected ObjectProperty<ResizeMode> resizeMode = new SimpleObjectProperty<>();
    
    public GraphicElement(int x, int y, int pageNumber, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode){
        super(x, y, pageNumber);
        
        this.repeatMode.set(repeatMode);
        this.resizeMode.set(resizeMode);
        this.realWidth.set(width);
        this.realHeight.set(height);
    }
    
    private Cursor dragType = PlatformUtils.CURSOR_MOVE;
    protected double shiftXFromEnd;
    protected double shiftYFromEnd;
    protected double originWidth;
    protected double originHeight;
    protected double originX;
    protected double originY;
    protected double ratio;
    
    public abstract void initializePage(int page, double x, double y);
    public abstract void defineSizeAuto();
    public abstract void incrementUsesAndLastUse();
    public abstract double getRatio();
    
    
    protected void setupGeneral(Node... components){
        super.setupGeneral(false, components);
        
        prefWidthProperty().bind(getPage().widthProperty().multiply(realWidth.divide(Element.GRID_WIDTH)));
        prefHeightProperty().bind(getPage().heightProperty().multiply(realHeight.divide(Element.GRID_HEIGHT)));
        Platform.runLater(() -> checkLocation(false));
        
        setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.DELETE || (e.getCode() == KeyCode.BACK_SPACE && e.isShortcutDown())){
                delete(true, UType.ELEMENT);
                e.consume();
            }else if(this instanceof VectorElement element){
                if(e.getCode() == KeyCode.ENTER){
                    e.consume();
                    element.enterEditMode();
                }else if(e.getCode() == KeyCode.BACK_SPACE){
                    e.consume();
                    element.undoLastAction();
                }
            }
        });
        
        setOnMouseMoved(e -> {
            if(MainWindow.mainScreen.isEditPagesMode()){
                setCursor(PlatformUtils.CURSOR_MOVE);
            }else{
                setCursor(getDragCursorType(e.getX(), e.getY()));
            }
        });
        
        setOnMousePressed(e -> {
            if(e.getButton() == MouseButton.MIDDLE) setCursor(Cursor.CLOSED_HAND);
            if(!(e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY)) return;
            
            wasInEditPagesModeWhenMousePressed = MainWindow.mainScreen.isEditPagesMode();
            if(wasInEditPagesModeWhenMousePressed) return;
            e.consume();
            
            dragAlreadyDetected = false;
            requestFocus();
            
            if(e.getButton() == MouseButton.SECONDARY){
                menu.show(getPage(), e.getScreenX(), e.getScreenY());
            }else{
                setupMousePressVars(e.getX(), e.getY(), null, false, true);
            }
        });
        
        setOnMouseClicked(e -> {
            if(!(e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY)) return;
            
            if(MainWindow.mainScreen.isEditPagesMode()) return;
            e.consume();
            requestFocus();
            if(e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY){
                onDoubleClick();
            }
        });
        
        setOnMouseDragged(e -> {
            if(wasInEditPagesModeWhenMousePressed || e.getButton() != MouseButton.PRIMARY) return;
            
            if(dragType == PlatformUtils.CURSOR_MOVE){
                if(!dragAlreadyDetected){ // UNDO system
                    MainWindow.mainScreen.registerNewAction(new MoveUndoAction(UType.ELEMENT, this));
                    dragAlreadyDetected = true;
                }
                
                double itemX = getLayoutX() + e.getX() - shiftX;
                double itemY = getLayoutY() + e.getY() - shiftY;
                checkLocation(itemX, itemY, true);
            }else{
                if(!dragAlreadyDetected){ // UNDO system
                    MainWindow.mainScreen.registerNewAction(new ResizeUndoAction(UType.ELEMENT, this));
                    dragAlreadyDetected = true;
                }
                
                simulateDragToResize(e.getX(), e.getY(), e.isShiftDown());
            }
        });
        
        setOnMouseReleased(e -> {
            if(e.getButton() == MouseButton.MIDDLE) setCursor(getDragCursorType(e.getX(), e.getY()));
            if(wasInEditPagesModeWhenMousePressed || e.getButton() != MouseButton.PRIMARY) return;
            Edition.setUnsave("GraphicElementMouseRelease");
            
            if(dragType == PlatformUtils.CURSOR_MOVE){
                double itemX = getLayoutX() + e.getX() - shiftX;
                double itemY = getLayoutY() + e.getY() - shiftY;
                
                checkLocation(itemX, itemY, true);
                PageRenderer newPage = MainWindow.mainScreen.document.getPreciseMouseCurrentPage();
                if(newPage != null){
                    if(newPage.getPage() != getPageNumber()){
                        MainWindow.mainScreen.setSelected(null);
                        
                        switchPage(newPage.getPage());
                        itemX = newPage.getPreciseMouseX() - shiftX;
                        itemY = newPage.getPreciseMouseY() - shiftY;
                        checkLocation(itemX, itemY, true);
                        
                        MainWindow.mainScreen.setSelected(this);
                    }
                }
                checkLocation(false);
                onMouseRelease();
            }else{
                simulateReleaseFromResize();
            }
        });
    }
    
    @Override
    protected void onSelected(){
        updateGrabIndicators(true);
    }
    
    @Override
    protected void onDeSelected(){
        updateGrabIndicators(false);
        menu.hide();
    }
    
    public void simulateReleaseFromResize(){
        checkLocation(false);
        if(getWidth() < 10 && getHeight() < 10){
            checkLocation(getLayoutX(), getLayoutY(),
                    MathUtils.clamp(getWidth(), 10, (int) GRID_WIDTH), MathUtils.clamp(getHeight(), 10, (int) GRID_HEIGHT), false);
        }
    }
    
    public void simulateDragToResize(double x, double y, boolean shift){
        if(!allowShiftToInvertResizeMode) shift = false;
        //
        //              |
        //            - +
        if(dragType == Cursor.SE_RESIZE){
            double width = Math.min(x + shiftXFromEnd, getPage().getWidth() - originX);
            double height = Math.min(y + shiftYFromEnd, getPage().getHeight() - originY);
            
            if(width < 0) invertX(x, y, Cursor.SW_RESIZE);
            else if(height < 0) invertY(x, y, Cursor.NE_RESIZE);
            else{
                if(doKeepRatio(shift, true)){
                    double requestedRatio = width / height;
                    if(requestedRatio >= ratio){
                        height = Math.min(width / ratio, getPage().getHeight() - originY);
                        width = height * ratio;
                    }else{
                        width = Math.min(height * ratio, getPage().getWidth() - originX);
                        height = width / ratio; // In case the width is > than the page, we re-edit the height so the ratio is respected
                    }
                }
                checkLocation(getLayoutX(), getLayoutY(), width, height, false);
            }
            
        }else if(dragType == Cursor.S_RESIZE){
            double height = Math.min(y + shiftYFromEnd, getPage().getHeight() - originY);
            
            if(doKeepRatio(shift, false)){
                originX = Math.max(0, originX + (originWidth - height * ratio) / 2);
                originWidth = Math.min(height * ratio, getPage().getWidth() - originX);
                height = originWidth / ratio; // In case the width is > than the page, we re-edit the height so the ratio is respected
            }
            
            if(height < 0) invertY(x, y, Cursor.N_RESIZE);
            else checkLocation(originX, getLayoutY(), originWidth, height, false);
            
        }else if(dragType == Cursor.E_RESIZE){
            double width = Math.min(x + shiftXFromEnd, getPage().getWidth() - originX);
            
            if(doKeepRatio(shift, false)){
                originY = Math.max(0, originY + (originHeight - width / ratio) / 2);
                originHeight = Math.min(width / ratio, getPage().getHeight() - originY);
                width = originHeight * ratio;
            }
            
            if(width < 0) invertX(x, y, Cursor.W_RESIZE);
            else checkLocation(getLayoutX(), originY, width, originHeight, false);
        }
        //               +
        //
        //          +
        else if(dragType == Cursor.NE_RESIZE){
            double width = Math.min(x + shiftXFromEnd, getPage().getWidth() - originX);
            double newY = Math.max(getLayoutY() + y - shiftY, 0);
            double height = Math.min(originHeight + (originY - newY), getPage().getHeight());
            
            if(width < 0) invertX(x, y, Cursor.NW_RESIZE);
            else if(height < 0) invertY(x, y, Cursor.SE_RESIZE);
            else{
                if(doKeepRatio(shift, true)){
                    double requestedRatio = width / height;
                    if(requestedRatio >= ratio){
                        height = Math.min(width / ratio, originHeight + originY);
                        width = height * ratio;
                    }else{
                        width = Math.min(height * ratio, getPage().getWidth() - originX);
                        height = width / ratio;
                    }
                    newY = originHeight + originY - height;
                }
                checkLocation(getLayoutX(), newY, width, height, false);
            }
            
        }else if(dragType == Cursor.SW_RESIZE){
            double height = Math.min(y + shiftYFromEnd, getPage().getHeight() - originY);
            double newX = Math.max(getLayoutX() + x - shiftX, 0);
            double width = Math.min(originWidth + (originX - newX), getPage().getWidth());
            
            if(width < 0) invertX(x, y, Cursor.SE_RESIZE);
            else if(height < 0) invertY(x, y, Cursor.NW_RESIZE);
            else{
                if(doKeepRatio(shift, true)){
                    double requestedRatio = width / height;
                    if(requestedRatio >= ratio){
                        height = Math.min(width / ratio, getPage().getHeight() - originY);
                        width = height * ratio;
                    }else{
                        width = Math.min(height * ratio, originWidth + originX);
                        height = width / ratio; // In case the width is > than the page, we re-edit the height so the ratio is respected
                    }
                    newX = originWidth + originX - width;
                }
                checkLocation(newX, getLayoutY(), width, height, false);
            }
            
        }
        //          + -
        //          |
        //
        else if(dragType == Cursor.NW_RESIZE){
            double newX = Math.max(getLayoutX() + x - shiftX, 0);
            double width = Math.min(originWidth + (originX - newX), getPage().getWidth());
            double newY = Math.max(getLayoutY() + y - shiftY, 0);
            double height = Math.min(originHeight + (originY - newY), getPage().getHeight());
            
            if(width < 0) invertX(x, y, Cursor.NE_RESIZE);
            else if(height < 0) invertY(x, y, Cursor.SW_RESIZE);
            else{
                if(doKeepRatio(shift, true)){
                    double requestedRatio = width / height;
                    if(requestedRatio >= ratio){
                        height = Math.min(width / ratio, originHeight + originY);
                        width = height * ratio;
                    }else{
                        width = Math.min(height * ratio, originWidth + originX);
                        height = width / ratio; // In case the width is > than the page, we re-edit the height so the ratio is respected
                    }
                    newX = originWidth + originX - width;
                    newY = originHeight + originY - height;
                }
                checkLocation(newX, newY, width, height, false);
            }
        }else if(dragType == Cursor.N_RESIZE){
            double newY = Math.max(getLayoutY() + y - shiftY, 0);
            double height = Math.min(originHeight + (originY - newY), getPage().getHeight());
            
            if(doKeepRatio(shift, false)){
                originX = Math.max(0, originX + (originWidth - height * ratio) / 2);
                originWidth = Math.min(height * ratio, getPage().getWidth() - originX);
                // In case the width is > than the page, we re-edit the height & newY so the ratio is respected
                newY += height - originWidth / ratio;
                height = originWidth / ratio;
            }
            
            if(height < 0) invertY(x, y, Cursor.S_RESIZE);
            else checkLocation(originX, newY, originWidth, height, false);
            
        }else if(dragType == Cursor.W_RESIZE){
            double newX = Math.max(getLayoutX() + x - shiftX, 0);
            double width = Math.min(originWidth + (originX - newX), getPage().getWidth());
            
            if(doKeepRatio(shift, false)){
                originY = Math.max(0, originY + (originHeight - width / ratio) / 2);
                originHeight = Math.min(width / ratio, getPage().getHeight() - originY);
                // In case the height is > than the page, we re-edit the width & newX so the ratio is respected
                newX += width - originHeight * ratio;
                width = originHeight * ratio;
            }
            
            if(width < 0) invertX(x, y, Cursor.E_RESIZE);
            else checkLocation(newX, originY, width, originHeight, false);
            
        }
    }
    
    public void setupMousePressVars(double x, double y, Cursor forceDragType, boolean originalRatio, boolean dimensionsFromReal){
        
        if(dimensionsFromReal){
            shiftXFromEnd = (getRealWidth() / GRID_WIDTH * getPage().getWidth() - x);
            shiftYFromEnd = (getRealHeight() / GRID_HEIGHT * getPage().getHeight() - y);
            originWidth = getRealWidth() / GRID_WIDTH * getPage().getWidth();
            originHeight = getRealHeight() / GRID_HEIGHT * getPage().getHeight();
            originX = getRealX() / GRID_WIDTH * getPage().getWidth();
            originY = getRealY() / GRID_HEIGHT * getPage().getHeight();
        }else{
            shiftXFromEnd = (getWidth() - x);
            shiftYFromEnd = (getHeight() - y);
            originWidth = getWidth();
            originHeight = getHeight();
            originX = getLayoutX();
            originY = getLayoutY();
        }
        
        if(forceDragType != null){
            dragType = forceDragType;
            
            shiftX = 0;
            shiftY = 0;
        }else{
            dragType = getDragCursorType(x, y);
            if(originalRatio) ratio = getRatio();
            else ratio = originWidth / originHeight;
            
            shiftX = x;
            shiftY = y;
        }
        
        menu.hide();
        setCursor(dragType);
        select();
    }
    
    public Cursor getDragCursorType(double x, double y){
        if(MainWindow.mainScreen.getSelected() != this) return PlatformUtils.CURSOR_MOVE;
        
        int grabSize = (int) (10 * (1 / MainWindow.mainScreen.getCurrentPaneScale()));
        
        if(getResizeMode() == ResizeMode.OPPOSITE_CORNERS){
            if(this instanceof VectorElement ve && ve.isInvertX() != ve.isInvertY()){
                if(x < grabSize && y < grabSize){ // Top Left
                    return Cursor.NW_RESIZE;
                }
                if(x > getWidth() - grabSize && y > getHeight() - grabSize){ // Bottom Right
                    return Cursor.SE_RESIZE;
                }
            }else{
                if(x < grabSize && y > getHeight() - grabSize){ // Bottom Left
                    return Cursor.SW_RESIZE;
                }
                if(x > getWidth() - grabSize && y < grabSize){ // Top Right
                    return Cursor.NE_RESIZE;
                }
            }
            
        }else if(getResizeMode() == ResizeMode.SIDE_EDGES){
            
            if(x < grabSize){ // Left Side
                return Cursor.W_RESIZE;
            }
            if(x > getWidth() - grabSize){ // Right Side
                return Cursor.E_RESIZE;
            }
            if(y < grabSize){ // Top only
                return Cursor.N_RESIZE;
            }
            if(y > getHeight() - grabSize){ // Bottom only
                return Cursor.S_RESIZE;
            }
            
        }else{
            // RESIZE
            if(x < grabSize){ // Left Side
                if(y < grabSize){ // Top Left
                    return Cursor.NW_RESIZE;
                }
                if(y > getHeight() - grabSize){ // Bottom Left
                    return Cursor.SW_RESIZE;
                }
                // Left only
                return Cursor.W_RESIZE;
            }
            if(x > getWidth() - grabSize){ // Right Side
                if(y < grabSize){ // Top Right
                    return Cursor.NE_RESIZE;
                }
                if(y > getHeight() - grabSize){ // Bottom Right
                    return Cursor.SE_RESIZE;
                }
                // Right only
                return Cursor.E_RESIZE;
            }
            
            if(y < grabSize){ // Top only
                return Cursor.N_RESIZE;
            }
            if(y > getHeight() - grabSize){ // Bottom only
                return Cursor.S_RESIZE;
            }
        }
        
        return PlatformUtils.CURSOR_MOVE;
    }
    
    protected static BorderStroke STROKE_SIDE_EDGES = new BorderStroke(Color.color(0 / 255.0, 100 / 255.0, 255 / 255.0),
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 2, 0, 2));
    
    public void updateGrabIndicators(boolean selected){
        if(!selected){
            setBorder(null);
            getChildren().removeIf((node) -> node instanceof GrabPoint);
            
        }else{
            getChildren().stream()
                    .filter(node -> node instanceof GrabPoint)
                    .forEach(node -> node.setVisible(false));
            
            switch(getResizeMode()){
                case CORNERS -> {
                    setBorder(new Border(STROKE_DEFAULT));
                    getChildren().add(getGrabPoint(true, true, true));
                    getChildren().add(getGrabPoint(true, false, true));
                    getChildren().add(getGrabPoint(false, true, true));
                    getChildren().add(getGrabPoint(false, false, true));
                }
                case SIDE_EDGES -> {
                    setBorder(new Border(STROKE_DEFAULT, STROKE_SIDE_EDGES));
                }
                case OPPOSITE_CORNERS -> {
                    setBorder(null);
                    if(this instanceof VectorElement ve && ve.isInvertX() != ve.isInvertY()){
                        getChildren().add(getGrabPoint(true, true, false));
                        getChildren().add(getGrabPoint(false, false, false));
                    }else{
                        getChildren().add(getGrabPoint(true, false, false));
                        getChildren().add(getGrabPoint(false, true, false));
                    }
                    
                }
            }
        }
    }
    
    private void invertX(double x, double y, Cursor forceDragType){
        setupMousePressVars(x, y, forceDragType, false, true);
        if(this instanceof VectorElement ve){
            ve.setInvertX(!ve.isInvertX());
            updateGrabIndicators(true);
        }
    }
    private void invertY(double x, double y, Cursor forceDragType){
        setupMousePressVars(x, y, forceDragType, false, true);
        if(this instanceof VectorElement ve){
            ve.setInvertY(!ve.isInvertY());
            updateGrabIndicators(true);
        }
    }
    
    private Region getGrabPoint(boolean top, boolean left, boolean withBorder){
        Region region = new GrabPoint();
        
        double outer = GrabPoint.POINT_OUTER;
        if(withBorder){
            outer -= STROKE_DEFAULT.getWidths().getTop() / 2;
        }
        
        if(top) region.setLayoutY(-outer);
        else region.layoutYProperty().bind(heightProperty().subtract(GrabPoint.POINT_WIDTH - outer));
        if(left) region.setLayoutX(-outer);
        else region.layoutXProperty().bind(widthProperty().subtract(GrabPoint.POINT_WIDTH - outer));
        
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
        
        NodeMenuItem item1 = new NodeMenuItem(TR.tr("actions.delete"), false);
        item1.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        item1.setToolTip(TR.tr("elements.delete.tooltip"));
        NodeMenuItem item2 = new NodeMenuItem(TR.tr("actions.duplicate"), false);
        item2.setToolTip(TR.tr("elements.duplicate.tooltip"));
        
        NodeMenuItem item3 = new NodeMenuItem(TR.tr("paintTab.resetRatio"), false);
        item3.setOnAction(e -> {
            checkLocation(getLayoutX(), getLayoutY(), getWidth(), getWidth() / getRatio(), false);
        });
        
        menu.getItems().addAll(item1, item2, item3);
        NodeMenuItem.setupMenu(menu);
        
        item1.setOnAction(e -> delete(true, UType.ELEMENT));
        item2.setOnAction(e -> cloneOnDocument());
    }
    
    // ACTIONS
    
    @Override
    public void select(){
        super.selectPartial();
        requestFocus();
    }
    
    @Override
    public void onDoubleClickAfterSelected(){
        // This method is not available with GraphicElements, listeners are not coded to detect clicks after selecting.
    }
    @Override
    public void onDoubleClick(){
    
    }
    
    @Override
    public void switchPage(int page){
        double oldPageWidth = getPage().getWidth();
        double oldPageHeight = getPage().getHeight();
        
        super.switchPage(page);
        
        // When the new page is just created, its dimensions are not defined yet.
        if(getPage().getHeight() == 0) Platform.runLater(() -> updateDimensionsAfterSwitchingPage(oldPageWidth, oldPageHeight));
        else updateDimensionsAfterSwitchingPage(oldPageWidth, oldPageHeight);
        
    }
    private void updateDimensionsAfterSwitchingPage(double oldPageWidth, double oldPageHeight){
        setRealHeight((int) (getRealHeight() * oldPageHeight / getPage().getHeight()));
        setRealWidth((int) (getRealWidth() * oldPageWidth / getPage().getWidth()));
    
        prefWidthProperty().bind(getPage().widthProperty().multiply(realWidth.divide(Element.GRID_WIDTH)));
        prefHeightProperty().bind(getPage().heightProperty().multiply(realHeight.divide(Element.GRID_HEIGHT)));
    }
    
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
        prefHeightProperty().unbind();
        prefWidthProperty().unbind();
    }
    
    @Override
    public void restoredToDocument(){
        super.restoredToDocument();
        prefWidthProperty().bind(getPage().widthProperty().multiply(realWidth.divide(Element.GRID_WIDTH)));
        prefHeightProperty().bind(getPage().heightProperty().multiply(realHeight.divide(Element.GRID_HEIGHT)));
    }
    
    @Override
    public void size(double scale){
        double oldWidth = getWidth();
        double oldHeight = getHeight();
        double newWidth = oldWidth*scale;
        double newHeight = oldHeight*scale;
    
        checkLocation(getLayoutX()/* + (oldWidth - newWidth)/2d*/, getLayoutY()/* + (oldHeight - newHeight)/2d*/, newWidth, newHeight, false);
    }
    
    // READERS AND WRITERS
    
    @Override
    protected LinkedHashMap<Object, Object> getYAMLPartialData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("width", getRealWidth());
        data.put("height", getRealHeight());
        data.put("repeatMode", getRepeatMode().name());
        data.put("resizeMode", getResizeMode().name());
        
        return data;
    }
    
    // GETTER AND SETTER
    
    public boolean doKeepRatio(boolean shift, boolean angle){
        if(getRepeatMode() == RepeatMode.AUTO) return angle != shift;
        if(getRepeatMode() == RepeatMode.KEEP_RATIO) return !shift;
        return shift;
    }
    
    @Override
    public float getBoundsHeight(){
        throw new RuntimeException("Unable to getAlwaysHeight on GraphicElement, use getRealHeight instead.");
    }
    @Override
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
