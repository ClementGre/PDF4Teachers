/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.MathUtils;
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
    
    private long lastClickTime;
    // When dynamically creating a new element to split drawing into different drawings,
    // this var should be true to not reset the vars when re-entering in draw mode.
    private boolean doSwitchElementMode;
    
    public VectorElementPageDrawer(PageRenderer page){
        this.page = page;
        setup();
        updateVisibility();
    }
    
    // Last coordinate of click AND release
    private double lastClickX;
    private double lastClickY;
    // Last coordinate of the mouse (onMove and not drag), of the last action or of the last initialized segment (initSegment())
    // It is used mainly by initSegment() (and moveTo()/lineTo()) to add a M (move) instruction when the cursor has moved
    private double lastX;
    private double lastY;
    private double lastLineAngle;
    // Is true when the next action will have to move to (lastX, lastY) before being added
    private boolean hasToMove = true;
    private boolean lastLineMode = isPerpendicularLineMode();
    private boolean spaceDown;
    private void setup(){
        
        // UI
        setBorder(new Border(new BorderStroke(Color.color(255 / 255.0, 50 / 255.0, 50 / 255.0),
                BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, new BorderWidths(1.5))));
        
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
            if(vector == null || MainWindow.mainScreen.isEditPagesMode()) return;
    
    
            if(e.getButton() == MouseButton.PRIMARY){
                if(e.getClickCount() == 1){
                    e.consume();
                    // Draw only one point
                    initSegment(e.getX(), e.getY());
                }else if(e.getClickCount() == 2){
                    // Cancel point
                    removeLastAction("M");
                }
            }else if(e.getButton() == MouseButton.SECONDARY){
                setCursor(Cursor.DEFAULT);
            }else if(e.getButton() == MouseButton.MIDDLE){
                e.consume();
                MainWindow.mainScreen.initDragOrigin(e);
            }
        });
        setOnMouseReleased((e) -> {
            if(vector == null || MainWindow.mainScreen.isEditPagesMode()) return;
            
            if(e.getClickCount() == 1 && e.getButton() == MouseButton.PRIMARY){
                e.consume();
                if((lastX != e.getX() && lastY != e.getY()) || hasToMove)
                    appendPoint(e.getX(), e.getY(), true);
            }
            lastClickTime = System.currentTimeMillis();
            lastClickX = e.getX();
            lastClickY = e.getY();
            setCursor(Cursor.CROSSHAIR);
        });
        setOnDragDetected(e -> {
            if(e.getButton() == MouseButton.MIDDLE) setCursor(Cursor.CLOSED_HAND);
        });
        setOnMouseDragged((e) -> {
            if(vector == null || MainWindow.mainScreen.isEditPagesMode()) return;
    
            if(e.getButton() == MouseButton.PRIMARY){
                e.consume();
                appendPoint(e.getX(), e.getY(), false);
            }else if(e.getButton() == MouseButton.MIDDLE){
                e.consume();
                MainWindow.mainScreen.onDragForScroll(e);
            }
        });
        setOnMouseMoved((e) -> {
            if(vector == null || MainWindow.mainScreen.isEditPagesMode()) return;
            
            if(spaceDown){
                if(lastX == 0 && lastY == 0){
                    lastX = e.getX();
                    lastY = e.getY();
                }
                appendPoint(e.getX(), e.getY(), false);
            }else{
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
    
        if(doSplitElement(x, y)){ // Create new element
            quitEditMode();
            doSwitchElementMode = true;
            MainWindow.paintTab.newVectorDrawing(false);
        }
    
        hasToMove = true;
        lastClickX = lastX = x;
        lastClickY = lastY = y;
        lastLineMode = isPerpendicularLineMode();
    }
    private boolean doSplitElement(double x, double y){
        if(Main.settings.drawingMaxTime.getValue() != -1){
            if(System.currentTimeMillis() - lastClickTime > Main.settings.drawingMaxTime.getValue()*1000) return true;
        }
        if(Main.settings.drawingMaxDistance.getValue() != -1){
            if(Math.sqrt(Math.pow(lastClickX - x, 2) + Math.pow(lastClickY - y, 2)) > Main.settings.drawingMaxDistance.getValue()/100d * PageRenderer.PAGE_WIDTH && !hasToMove) return true;
        }
        if(Main.settings.drawingMaxLength.getValue() != -1){
            return vector.getPath().length() > Main.settings.drawingMaxLength.getValue();
        }
        return false;
    }
    
    private void appendPoint(double x, double y, boolean force){
        if(hasToMove){
            moveTo(lastX, lastY);
            hasToMove = false;
        }
        lineTo(x, y, force);
    }
    
    public void delete(){
        page = null;
    
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
    
        setCursor(Cursor.CROSSHAIR);
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
        
        if(!doSwitchElementMode) lastX = lastY = lastClickX = lastClickY = lastLineAngle = 0;
        hasToMove = true; // Anyway, we will need to move. This can also prevent some bugs where the first move is missing.
        if(!doSwitchElementMode) spaceDown = false;
        lastLineMode = isPerpendicularLineMode();
        
        lastClickTime = Long.MAX_VALUE;
        doSwitchElementMode = false;
    }
    public void quitEditMode(){
        lastClickTime = Long.MAX_VALUE;
        
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
        
        vector = null;
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
            }
            if(lastAction.equalsIgnoreCase("L")){
                removeLastAction("L");
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
                lastClickY = lastY = y;
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
        x = MathUtils.clamp(x, vector.getSVGPadding(), page.getWidth()-vector.getSVGPadding());
        return x - getVectorShiftX();
    }
    public double checkY(double y){
        y = MathUtils.clamp(y, vector.getSVGPadding(), page.getHeight()-vector.getSVGPadding());
        return y - getVectorShiftY();
    }
    
    public void removeLastAction(String name){
        vector.setPath(StringUtils.removeAfterLastOccurrenceIgnoringCase(vector.getPath(), name));
    }
    public void removeLastAction(String[] names){
        vector.setPath(StringUtils.removeAfterLastOccurrenceIgnoringCase(vector.getPath(), names));
    }
    public void appendAction(String name, double x, double y){
        lastAction = name;
        vector.setPath(vector.getPath()
                + name
                + Main.oneDigENFormat.format(x)
                + " "
                + Main.oneDigENFormat.format(y));
    }
    
    // The origin of the vector element is also the origin of the display svg.
    // A translation is applied to put coordinates on the good empty (page -> element).
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
