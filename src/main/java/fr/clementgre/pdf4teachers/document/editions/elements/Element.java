/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.MoveUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Element extends Region {
    
    public static BorderStroke STROKE_DEFAULT = new BorderStroke(Color.color(0 / 255.0, 100 / 255.0, 255 / 255.0),
            BorderStrokeStyle.DOTTED, CornerRadii.EMPTY, new BorderWidths(1.5));
    
    // Size for A4 - 200dpi (Static)
    public static float GRID_WIDTH = 165400;
    public static float GRID_HEIGHT = 233900;
    public static float GRID_RATIO = GRID_WIDTH / GRID_HEIGHT;
    
    // ATTRIBUTES
    
    protected IntegerProperty realX = new SimpleIntegerProperty();
    protected IntegerProperty realY = new SimpleIntegerProperty();
    
    protected int pageNumber;
    protected double shiftX;
    protected double shiftY;
    protected boolean wasInEditPagesModeWhenMousePressed;
    
    public ContextMenu menu = new ContextMenu();
    
    public Element(int x, int y, int pageNumber){
        this.pageNumber = pageNumber;
        this.realX.set(x);
        this.realY.set(y);
    }
    
    // SETUP / EVENTS CALLBACK
    
    private final ChangeListener<Element> mainScreenSelectedListener = (observable, oldElement, newElement) -> {
        if(oldElement == this && newElement != this) onDeSelected();
        else if(oldElement != this && newElement == this) onSelected();
    };
    
    public static final String ELEMENT_CLIPBOARD_KEY = "ElementsClipboard";
    public static Element elementClipboard;
    
    boolean dragAlreadyDetected;
    
    public static boolean paste(){
        if(!MainWindow.mainScreen.hasDocument(false)) return false;
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        
        if(ELEMENT_CLIPBOARD_KEY.equals(clipboard.getContent(Main.INTERNAL_FORMAT)) && elementClipboard != null){
            if(elementClipboard.getPage() != null){
                Element element = elementClipboard.clone();
                
                PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
                element.setPage(page);
                page.addElement(element, true, UType.ELEMENT);
                element.checkLocation(page.getMouseX(), page.getMouseY(), false);
                element.centerOnCoordinatesY();
                if(element instanceof GraphicElement) element.centerOnCoordinatesX();
                element.select();
                
                return true;
            }
        }
        return false;
    }
    
    public static void copy(Element element){
        if(element instanceof GradeElement || element instanceof SkillTableElement) return;
        
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.put(Main.INTERNAL_FORMAT, ELEMENT_CLIPBOARD_KEY);
        elementClipboard = element;
        
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(clipboardContent);
    }
    protected void setupGeneral(boolean setupEvents, Node... components){
        if(components != null) getChildren().setAll(components);
        
        layoutXProperty().bind(getPage().widthProperty().multiply(realX.divide(Element.GRID_WIDTH)));
        layoutYProperty().bind(getPage().heightProperty().multiply(realY.divide(Element.GRID_HEIGHT)));
        
        checkLocation(false);
        setCursor(PlatformUtils.CURSOR_MOVE);
        
        //////////////////////////// EVENTS ///////////////////////////////////
        
        MainWindow.mainScreen.selectedProperty().addListener(mainScreenSelectedListener);
        if(setupEvents){
            
            AtomicBoolean lastClickSelected = new AtomicBoolean(false);
            setOnMousePressed(e -> {
                if(e.getButton() == MouseButton.MIDDLE) setCursor(Cursor.CLOSED_HAND);
                if(!(e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY)) return;
                
                wasInEditPagesModeWhenMousePressed = MainWindow.mainScreen.isEditPagesMode();
                if(wasInEditPagesModeWhenMousePressed) return;
                e.consume();
                dragAlreadyDetected = false;
                
                if(e.getClickCount() == 1){
                    lastClickSelected.set(MainWindow.mainScreen.getSelected() == this);
                    menu.hide();
                    select();
                    
                    if(e.getButton() == MouseButton.SECONDARY){
                        menu.show(getPage(), e.getScreenX(), e.getScreenY());
                    }else{
                        shiftX = (int) e.getX();
                        shiftY = (int) e.getY();
                    }
                    
                }
            });
            setOnMouseClicked(e -> {
                if(!(e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY)) return;
                if(MainWindow.mainScreen.isEditPagesMode()) return;
                e.consume();
                if(e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY){
                    onDoubleClick();
                    if(lastClickSelected.get()){
                        onDoubleClickAfterSelected();
                    }
                }
            });
            setOnMouseDragged(e -> {
                if(wasInEditPagesModeWhenMousePressed || e.getButton() != MouseButton.PRIMARY) return;
                
                if(!dragAlreadyDetected){
                    MainWindow.mainScreen.registerNewAction(new MoveUndoAction(UType.ELEMENT, this));
                    dragAlreadyDetected = true;
                }
                
                double itemX = getLayoutX() + e.getX() - shiftX;
                double itemY = getLayoutY() + e.getY() - shiftY;
                checkLocation(itemX, itemY, true);
            });
            setOnMouseReleased(e -> {
                setCursor(PlatformUtils.CURSOR_MOVE);
                if(wasInEditPagesModeWhenMousePressed || e.getButton() != MouseButton.PRIMARY) return;
                Edition.setUnsave("ElementMouseRelease");
                
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
            });
        }
        
        /////////////////////////////////////////////////////////////////////////
        
        setupBindings();
        setupMenu();
    }
    
    public boolean isSelected(){
        return MainWindow.mainScreen.getSelected() == this;
    }
    
    protected abstract void setupBindings();
    
    protected abstract void setupMenu();
    
    protected abstract void onMouseRelease();
    
    protected void onSelected(){
        setBorder(new Border(STROKE_DEFAULT));
    }
    
    protected void onDeSelected(){
        setBorder(null);
        menu.hide();
    }
    
    // CHECKS
    
    public void checkLocation(boolean allowSwitchPage){
        checkLocation(getLayoutX(), getLayoutY(), allowSwitchPage);
    }
    
    public void checkLocation(double itemX, double itemY, boolean allowSwitchPage){
        checkLocation(itemX, itemY, getWidth(), getHeight(), allowSwitchPage);
    }
    
    public void checkLocation(double itemX, double itemY, double width, double height, boolean allowSwitchPage){
        
        // Negative Y
        if(getPageNumber() < MainWindow.mainScreen.getGridModePagesPerRow() || !allowSwitchPage)
            if(itemY < 0) itemY = 0;
        // Positive Y
        if(getPageNumber() >= MainWindow.mainScreen.document.numberOfPages - MainWindow.mainScreen.getGridModePagesInLastRow() || !allowSwitchPage)
            if(itemY > getPage().getHeight() - height) itemY = getPage().getHeight() - height;
        
        // Negative X
        if(getPageNumber() % MainWindow.mainScreen.getGridModePagesPerRow() == 0 || !allowSwitchPage)
            if(itemX < 0) itemX = 0;
        // Positive X
        if((getPageNumber() + 1) % MainWindow.mainScreen.getGridModePagesPerRow() == 0 || !allowSwitchPage)
            if(itemX > getPage().getWidth() - width) itemX = getPage().getWidth() - width;
        
        realX.set(getPage().toGridX(itemX));
        realY.set(getPage().toGridY(itemY));
        
        if(this instanceof GraphicElement graphicElement){
            
            if(getHeight() != height){
                int value = getPage().toGridY(height);
                graphicElement.setRealHeight(MathUtils.clamp(value, 0, (int) Element.GRID_HEIGHT));
            }
            
            if(getWidth() != width){
                int value = getPage().toGridX(width);
                graphicElement.setRealWidth(MathUtils.clamp(value, 0, (int) Element.GRID_WIDTH));
            }
        }
    }
    
    // ACTIONS
    
    public abstract void select();
    
    public abstract void onDoubleClickAfterSelected();
    
    public abstract void onDoubleClick();
    
    protected void selectPartial(){
        MainWindow.mainScreen.setSelected(this);
        toFront();
        getPage().toFront();
    }
    
    public abstract void addedToDocument(boolean markAsUnsave);
    
    public void removedFromDocument(boolean markAsUnsave){
        // Useless because bound values are stored in a weak reference.
        // But if this element leaks, the PageRenderer will leak too if we do not unbind this.
        layoutXProperty().unbind();
        layoutYProperty().unbind();
        MainWindow.mainScreen.selectedProperty().removeListener(mainScreenSelectedListener);
    }
    
    // Called when element was restored (With Undo/Redo system)
    public void restoredToDocument(){
        layoutXProperty().bind(getPage().widthProperty().multiply(realX.divide(Element.GRID_WIDTH)));
        layoutYProperty().bind(getPage().heightProperty().multiply(realY.divide(Element.GRID_HEIGHT)));
        MainWindow.mainScreen.selectedProperty().addListener(mainScreenSelectedListener);
    }
    
    public void delete(boolean markAsUnsave, UType undoType){
        if(getPage() != null){
            if(equals(MainWindow.mainScreen.getSelected())) MainWindow.mainScreen.setSelected(null);
            getPage().removeElement(this, markAsUnsave, undoType);
        }
    }
    
    public void switchPage(int page){
        getPage().switchElementPage(this, MainWindow.mainScreen.document.getPage(page));
        layoutXProperty().bind(getPage().widthProperty().multiply(realX.divide(Element.GRID_WIDTH)));
        layoutYProperty().bind(getPage().heightProperty().multiply(realY.divide(Element.GRID_HEIGHT)));
    }
    
    public void centerOnCoordinatesY(){
        setRealY(getRealY() - getRealHeight() / 2);
    }
    
    public void centerOnCoordinatesX(){
        setRealX(getRealX() - getRealWidth() / 2);
    }
    
    // READER AND WRITERS
    
    public abstract LinkedHashMap<Object, Object> getYAMLData();
    
    protected LinkedHashMap<Object, Object> getYAMLPartialData(){
        LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
        data.put("x", getRealX());
        data.put("y", getRealY());
        return data;
    }
    
    // GETTERS AND SETTERS
    
    public abstract String getElementName(boolean plural);
    
    public abstract float getBoundsHeight();
    
    public int getRealHeight(){
        return getPage().toGridY(getBoundsHeight());
    }
    
    public int getRealWidth(){
        return getPage().toGridY(getWidth());
    }
    
    // COORDINATES GETTERS AND SETTERS
    
    public int getRealX(){
        return realX.get();
    }
    
    public IntegerProperty realXProperty(){
        return realX;
    }
    
    public void setRealX(int x){
        realX.set(x);
    }
    
    public int getRealY(){
        return realY.get();
    }
    
    public IntegerProperty realYProperty(){
        return realY;
    }
    
    public void setRealY(int y){
        realY.set(y);
    }
    
    // compare pages and coordinates
    public int compareTo(Element element){
        if(getPageNumber() != element.getPageNumber()){
            return getPageNumber() - element.getPageNumber();
        }
        return getRealY() - element.getRealY();
    }
    
    // PAGE GETTERS AND SETTERS
    
    public PageRenderer getPage(){
        if(MainWindow.mainScreen.document == null) return null;
        if(MainWindow.mainScreen.document.getPagesNumber() > pageNumber){
            return MainWindow.mainScreen.document.getPage(pageNumber);
        }
        return null;
    }
    
    public int getPageNumber(){
        return pageNumber;
    }
    
    public void setPage(PageRenderer page){
        pageNumber = page.getPage();
    }
    
    public void setPage(int pageNumber){
        this.pageNumber = pageNumber;
    }
    
    public boolean isInPageRange(int minPage, int maxPage){
        return getPageNumber() >= minPage && getPageNumber() <= maxPage;
    }
    
    // TRANSFORMATIONS
    
    @Override
    public abstract Element clone();
    public abstract Element cloneHeadless(); // hasPage = false
    public void cloneOnDocument(){
        Element element = clone();
        element.setRealX((int) (getRealX() + (10 / getPage().getWidth() * GRID_WIDTH)));
        element.setRealY((int) (getRealY() + (10 / getPage().getHeight() * GRID_HEIGHT)));
        element.getPage().addElement(element, true, UType.ELEMENT);
        element.select();
    }
    
    public abstract void size(double scale);
}
