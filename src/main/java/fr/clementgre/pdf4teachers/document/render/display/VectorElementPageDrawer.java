package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

public class VectorElementPageDrawer extends Pane{
    
    public static int LINE_MIN_LENGTH = 3;
    
    private ContextMenu menu = new ContextMenu();
    private PageRenderer page;
    
    private VectorElement vector;
    private SVGPath svgPath = new SVGPath();
    
    public VectorElementPageDrawer(PageRenderer page){
        this.page = page;
        setup();
        updateVisibility();
    }
    
    double lastX = 0;
    double lastY = 0;
    private void setup(){
        
        // UI
        setBorder(new Border(new BorderStroke(Color.color(255 / 255.0, 50 / 255.0, 50 / 255.0),
                BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, new BorderWidths(1.5))));
        
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
        
        // MENU
        NodeMenuItem quitEditMode = new NodeMenuItem(TR.tr("actions.quitEditMode"), false);
        NodeMenuItem undo = new NodeMenuItem(TR.tr("paintTab.vectorElements.undo"), false);
        
        quitEditMode.setOnAction((e) -> {
            quitEditMode();
        });
        undo.setOnAction((e) -> {
            MainWindow.paintTab.vectorUndoPath.fire();
        });
        
        menu.getItems().addAll(quitEditMode, undo);
        
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
            
            if(isPointMode()){
            
            }else if(e.getButton() == MouseButton.PRIMARY){
                e.consume();
                moveTo(e.getX(), e.getY());
            }
        });
        setOnMouseReleased((e) -> {
            e.consume();
            
        });
        
        setOnMouseDragged((e) -> {
            e.consume();
            if(isPointMode()){
        
            }else{
                double dist = Math.sqrt(Math.pow(e.getX() - lastX, 2) + Math.pow(e.getY() - lastY, 2));
                if(dist < LINE_MIN_LENGTH) return;
                
                lastX = e.getX();
                lastY = e.getY();
                lineTo(e.getX(), e.getY());
            }
        });
    
        setOnMouseClicked((e) -> {
            e.consume();
            menu.hide();
            if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2){
                if(vector != null) vector.quitEditMode();
            }else if(e.getButton() == MouseButton.SECONDARY && e.getClickCount() == 1){
                menu.show(this, e.getScreenX(), e.getScreenY());
            }
        });
    }
    
    public void delete(){
        this.page = null;
        this.menu = null;
    
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
    
    public void moveTo(double x, double y){
        x = checkX(x); y = checkY(y);
        
        appendAction("M", x, y);
    }
    public void lineTo(double x, double y){
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
    public void undo(){
        vector.undoLastLines();
    }
    public void appendAction(String name, double x, double y){
        appendAction(name + Main.oneDigFormat.format(x) + " " + Main.oneDigFormat.format(y));
    }
    public void appendAction(String text){
        vector.setPath(vector.getPath() + text);
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
    private boolean isLineMode(){
        return MainWindow.paintTab.vectorStraightLineMode.isSelected();
    }
    
    public VectorElement getVectorElement(){
        return vector;
    }
}
