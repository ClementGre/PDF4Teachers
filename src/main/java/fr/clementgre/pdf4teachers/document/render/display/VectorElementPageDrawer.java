package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class VectorElementPageDrawer extends Pane{
    
    public static int LINE_MIN_LENGTH = 2;
    public static double MERGE_LINE_MAX_ANGLE = Math.toRadians(10);
    
    private PageRenderer page;
    
    private VectorElement vector;
    private final SVGPath svgPath = new SVGPath();
    
    public VectorElementPageDrawer(PageRenderer page){
        this.page = page;
        setup();
        updateVisibility();
    }
    
    private double lastClickX = 0;
    private double lastClickY = 0;
    private double lastX = 0;
    private double lastY = 0;
    private double lastLineAngle = 0;
    private boolean hasToMove = true;
    private boolean lastLineMode = isPerpendicularLineMode();
    private boolean spaceDown = false;
    private void setup(){
        
        // UI
        setBorder(new Border(new BorderStroke(Color.color(255 / 255.0, 50 / 255.0, 50 / 255.0),
                BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, new BorderWidths(1.5))));
        setCursor(Cursor.CROSSHAIR);
        
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
        
        // Bindings / Listeners
        prefWidthProperty().bind(page.widthProperty());
        prefHeightProperty().bind(page.heightProperty());
        svgPath.strokeWidthProperty().addListener((observable, oldValue, newValue) -> {
            // Update dimensions
            double shiftX = svgPath.getLayoutX() - getVectorShiftX();
            vector.setRealX(page.toGridX(vector.getLayoutX() + shiftX));
    
            double shiftY = svgPath.getLayoutY() - getVectorShiftY();
            vector.setRealY(page.toGridY(vector.getLayoutY() + shiftY));
    
            vector.setRealWidth(page.toGridX(getCurrentWidth()));
            vector.setRealHeight(page.toGridY(getCurrentHeight()));
    
            svgPath.setLayoutX(getVectorShiftX());
            svgPath.setLayoutY(getVectorShiftY());
            
        });
        
        // Events
        setOnMousePressed((e) -> {
            if(vector == null) return;
            if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1){
                e.consume();
                // Draw only one point
                initSegment(e.getX(), e.getY());
            }else if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2){
                // Cancel point
                removeLastAction("M");
            }
        });
        setOnMouseReleased((e) -> {
            if(vector == null) return;
            if(e.getClickCount() == 1 && e.getButton() == MouseButton.PRIMARY){
                e.consume();
                if((lastX != e.getX() && lastY != e.getY()) || hasToMove)
                    appendPoint(e.getX(), e.getY(), true);
            }
        });
        /*addEventHandler(MouseDragEvent.DRAG_DETECTED, (e) -> {
            initSegment(e.getX(), e.getY());
        });*/
        setOnMouseDragged((e) -> {
            if(vector == null || PageRenderer.isEditPagesMode()) return;
            
            e.consume();
            if(e.getButton() == MouseButton.PRIMARY) appendPoint(e.getX(), e.getY(), false);
        });
        setOnMouseMoved((e) -> {
            if(vector == null || PageRenderer.isEditPagesMode()) return;
            
            if(spaceDown) appendPoint(e.getX(), e.getY(), false);
            else{
                lastX = e.getX();
                lastY = e.getY();
            }
        });
    
        setOnKeyPressed((e) -> {
            
            if(e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER){
                e.consume();
                if(vector != null) vector.quitEditMode();
            }else if(e.getCode() == KeyCode.BACK_SPACE){
                e.consume();
                if(vector != null) vector.undoLastAction();
            }else if(e.getCode() == KeyCode.DELETE){
                e.consume();
                if(vector != null) vector.undoLastLines();
            }else if(e.getCode() == KeyCode.L){
                e.consume();
                MainWindow.paintTab.vectorStraightLineMode.setSelected(true);
                requestFocus();
            }else if(e.getCode() == KeyCode.P){
                e.consume();
                MainWindow.paintTab.vectorPerpendicularLineMode.setSelected(true);
                requestFocus();
            }else if(e.getCode() == KeyCode.M){
                e.consume();
                MainWindow.paintTab.vectorPerpendicularLineMode.setSelected(!MainWindow.paintTab.vectorPerpendicularLineMode.isSelected());
                requestFocus();
            }else if(e.getCode() == KeyCode.SHIFT){
                e.consume();
                MainWindow.paintTab.vectorStraightLineMode.setSelected(!MainWindow.paintTab.vectorStraightLineMode.isSelected());
                requestFocus();
            }else if(e.getCode() == KeyCode.SPACE){
                e.consume();
                if(!spaceDown){
                    spaceDown = true;
                    initSegment(lastX, lastY);
                }
            }
        });
        setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.L){
                e.consume();
                MainWindow.paintTab.vectorStraightLineMode.setSelected(false);
                requestFocus();
            }else if(e.getCode() == KeyCode.P){
                e.consume();
                MainWindow.paintTab.vectorPerpendicularLineMode.setSelected(false);
                requestFocus();
            }else if(e.getCode() == KeyCode.SPACE){
                e.consume();
                spaceDown = false;
            }
        });
    }
    
    private void initSegment(double x, double y){
        requestFocus();
        hasToMove = true;
        lastClickX = lastX = x;
        lastClickY = lastY = y;
        lastLineMode = isPerpendicularLineMode();
    }
    private void appendPoint(double x, double y, boolean force){
        if(hasToMove){
            moveTo(lastX, lastY);
            hasToMove = false;
        }
        lineTo(x, y, force);
    }
    
    public void delete(){
        this.page = null;
    
        prefWidthProperty().unbind();
        prefHeightProperty().unbind();
    }
    
    public void updateVisibility(){
        if(!page.isVisible() && isEditMode()){
            quitEditMode();
        }
    }
    
    public void enterEditMode(VectorElement vector){
        if(isEditMode()) quitEditMode();
        
        page.getChildren().add(this);
        toFront();
        requestFocus();
        
        this.vector = vector;
        vector.setVisible(false);
        vector.formatNoScaledSvgPathToPage();
    
        svgPath.setLayoutX(getVectorShiftX());
        svgPath.setLayoutY(getVectorShiftY());
    
        svgPath.contentProperty().bind(vector.getNoScaledSvgPath().contentProperty());
        svgPath.fillProperty().bind(vector.getSvgPath().fillProperty());
        svgPath.strokeProperty().bind(vector.getSvgPath().strokeProperty());
        svgPath.strokeWidthProperty().bind(vector.getSvgPath().strokeWidthProperty());
        svgPath.strokeMiterLimitProperty().bind(vector.getSvgPath().strokeMiterLimitProperty());
        
        getChildren().setAll(svgPath);
        
        lastX = lastY = lastClickX = lastClickY = lastLineAngle = 0;
        hasToMove = true; // Anyway, we will need to move. This can also prevent some bugs where the first move is missing.
        spaceDown = false;
        lastLineMode = isPerpendicularLineMode();
    }
    public void quitEditMode(){
        getChildren().clear();
        page.getChildren().remove(this);
        
        if(vector != null){
            vector.setVisible(true);
            svgPath.contentProperty().unbind();
    
            vector.setRealX(page.toGridX(getCurrentShiftX()));
            vector.setRealY(page.toGridY(getCurrentShiftY()));
            vector.setRealWidth(page.toGridX(getCurrentWidth()));
            vector.setRealHeight(page.toGridY(getCurrentHeight()));
            
        }else{
            svgPath.contentProperty().unbind();
        }
    
        
        svgPath.fillProperty().unbind();
        svgPath.strokeProperty().unbind();
        svgPath.strokeWidthProperty().unbind();
        svgPath.strokeMiterLimitProperty().unbind();
        
        this.vector = null;
    }
    
    private String lastAction = "";
    public void moveTo(double x, double y){
        x = checkX(x); y = checkY(y);
        
        if(lastAction.equalsIgnoreCase("M")){
            removeLastAction("M");
        }
        appendAction("M", x, y);
    }
    public void lineTo(double x, double y, boolean force){
        double moveX = x - lastX;
        double moveY = y - lastY;
        
        if(isStraightLineMode() || isPerpendicularLineMode()){
            
            if(!lastLineMode){ // Enter straight line mode
                lastLineMode = true;
                lastClickX = lastX = x;
                lastClickY = lastY = y;
                appendAction("L", checkX(x), checkY(y));
                appendAction("L", checkX(x), checkY(y)); // Needs two times because the last one will be edited
                return;
            }
            
            if(isPerpendicularLineMode()){
                
                if(Math.abs(x - lastClickX) >= Math.abs(y - lastClickY)) y = lastClickY;
                else x = lastClickX;
                if(lastAction.equalsIgnoreCase("L")){
                    removeLastAction("L");
                }
                
            }else{ // straightLineMode
                if(lastAction.equalsIgnoreCase("L")){
                    removeLastAction("L");
                }
            }
        }else{ // Basic drawing
    
            // Do the line only if it has a min length
            if(!force){
                double dist = Math.sqrt(Math.pow(moveX, 2) + Math.pow(moveY, 2));
                if(dist < LINE_MIN_LENGTH) return;
            }
            
            if(lastLineMode){ // exit straight line mode
                lastLineMode = false;
                lastClickX = lastX = x;
                lastClickY = lastY =  y;
                appendAction("L", checkX(x), checkY(y));
                return;
            }
            // Merge with last line if there is a small angle
            if(lastAction.equalsIgnoreCase("L")){
                double angle = -Math.atan(moveX == 0 ? 0 : (moveY / moveX));
                if(moveX < 0) angle += Math.PI;
        
                if(Math.abs(angle - lastLineAngle) < MERGE_LINE_MAX_ANGLE) removeLastAction("L");
                else lastLineAngle = angle;
            }
            
        }
    
        lastX = x; lastY = y;
        x = checkX(x); y = checkY(y);
        appendAction("L", x, y);
    }
    public double checkX(double x){
        x = StringUtils.clamp(x, vector.getSVGPadding(), page.getWidth()-vector.getSVGPadding());
        return x - getVectorShiftX();
    }
    public double checkY(double y){
        y = StringUtils.clamp(y, vector.getSVGPadding(), page.getHeight()-vector.getSVGPadding());
        return y - getVectorShiftY();
    }
    
    public void onCreateCurve(){
    
    }
    public void removeLastAction(String name){
        vector.setPath(StringUtils.removeAfterLastRegexIgnoringCase(vector.getPath(), name));
    }
    public void removeLastAction(String[] names){
        vector.setPath(StringUtils.removeAfterLastRegexIgnoringCase(vector.getPath(), names));
    }
    public void appendAction(String name, double x, double y){
        lastAction = name;
        vector.setPath(vector.getPath()
                + name
                + Main.oneDigENFormat.format(x)
                + " "
                + Main.oneDigENFormat.format(y));
    }
    
    public double getVectorShiftX(){
        return vector.getLayoutX() + vector.getSVGPadding();
    }
    public double getVectorShiftY(){
        return vector.getLayoutY() + vector.getSVGPadding();
    }
    public double getCurrentShiftX(){
        return svgPath.getLayoutX() + svgPath.getLayoutBounds().getMinX() + .5;
    }
    public double getCurrentShiftY(){
        return svgPath.getLayoutY() + svgPath.getLayoutBounds().getMinY() + .5;
    }
    public double getCurrentWidth(){
        return svgPath.getLayoutBounds().getWidth() - 1;
    }
    public double getCurrentHeight(){
        return svgPath.getLayoutBounds().getHeight() - 1;
    }
    
    public boolean isEditMode(){
        return page.getChildren().contains(this);
    }
    private boolean isStraightLineMode(){
        return MainWindow.paintTab.vectorStraightLineMode.isSelected();
    }
    private boolean isPerpendicularLineMode(){
        return MainWindow.paintTab.vectorPerpendicularLineMode.isSelected();
    }
    
    public VectorElement getVectorElement(){
        return vector;
    }
}
