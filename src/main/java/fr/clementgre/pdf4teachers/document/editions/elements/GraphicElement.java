package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.LinkedHashMap;

public abstract class GraphicElement extends Element{
    
    public enum RepeatMode{
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
    
    private Cursor dragType = Cursor.MOVE;
    protected double shiftXFromEnd = 0;
    protected double shiftYFromEnd = 0;
    protected double originWidth = 0;
    protected double originHeight = 0;
    protected double originX = 0;
    protected double originY = 0;
    protected double ratio = 0;

    protected void setupGeneral(Node... components){
        super.setupGeneral(false, components);
    
        setOnMouseMoved(e -> {
            setCursor(getDragCursorType(e.getX(), e.getY()));
        });
    
        setOnMousePressed(e -> {
            e.consume();
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

                    if(doKeepRatio(e)){
                        double requestedRatio = width / height;
                        if(requestedRatio >= ratio) height = width / ratio;
                        else width = height * ratio;
                    }

                    if(width < 0) setupMousePressVars(e, Cursor.SW_RESIZE);
                    if(height < 0) setupMousePressVars(e, Cursor.NE_RESIZE);
                    else checkLocation(getLayoutX(), getLayoutY(), width, height, false);

                }else if(dragType == Cursor.S_RESIZE){
                    double height = e.getY() + shiftYFromEnd;

                    if(doKeepRatio(e)){
                        originWidth = height * ratio;
                    }

                    if(height < 0) setupMousePressVars(e, Cursor.N_RESIZE);
                    else checkLocation(getLayoutX(), getLayoutY(), originWidth, height, false);

                }else if(dragType == Cursor.E_RESIZE){
                    double width = e.getX() + shiftXFromEnd;

                    if(doKeepRatio(e)){
                        originHeight = width / ratio;
                    }

                    if(width < 0) setupMousePressVars(e, Cursor.W_RESIZE);
                    else checkLocation(getLayoutX(), getLayoutY(), width, originHeight, false);
                }
                //               +
                //
                //          +
                else if(dragType == Cursor.NE_RESIZE){
                    double width = e.getX() + shiftXFromEnd;
                    double newY = getLayoutY() + e.getY() - shiftY;
                    double height = originHeight + (originY - newY);

                    if(doKeepRatio(e)){
                        double requestedRatio = width / height;
                        if(requestedRatio >= ratio){
                            height = width / ratio;
                            newY = originHeight + originY - height;
                        }
                        else width = height * ratio;
                    }


                    if(width < 0) setupMousePressVars(e, Cursor.NW_RESIZE);
                    if(height < 0) setupMousePressVars(e, Cursor.SE_RESIZE);
                    else checkLocation(getLayoutX(), newY, width, height, false);

                }else if(dragType == Cursor.SW_RESIZE){
                    double height = e.getY() + shiftYFromEnd;
                    double newX = getLayoutX() + e.getX() - shiftX;
                    double width = originWidth + (originX - newX);

                    if(width < 0) setupMousePressVars(e, Cursor.SE_RESIZE);
                    if(height < 0) setupMousePressVars(e, Cursor.NW_RESIZE);
                    else checkLocation(newX, getLayoutY(), width, height, false);

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
                    if(height < 0) setupMousePressVars(e, Cursor.SW_RESIZE);
                    else checkLocation(newX, newY, width, height, false);

                }else if(dragType == Cursor.N_RESIZE){
                    double newY = getLayoutY() + e.getY() - shiftY;
                    double height = originHeight + (originY - newY);

                    if(height < 0) setupMousePressVars(e, Cursor.S_RESIZE);
                    else checkLocation(getLayoutX(), newY, getWidth(), height, false);

                }else if(dragType == Cursor.W_RESIZE){
                    double newX = getLayoutX() + e.getX() - shiftX;
                    double width = originWidth + (originX - newX);

                    if(width < 0) setupMousePressVars(e, Cursor.E_RESIZE);
                    else checkLocation(newX, getLayoutY(), width, getHeight(), false);

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
    public void doubleClick(){
    
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

    public boolean doKeepRatio(MouseEvent e){
        if(e == null){
            return getRepeatMode() == RepeatMode.KEEP_RATIO;
        }else{
            if(getRepeatMode() == RepeatMode.KEEP_RATIO) return !e.isShiftDown();
            else return e.isShiftDown();
        }
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
    
    public RotateMode getRotateMode(){
        return rotateMode.get();
    }
    
    public ObjectProperty<RotateMode> rotateModeProperty(){
        return rotateMode;
    }
    
    public void setRotateMode(RotateMode rotateMode){
        this.rotateMode.set(rotateMode);
    }
}
