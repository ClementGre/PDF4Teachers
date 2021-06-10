package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.scene.Cursor;
import javafx.scene.input.*;
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
    private SVGPath svgPath = new SVGPath();
    
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
    private boolean lastLineMode = isStraightLineMode();
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
            if(e.getClickCount() == 1){
                e.consume();
                requestFocus();
                hasToMove = true;
                lastClickX = lastX = e.getX();
                lastClickY = lastY = e.getY();
                lastLineMode = isStraightLineMode();
            }
        });
        setOnMouseReleased((e) -> {
            if(vector == null) return;
            if(e.getClickCount() == 1){
                e.consume();
                if(hasToMove) return;
                lineTo(e.getX(), e.getY(), true);
            }
        });
        
        setOnMouseDragged((e) -> {
            if(vector == null || PageRenderer.isEditPagesMode()) return;
            
            if(isPointMode()){
        
            }else{
                if(hasToMove){
                    moveTo(lastX, lastY);
                    hasToMove = false;
                }
                lineTo(e.getX(), e.getY(), false);
            }
        });
    
        setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER){
                e.consume();
                if(vector != null) vector.quitEditMode();
            }else if(e.getCode() == KeyCode.DELETE || e.getCode() == KeyCode.BACK_SPACE){
                e.consume();
                if(vector != null) undo();
            }else if(e.getCode() == KeyCode.SHIFT){
                e.consume();
                MainWindow.paintTab.vectorDrawMode.getToggles().forEach((t) -> t.setSelected(!t.isSelected()));
                requestFocus();
            }else if(e.getCode() == KeyCode.L){
                e.consume();
                MainWindow.paintTab.vectorStraightLineMode.setSelected(true);
                requestFocus();
            }else if(e.getCode() == KeyCode.C){
                e.consume();
                onCreateCurve();
            }
        });
        setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.L){
                e.consume();
                MainWindow.paintTab.vectorStraightLineMode.setSelected(false);
                requestFocus();
            }
        });
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
        lastLineMode = isStraightLineMode();
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
    
        // Do the line only if it has a min length
        if(!force){
            double dist = Math.sqrt(Math.pow(moveX, 2) + Math.pow(moveY, 2));
            if(dist < LINE_MIN_LENGTH) return;
        }
        
        if(isStraightLineMode()){
            if(!lastLineMode){ // Enter line mode
                lastLineMode = true;
                lastClickX = lastX = x;
                lastClickY = lastY = y;
                appendAction("L", checkX(x), checkY(y));
                appendAction("L", checkX(x), checkY(y)); // Needs two times because the last one will be edited
                return;
            }
            if(Math.abs(x - lastClickX) >= Math.abs(y - lastClickY)) y = lastClickY;
            else x = lastClickX;
            if(lastAction.equalsIgnoreCase("L")){
                removeLastAction("L");
            }
        }else{
            if(lastLineMode){ // exit line mode
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
                + Main.oneDigFormat.format(x)
                + " "
                + Main.oneDigFormat.format(y));
    }
    
    public void undo(){
        vector.undoLastLines();
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
    private boolean isPointMode(){
        return MainWindow.paintTab.vectorModePoint.isSelected();
    }
    private boolean isStraightLineMode(){
        return MainWindow.paintTab.vectorStraightLineMode.isSelected();
    }
    
    public VectorElement getVectorElement(){
        return vector;
    }
}
