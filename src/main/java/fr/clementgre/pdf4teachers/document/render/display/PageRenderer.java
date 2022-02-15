/*
 * Copyright (c) 2019-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.*;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.CreateDeleteUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.pages.PageMoveUndoAction;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.gallery.GalleryManager;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageListPane;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorListPane;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTab;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeView;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageRenderer extends Pane {
    
    public static final int PAGE_WIDTH = 596;
    public static final int PAGE_MARGIN = 30;
    public static final int PAGE_MARGIN_GRID = 60;
    
    public static final KeyCombination KEY_COMB_ALL = new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN);
    public static Background WHITE_BACKGROUND = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
    
    private PageStatus status = PageStatus.HIDE;
    
    public enum PageGridPosition {
        LEFT,
        MIDDLE,
        RIGHT
    }
    private PageGridPosition pageGridPosition = PageGridPosition.MIDDLE;
    private int pageRowIndex = 0;
    
    private int page;
    private ArrayList<Element> elements = new ArrayList<>();
    private double mouseX = 0;
    private double mouseY = 0;
    
    private ProgressBar loader = new ProgressBar();
    private ContextMenu menu = new ContextMenu();
    
    private int lastShowStatus = 1; // default status is hide (1)
    private double renderedZoomFactor;
    
    private PageEditPane pageEditPane;
    private PageZoneSelector pageZoneSelector;
    private VectorElementPageDrawer vectorElementPageDrawer;
    private PageGridSeparator pageGridSeparator;
    private PageGridSeparator pageGridSeparatorBefore;
    private PageGridEditPane pageGridEditPane;
    private PageGridNumber pageGridNumber;
    
    private GraphicElement placingElement = null;
    
    private double shiftY = 0;
    private double shiftX = 0;
    private double defaultTranslateY = 0;
    private double defaultTranslateX = 0;
    
    private boolean removed = false;
    
    private final InvalidationListener translateYListener = e -> updateShowStatus();
    
    private final InvalidationListener mainScreenZoomListener = (observable) -> {
        if(MainWindow.mainScreen.isIsGridMode()) setCursor(PlatformUtils.CURSOR_MOVE);
        else if(MainWindow.mainScreen.hasToPlace()) setCursor(Cursor.CROSSHAIR);
        else setCursor(Cursor.DEFAULT);
        
        if(pageEditPane != null) pageEditPane.updatePosition();
        if(pageGridNumber != null) pageGridNumber.updateZoom();
        if(pageGridEditPane != null) pageGridEditPane.updateZoom();
        if(pageGridSeparator != null) pageGridSeparator.updateZoom();
        if(pageGridSeparatorBefore != null) pageGridSeparatorBefore.updateZoom();
    };
    
    public PageRenderer(int page){
        this.page = page;
        setup();
    }
    
    private void setup(){
        setBackground(WHITE_BACKGROUND);
        
        // LOADER
        loader.setPrefWidth(300);
        loader.setPrefHeight(20);
        loader.translateXProperty().bind(widthProperty().divide(2).subtract(loader.widthProperty().divide(2)));
        loader.translateYProperty().bind(heightProperty().divide(2).subtract(loader.heightProperty().divide(2)));
        loader.setVisible(false);
        getChildren().add(loader);
        
        // BORDER
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        setEffect(ds);
        
        translateYProperty().addListener(translateYListener);
        
        //////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// ZOOM //////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        MainWindow.mainScreen.zoomProperty().addListener(mainScreenZoomListener);
        
        //////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////// DRAGGING ////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setOnDragDetected(e -> {
            if(MainWindow.mainScreen.isIsGridMode() && !isPageZoneSelectorActive()) MainWindow.mainScreen.registerNewPageAction(new PageMoveUndoAction(UType.UNDO, this, getPage()));
        });
        setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
            
            if(MainWindow.mainScreen.isIsGridMode() && !isPageZoneSelectorActive()){
                
                double translateY = getTranslateY() + e.getY() - shiftY;
                if(getPage() < MainWindow.mainScreen.getGridModePagesPerRow()) translateY = Math.max(translateY, defaultTranslateY);
                if(getPage() >= MainWindow.mainScreen.document.totalPages - MainWindow.mainScreen.getGridModePagesInLastRow()) translateY = Math.min(translateY, defaultTranslateY);
                setTranslateY(translateY);
                translateYTimeline.stop();
    
                double translateX = getTranslateX() + e.getX() - shiftX;
                if(pageGridPosition == PageGridPosition.LEFT) translateX = Math.max(translateX, defaultTranslateX);
                if(pageGridPosition == PageGridPosition.RIGHT) translateX = Math.min(translateX, defaultTranslateX);
                setTranslateX(translateX);
                translateXTimeline.stop();
                
                // Vertical : pass the number of pages in the row
                if(getTranslateY() - defaultTranslateY > getHeight() * .75 && getPage() != MainWindow.mainScreen.document.totalPages - 1){
                    MainWindow.mainScreen.document.pdfPagesRender.editor.movePage(this, StringUtils.clamp(MainWindow.mainScreen.getGridModePagesPerRow(), 1, MainWindow.mainScreen.document.totalPages - 1 - getPage()));
                }else if(getTranslateY() - defaultTranslateY < -getHeight() * .75 && getPage() != 0){
                    MainWindow.mainScreen.document.pdfPagesRender.editor.movePage(this, StringUtils.clamp(-MainWindow.mainScreen.getGridModePagesPerRow(), -getPage(), -1));
                }
                // Horizontal
                if(getTranslateX() - defaultTranslateX > getWidth() * .75 && getPage() != MainWindow.mainScreen.document.totalPages - 1){
                    MainWindow.mainScreen.document.pdfPagesRender.editor.movePage(this, 1);
                }else if(getTranslateX() - defaultTranslateX < -getWidth() * .75 && getPage() != 0){
                    MainWindow.mainScreen.document.pdfPagesRender.editor.movePage(this, -1);
                }
                
            }else{
                if(placingElement != null){
                    e.consume();
                    placingElement.simulateDragToResize(e.getX() - placingElement.getLayoutX(), e.getY() - placingElement.getLayoutY(), e.isShiftDown());
                }
            }
            
        });
        
        //////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////// DRAG'N DROP ///////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setOnDragEntered(e -> {
            final Dragboard dragboard = e.getDragboard();
            if(TextTab.TEXT_TREE_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Drag TextElement
                if(TextTab.draggingItem != null){
                    e.acceptTransferModes(TransferMode.ANY);
                    e.consume();
                    if(!Main.isLinux()) menu.hide(); // On Linux, hiding the menu makes the drag cancelled
                    
                    if(TextTab.draggingElement == null){ // Add to document
                        TextTab.draggingElement = TextTab.draggingItem.addToDocument(false, this, toGridX(e.getX()), toGridY(e.getY()), true);
                    }
                }
            }else if(PaintTab.PAINT_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Drag ImageElement
                if(PaintTab.draggingItem instanceof ImageGridElement item){
                    e.acceptTransferModes(TransferMode.ANY);
                    e.consume();
                    if(!Main.isLinux()) menu.hide(); // On Linux, hiding the menu makes the drag cancelled
                    
                    if(PaintTab.draggingElement == null){ // Add to document
                        PaintTab.draggingElement = item.addToDocument();
                        PaintTab.draggingElement.checkLocation(e.getX() - PaintTab.draggingElement.getWidth() / 2, e.getY() - PaintTab.draggingElement.getHeight() / 2, false);
                    }
                }else if(PaintTab.draggingItem instanceof VectorGridElement item){ // Drag VectorElement
                    e.acceptTransferModes(TransferMode.ANY);
                    e.consume();
                    if(!Main.isLinux()) menu.hide(); // On Linux, hiding the menu makes the drag cancelled
                    
                    if(PaintTab.draggingElement == null){ // Add to document
                        PaintTab.draggingElement = item.addToDocument(false);
                        PaintTab.draggingElement.checkLocation(e.getX() - PaintTab.draggingElement.getWidth() / 2, e.getY() - PaintTab.draggingElement.getHeight() / 2, false);
                    }
                    
                }
            }
        });
        setOnDragOver(e -> {
            final Dragboard dragboard = e.getDragboard();
            if(TextTab.TEXT_TREE_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Drag TextElement
                if(TextTab.draggingItem != null){
                    e.acceptTransferModes(TransferMode.ANY);
                    
                    if(TextTab.draggingElement != null){ // Move element
                        TextTab.draggingElement.checkLocation(e.getX(), e.getY() - TextTab.draggingElement.getHeight() / 2, false);
                    }
                }
            }else if(PaintTab.PAINT_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Drag GraphicElement
                if(PaintTab.draggingItem instanceof ImageGridElement || PaintTab.draggingItem instanceof VectorGridElement){
                    e.acceptTransferModes(TransferMode.ANY);
                    e.consume();
                    
                    if(PaintTab.draggingElement != null){ // Move element
                        PaintTab.draggingElement.checkLocation(e.getX() - PaintTab.draggingElement.getWidth() / 2, e.getY() - PaintTab.draggingElement.getHeight() / 2, false);
                    }
                }
            }else if(dragboard.hasFiles()){ // Image/SVG drag'n drop
                for(File file : dragboard.getFiles()){
                    if(GalleryManager.isAcceptableImage(file.getName())){
                        e.acceptTransferModes(TransferMode.COPY);
                    }else if(FilesUtils.getExtension(file.getName()).equalsIgnoreCase("svg")){
                        e.acceptTransferModes(TransferMode.COPY);
                    }
                }
                e.consume();
            }
        });
        setOnDragExited(e -> {
            final Dragboard dragboard = e.getDragboard();
            if(TextTab.TEXT_TREE_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Drag TextElement
                if(TextTab.draggingItem != null){
                    e.acceptTransferModes(TransferMode.ANY);
                    e.consume();
                    
                    if(TextTab.draggingElement != null){ // Remove from document
                        TextTab.draggingElement.delete(false, UType.NO_UNDO);
                        TextTab.draggingElement = null;
                    }
                }
            }else if(PaintTab.PAINT_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Drag GraphicElement
                if(PaintTab.draggingItem instanceof ImageGridElement || PaintTab.draggingItem instanceof VectorGridElement){
                    e.acceptTransferModes(TransferMode.ANY);
                    e.consume();
                    
                    if(PaintTab.draggingElement != null){ // Remove from document
                        PaintTab.draggingElement.delete(false, UType.NO_UNDO);
                        PaintTab.draggingElement = null;
                    }
                }
            }
        });
        setOnDragDropped(e -> {
            final Dragboard dragboard = e.getDragboard();
            if(TextTab.TEXT_TREE_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Set vars to null
                e.setDropCompleted(true);
                menu.hide();
                if(TextTab.draggingElement != null) TextTab.draggingElement.select();
                TextTab.draggingItem = null;
                TextTab.draggingElement = null;
            }else if(PaintTab.PAINT_ITEM_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){ // Set vars to null
                e.setDropCompleted(true);
                menu.hide();
                if(PaintTab.draggingElement != null) PaintTab.draggingElement.select();
                PaintTab.draggingItem = null;
                PaintTab.draggingElement = null;
            }else if(dragboard.hasFiles()){ // Image/SVG drag'n drop
                for(File file : dragboard.getFiles()){
                    MainWindow.mainScreen.setSelected(null);
                    GraphicElement element = null;
                    if(GalleryManager.isAcceptableImage(file.getName())){
                        element = MainWindow.paintTab.newImageElementFromPath(file.getAbsolutePath());
                    }else if(FilesUtils.getExtension(file.getName()).equalsIgnoreCase("svg")){
                        element = MainWindow.paintTab.openSVGFile(file, null);
                    }
                    if(element != null){
                        if(element.getPageNumber() != getPage()) element.switchPage(getPage());
                        element.setRealX(toGridX(e.getX()));
                        element.setRealY(toGridX(e.getY()));
                        element.centerOnCoordinatesX();
                        element.centerOnCoordinatesY();
                        element.checkLocation(element.getLayoutX(), element.getLayoutY(), false);
                    }
                }
                MainWindow.mainScreen.setSelected(null); // prevent some dimensions issues
                e.consume();
                e.setDropCompleted(true);
            }
        });
        
        //////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////// ENTER / MOVE / EXIT ///////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setOnMouseEntered(e -> {
            MainWindow.mainScreen.document.setCurrentPage(page);
            
            if(MainWindow.mainScreen.isIsGridMode()){
                setCursor(PlatformUtils.CURSOR_MOVE);
                
                if(pageGridEditPane == null) pageGridEditPane = new PageGridEditPane(this);
                pageGridEditPane.show(true);
            }else{
                if(MainWindow.mainScreen.hasToPlace()) setCursor(Cursor.CROSSHAIR);
                else setCursor(Cursor.DEFAULT);
                
                if(pageEditPane == null) pageEditPane = new PageEditPane(this);
                else pageEditPane.setVisible(true);
            }
            
        });
        // UPDATE MOUSE COORDINATES
        // Detect all mouse events, even ones of Elements because they don't consume the event.
        // (This is used to autoscroll when approaching the top/bottom of MainScreen)
        setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
    
            if(MainWindow.mainScreen.isIsGridMode()) setCursor(PlatformUtils.CURSOR_MOVE);
            else if(MainWindow.mainScreen.hasToPlace()) setCursor(Cursor.CROSSHAIR);
            else setCursor(Cursor.DEFAULT);
        });
        setOnMouseExited(e -> {
            if(pageEditPane != null) pageEditPane.checkMouseExited();
            if(pageGridEditPane != null) pageGridEditPane.hide();
        });
        
        //////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////// PRESS / RELEASE ////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setOnMousePressed(e -> {
            requestFocus();
            toFront();
            shiftY = e.getY();
            shiftX = e.getX();
            defaultTranslateY = getTranslateY();
            defaultTranslateX = getTranslateX();
            MainWindow.mainScreen.setSelected(null);
            menu.hide();
            menu.getItems().clear();
            
            if(MainWindow.mainScreen.isIsGridMode()){
                if(e.isShiftDown()) MainWindow.mainScreen.document.selectToPage(getPage());
                else if(e.isControlDown()) MainWindow.mainScreen.document.invertSelectedPage(getPage());
                else MainWindow.mainScreen.document.selectPage(getPage());
                return;
            }
            
            if(MainWindow.mainScreen.hasToPlace()){
                placingElement = MainWindow.mainScreen.getToPlace();
                
                placingElement.initializePage(getPage(), e.getX(), e.getY());
                
                if(placingElement instanceof VectorElement vectorElement && vectorElement.getResizeMode() != GraphicElement.ResizeMode.SIDE_EDGES){
                    vectorElement.invertInversions();
                }
                if(placingElement.getResizeMode() == GraphicElement.ResizeMode.SIDE_EDGES){
                    placingElement.centerOnCoordinatesY();
                }
                
                addElement(placingElement, true, UType.UNDO);
                placingElement.select();
                placingElement.incrementUsesAndLastUse();
                
                int shiftX = (int) placingElement.getLayoutX();
                int shiftY = (int) placingElement.getLayoutY();
                
                placingElement.setupMousePressVars(e.getX() - shiftX - 1, e.getY() - shiftY - 1, null, true, true);
                placingElement.simulateDragToResize(e.getX() - placingElement.getLayoutX(), e.getY() - placingElement.getLayoutY(), e.isShiftDown());
                
                Platform.runLater(() -> {
                    placingElement.select();
                });
                
                setCursor(Cursor.CROSSHAIR);
            }else{
                if(e.getButton() == MouseButton.SECONDARY) showContextMenu(e.getY(), e.getScreenX(), e.getScreenY());
                else{
                    setCursor(Cursor.CLOSED_HAND);
                    
                    if(e.getClickCount() == 2 && !MainWindow.mainScreen.isIsGridMode()){
                        e.consume();
                        SideBar.selectTab(MainWindow.textTab);
                        MainWindow.textTab.newBtn.fire();
                        Element selected = MainWindow.mainScreen.getSelected();
                        if(selected != null){
                            if(selected instanceof TextElement){
                                selected.setRealX((int) (selected.getPage().getMouseX() * Element.GRID_WIDTH / selected.getPage().getWidth()));
                            }
                        }
                    }
                    
                }
            }
        });
        setOnMouseReleased(e -> {
            if(MainWindow.mainScreen.isIsGridMode()){
                if(getTranslateY() != defaultTranslateY) animateTranslateY(defaultTranslateY);
                if(getTranslateX() != defaultTranslateX) animateTranslateX(defaultTranslateX);
            }
            
            if(placingElement != null && !MainWindow.mainScreen.isIsGridMode()){
                e.consume();
                if(placingElement.getWidth() < 10 && placingElement.getHeight() < 10
                        || (placingElement.getWidth() < 10 && placingElement.getResizeMode() == GraphicElement.ResizeMode.SIDE_EDGES)){
                    placingElement.simulateReleaseFromResize();
                    placingElement.defineSizeAuto();
                }else{
                    placingElement.simulateReleaseFromResize();
                }
                placingElement = null;
            }
            if(MainWindow.mainScreen.isIsGridMode()) setCursor(PlatformUtils.CURSOR_MOVE);
            else setCursor(Cursor.DEFAULT);
        });
        
        //////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////// ROTATE /////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setEventHandler(RotateEvent.ROTATE, e -> {
            if(isVectorEditMode()) return;
            
            double rotate = e.getTotalAngle() * 2;
            if(rotate > 85) rotate = 90;
            else if(rotate < -85) rotate = -90;
            else if(rotate > -3 && rotate < 3) rotate = 0;
            setVisibleRotate(rotate, false);
            MainScreen.isRotating = true;
        });
        setEventHandler(RotateEvent.ROTATION_FINISHED, e -> {
            MainScreen.isRotating = false;
            if(isVectorEditMode()) return;
            
            double rotate = e.getTotalAngle() * 2;
            if(rotate < -45){
                setVisibleRotate(-90, true, () -> {
                    MainWindow.mainScreen.document.pdfPagesRender.editor.rotatePage(this, false, false);
                    setRotate(0);
                });
            }else if(rotate > 45){
                setVisibleRotate(90, true, () -> {
                    MainWindow.mainScreen.document.pdfPagesRender.editor.rotatePage(this, true, false);
                    setRotate(0);
                });
            }else{
                setVisibleRotate(0, true);
            }
        });
        
    }
    public void setVisibleRotate(double rotate, boolean animated){
        setVisibleRotate(rotate, animated, null);
    }
    public void setVisibleRotate(double rotate, boolean animated, CallBack finished){
        if(rotate == getRotate()){
            if(finished != null) finished.call();
        }else if(animated){
            Timeline timeline = new Timeline(60);
            timeline.getKeyFrames().clear();
            double durationFactor = Math.abs(rotate - getRotate()) / 45;
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200 * durationFactor), new KeyValue(rotateProperty(), rotate))
            );
            timeline.play();
            if(finished != null) timeline.setOnFinished(e -> {
                timeline.stop();
                timeline.setOnFinished(null);
                finished.call();
            });
        }else{
            setRotate(rotate);
            if(finished != null) finished.call();
        }
    }
    
    public void showContextMenu(double pageY, double screenX, double screenY){
        if(MainWindow.gradeTab.treeView.getRoot().getChildren().size() != 0){
            GradeTreeView.defineNaNLocations();
            GradeTreeItem logicalNextGrade = GradeTreeView.getNextLogicGrade();
            if(logicalNextGrade != null){
                MenuItem menuItem = logicalNextGrade.getEditMenuItem(menu);
                menu.getItems().add(menuItem);
            }
            
            GradeElement documentNextGradeElement = GradeTreeView.getNextGrade(page, (int) pageY);
            GradeTreeItem documentNextGrade = documentNextGradeElement == null ? null : documentNextGradeElement.getGradeTreeItem();
            if(documentNextGrade != null && logicalNextGrade != documentNextGrade){
                MenuItem menuItem = documentNextGrade.getEditMenuItem(menu);
                menu.getItems().add(0, menuItem);
            }
            
        }
        
        List<TextTreeItem> mostUsed = TextTreeView.getMostUseElements();
        
        for(int i = 0; i < Main.settings.pagesFastMenuTextsNumber.getValue(); i++){
            if(mostUsed.size() > i){
                TextTreeItem item = mostUsed.get(i);
                
                NodeMenuItem menuItem = new NodeMenuItem(false);
                
                ScratchText name = new ScratchText(new TextWrapper(item.name.getText().replace("\n", ""), null, 175).wrapFirstLine());
                name.setTextOrigin(VPos.TOP);
                name.setFont(item.name.getFont());
                name.setFill(item.name.getFill());
                
                menuItem.setLeftData(name);
                menuItem.setOnAction((e) -> {
                    item.addToDocument(false, false);
                    MainWindow.textTab.selectItem();
                });
                menu.getItems().add(menuItem);
            }
        }
        
        NodeMenuItem vectorsMenuItem = VectorListPane.getPagesMenuItem();
        if(vectorsMenuItem != null) menu.getItems().add(vectorsMenuItem);
        
        if(Main.settings.pagesFastMenuShowImages.getValue()){
            NodeMenuItem imagesMenuItem = ImageListPane.getPagesMenuItem();
            if(imagesMenuItem != null) menu.getItems().add(imagesMenuItem);
        }
        
        
        NodeMenuItem.setupMenu(menu);
        menu.show(this, screenX, screenY);
    }
    
    public void updatePosition(int totalHeight, boolean animated){
        updatePosition(totalHeight, 0, 0, 0, 0, animated);
    }
    /**
     * @param totalHeight The Y coordinate where this page should be places (sum of all pages height + margin)
     * @param maxHeight When GridMode, the height + margin of the highest page of the current row (= the height of the row)
     * @param totalWidth When GridMode, the X coordinate where this page should be places (sum of all pages width + margin)
     * @param maxWidth The width reached by the largest column
     * @param rowCount The number of pages in the current row
     */
    public void updatePosition(int totalHeight, int maxHeight, int totalWidth, int maxWidth, int rowCount, boolean animated){
        if(totalHeight == -1) totalHeight = (int) getTranslateY();
        
        PDRectangle pageSize = MainWindow.mainScreen.document.pdfPagesRender.getPageSize(page);
        final double ratio = pageSize.getHeight() / pageSize.getWidth();
        
        setWidth(MainWindow.mainScreen.getPageWidth());
        setHeight(MainWindow.mainScreen.getPageWidth() * ratio);
        
        setMaxWidth(MainWindow.mainScreen.getPageWidth());
        setMinWidth(MainWindow.mainScreen.getPageWidth());
        
        setMaxHeight(MainWindow.mainScreen.getPageWidth() * ratio);
        setMinHeight(MainWindow.mainScreen.getPageWidth() * ratio);
        
        
        if(animated) animateTranslateY(totalHeight);
        else setTranslateY(totalHeight);
    
        if(MainWindow.mainScreen.isIsGridMode()){
            pageGridPosition = PageGridPosition.MIDDLE; // Default value
            pageRowIndex = rowCount;
            
            if(totalWidth <= 0){
                totalWidth = PAGE_MARGIN_GRID;
                pageGridPosition = PageGridPosition.LEFT;
                
                // PageGridSeparator for before page is shown only if it is the first page of the row
                if(pageGridSeparatorBefore == null) pageGridSeparatorBefore = new PageGridSeparator(this, true);
            }else if(pageGridSeparatorBefore != null){
                pageGridSeparatorBefore.remove();
                pageGridSeparatorBefore = null;
            }
    
            defaultTranslateX = totalWidth;
            
            if(animated) animateTranslateX(totalWidth);
            else setTranslateX(totalWidth);
            totalWidth += PAGE_WIDTH + PAGE_MARGIN_GRID;
            
            // Update maxHeight & maxWidth
            if(getHeight() + PAGE_MARGIN_GRID > maxHeight) maxHeight = (int) (getHeight() + PAGE_MARGIN_GRID);
            if(totalWidth > maxWidth) maxWidth = totalWidth;
            
            // PageGridNumber
            if(pageGridNumber == null) pageGridNumber = new PageGridNumber(this);
            if(pageGridSeparator == null) pageGridSeparator = new PageGridSeparator(this, false);
            
        }else{
            if(getTranslateX() != PAGE_MARGIN){ // Reset translateX
                if(animated) animateTranslateX(PAGE_MARGIN);
                else setTranslateX(PAGE_MARGIN);
            }
            totalHeight += (int) (getHeight() + PAGE_MARGIN);
    
            removeGridModePanes();
        }
    
        defaultTranslateY = totalHeight;
        
        if(MainWindow.mainScreen.document.totalPages > page + 1){
            if(MainWindow.mainScreen.isIsGridMode()){
                if(rowCount+1 >= MainWindow.mainScreen.getGridModePagesPerRow()){ // Wrap
                    pageGridPosition = PageGridPosition.RIGHT;
                    totalHeight += maxHeight;
                    MainWindow.mainScreen.document.getPage(page + 1).updatePosition(totalHeight, 0, 0, maxWidth, 0, animated);
                }else{
                    MainWindow.mainScreen.document.getPage(page + 1).updatePosition(totalHeight, maxHeight, totalWidth, maxWidth, rowCount+1, animated);
                }
            }else MainWindow.mainScreen.document.getPage(page + 1).updatePosition(totalHeight, animated);
        }else{ // Last page
            if(MainWindow.mainScreen.isIsGridMode()) MainWindow.mainScreen.updateSize(totalHeight + maxHeight, maxWidth);
            else MainWindow.mainScreen.updateSize(totalHeight);
        }
        
        if(pageEditPane != null) pageEditPane.updatePosition();
        
    }
    private void removeGridModePanes(){
        if(pageGridEditPane != null){
            pageGridEditPane.remove();
            pageGridEditPane = null;
        }
        if(pageGridNumber != null){
            pageGridNumber.remove();
            pageGridNumber = null;
        }
        if(pageGridSeparatorBefore != null){
            pageGridSeparatorBefore.remove();
            pageGridSeparatorBefore = null;
        }
        if(pageGridSeparator != null){
            pageGridSeparator.remove();
            pageGridSeparator = null;
        }
    }
    
    private final Timeline translateYTimeline = new Timeline(60);
    private double targetTranslateY;
    private void animateTranslateY(double translateY){
        animateTranslate(translateYTimeline, translateYProperty(), translateY, targetTranslateY);
        targetTranslateY = translateY;
    }
    private final Timeline translateXTimeline = new Timeline(60);
    private double targetTranslateX;
    private void animateTranslateX(double translateX){
        animateTranslate(translateXTimeline, translateXProperty(), translateX, targetTranslateX);
        targetTranslateX = translateX;
    }
    private void animateTranslate(Timeline timeline, DoubleProperty property, double translate, double targetTranslate){
        int duration = 200;
        if(timeline.getStatus() == Animation.Status.RUNNING){
            if(targetTranslate == translate) return;
            duration -= timeline.getCurrentTime().toMillis();
        }
        timeline.stop();
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(duration), new KeyValue(property, translate))
        );
        timeline.play();
    }
    
    public void updateShowStatus(){
        
        int firstTest = getShowStatus();
        switchVisibleStatus(firstTest);
        if(pageEditPane != null){
            pageEditPane.updateVisibility();
        }
        if(vectorElementPageDrawer != null){
            vectorElementPageDrawer.updateVisibility();
        }
        /*Platform.runLater(() -> {
            if(firstTest == getShowStatus()) switchVisibleStatus(firstTest);
        });*/
    }
    
    public void updateZoom(){
        if(lastShowStatus != 0) return; // Verify that the page is visible
        if(status != PageStatus.RENDERED) return; // Verify that the page is rendered
        
        if(Math.abs(renderedZoomFactor - getRenderingZoomFactor()) > 0.2){
            status = PageStatus.RENDERING;
            render(null);
        }
        
    }
    
    public void removeRender(){
        // If we remove the rendered image while the page is not visible, it will leak.
        // That's why we need to make the page visible during the replacing of the background.
        if(!isVisible() && hasRenderedImage()){
            setVisible(true);
            setBackground(WHITE_BACKGROUND);
            setVisible(false);
        }else{
            setBackground(WHITE_BACKGROUND);
        }
        
    }
    
    public void updateRender(){
        removeRender();
        render(null);
    }
    public void updateRenderAsync(CallBack callback, boolean clearRender){
        if(clearRender) removeRender();
        render(callback);
    }
    
    public void remove(){
        removed = true;
        removeRender();
        setVisible(false);
        
        // Wait a bit before removing the page because else, the render could be leaked
        // JavaFX Bug : When the parent of the ImageView is removed from its parent too fast, the ImageView (NGImageView) is leaked.
        PlatformUtils.runLaterOnUIThread(10000, () -> {
            MainWindow.mainScreen.pane.getChildren().remove(this);
        });
        
        for(Node child : getChildren()){
            if(child instanceof Element e) e.removedFromDocument(false);
        }
        getChildren().clear();
        elements.clear();
        elements = null;
        
        MainWindow.mainScreen.zoomProperty().removeListener(mainScreenZoomListener);
        
        if(pageEditPane != null){
            pageEditPane.delete();
            pageEditPane = null;
        }if(pageZoneSelector != null){
            pageZoneSelector.delete();
            pageZoneSelector = null;
        }
        removeGridModePanes();
        
        vectorElementPageDrawer = null;
        
        loader.translateXProperty().unbind();
        loader.translateYProperty().unbind();
        loader = null;
        
        menu.getItems().clear();
        menu = null;
    }
    
    public int getShowStatus(){ // 0 : Visible | 1 : Hide | 2 : Hard Hide
        int pageHeight = (int) (getHeight() * MainWindow.mainScreen.zoomOperator.getPaneScale());
        int upDistance = (int) (MainWindow.mainScreen.zoomOperator.getPaneY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY() * MainWindow.mainScreen.zoomOperator.getPaneScale() + pageHeight);
        int downDistance = (int) (MainWindow.mainScreen.zoomOperator.getPaneY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY() * MainWindow.mainScreen.zoomOperator.getPaneScale());
        
        //if((upDistance + pageHeight) > 0 && (downDistance - pageHeight) < MainWindow.mainScreen.getHeight()){ // one page of space
        if((upDistance) > 0 && (downDistance) < MainWindow.mainScreen.getHeight()){ // pil poil
            return 0;
        }else{
            if((upDistance + pageHeight * 10) < 0 || (downDistance - pageHeight * 10) > MainWindow.mainScreen.getHeight())
                return 2;
            return 1;
        }
    }
    
    private void switchVisibleStatus(int showStatus){ // 0 : Visible | 1 : Hide | 2 : Hard Hide
        lastShowStatus = showStatus;
        if(showStatus == 0){
            setVisible(true);
            
            if(status == PageStatus.HIDE){
                status = PageStatus.RENDERING;
                loader.setVisible(true);
                setCursor(Cursor.WAIT);
                
                render(null);
            }else{
                updateZoom();
            }
        }else if(showStatus >= 1){
            
            // don't disable this page if the selected element is on this page.
            if(MainWindow.mainScreen.getSelected() != null){
                if(MainWindow.mainScreen.getSelected().getPageNumber() == getPage()){
                    return;
                }
            }
            
            // Hard hide, delete render
            if(showStatus == 2){
                removeRender();
                status = PageStatus.HIDE;
            }
            
            setVisible(false);
        }
    }
    
    private double getRenderingZoomFactor(){
        if(Main.settings.renderWithZoom.getValue()){
            return Math.min(MainWindow.mainScreen.getZoomFactor(), 3) * Main.settings.renderZoom.getValue();
        }else{
            return 1.5 * Main.settings.renderZoom.getValue();
        }
        
    }
    
    private void render(CallBack callBack){
        renderedZoomFactor = getRenderingZoomFactor();
        
        MainWindow.mainScreen.document.pdfPagesRender.renderPage(page, renderedZoomFactor, (image) -> {
            if(removed || status == PageStatus.HIDE) return;
            
            if(image == null){
                status = PageStatus.FAIL;
                return;
            }
            
            setBackground(new Background(
                    Collections.singletonList(new BackgroundFill(
                            javafx.scene.paint.Color.WHITE,
                            CornerRadii.EMPTY,
                            Insets.EMPTY)),
                    Collections.singletonList(new BackgroundImage(
                            image,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(getWidth(), getHeight(), false, false, false, true)))));
            
            setCursor(Cursor.DEFAULT);
            loader.setVisible(false);
            status = PageStatus.RENDERED;
            if(callBack != null) callBack.call();
        });
    }
    
    // COORDINATES
    
    // Bottom of the page coordinates in the Pane of MainScreen
    public double getBottomY(){
        return MainWindow.mainScreen.zoomOperator.getPaneY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + (getHeight() + 15 + getTranslateY()) * MainWindow.mainScreen.zoomOperator.getPaneScale();
    }
    
    public double getPreciseMouseY(){
        return (MainWindow.mainScreen.mouseY - (getBottomY() - getHeight() * MainWindow.mainScreen.zoomOperator.getPaneScale())) / MainWindow.mainScreen.zoomOperator.getPaneScale() + 15;
    }
    
    // ELEMENTS
    
    public void clearElements(){
        ArrayList<Element> toRemove = new ArrayList<>();
        for(Object children : getChildren()){
            if(children instanceof Element) toRemove.add((Element) children);
        }
        for(Element element : toRemove){
            getChildren().remove(element);
            elements.remove(element);
        }
        elements = new ArrayList<>();
    }
    
    public void clearTextElements(){
        ArrayList<TextElement> toRemove = new ArrayList<>();
        for(Object children : getChildren()){
            if(children instanceof TextElement) toRemove.add((TextElement) children);
        }
        for(TextElement element : toRemove){
            getChildren().remove(element);
            elements.remove(element);
        }
    }
    
    public void switchElementPage(Element element, PageRenderer page){
        
        if(element != null){
            
            elements.remove(element);
            getChildren().remove(element);
            
            element.setPage(page);
            
            page.elements.add(element);
            page.getChildren().add(element);
        }
    }
    
    public void addElement(Element element, boolean markAsUnsave, UType undoType){
        
        if(element != null){
            
            elements.add(element);
            getChildren().add(element);
            
            if(markAsUnsave){
                Edition.setUnsave("PageRenderer ElementAdded");
            }
            element.addedToDocument(markAsUnsave);
            
            MainWindow.mainScreen.registerNewAction(new CreateDeleteUndoAction(element, false, undoType));
        }
    }
    
    public void removeElement(Element element, boolean markAsUnsave, UType undoType){
        
        if(element != null){
            elements.remove(element);
            getChildren().remove(element);
            
            if(markAsUnsave) Edition.setUnsave("PageRenderer ElementRemoved");
            element.removedFromDocument(markAsUnsave);
            
            MainWindow.mainScreen.registerNewAction(new CreateDeleteUndoAction(element, true, undoType));
        }
    }
    
    public double getMouseX(){
        return Math.max(Math.min(mouseX, getWidth()), 0);
    }
    public double getMouseY(){
        return Math.max(Math.min(mouseY, getHeight()), 0);
    }
    
    public int getNewElementYOnGrid(){
        return toGridY(getMouseY());
    }
    public int getNewElementXOnGrid(boolean margin){
        return toGridX(getMouseX() + (margin ? 30 : 0));
    }
    
    public int toGridX(double x){
        return (int) (x / getWidth() * Element.GRID_WIDTH);
    }
    public int toGridY(double y){
        return (int) (y / getHeight() * Element.GRID_HEIGHT);
    }
    public double fromGridX(double x){
        return x / Element.GRID_WIDTH * getWidth();
    }
    public double fromGridY(double y){
        return y / Element.GRID_HEIGHT * getHeight();
    }
    
    public double getRealMouseX(){
        return mouseX;
    }
    
    public double getRealMouseY(){
        return mouseY;
    }
    
    public int getPage(){
        return page;
    }
    
    public void setPage(int page){
        if(this.page != page){
            this.page = page;
            if(pageEditPane != null) pageEditPane.updateVisibility();
            if(vectorElementPageDrawer != null) vectorElementPageDrawer.updateVisibility();
            if(pageGridNumber != null) pageGridNumber.updateNumber();
            updateElementsPage();
        }
    }
    
    public void updateElementsPage(){
        for(Element element : elements){
            element.setPage(page);
            Edition.setUnsave("PageRenderer updateElementsPage()");
        }
    }
    
    public ArrayList<Element> getElements(){
        return elements;
    }
    
    public void setStatus(PageStatus status){
        this.status = status;
    }
    
    public PageZoneSelector getPageZoneSelector(){
        if(pageZoneSelector == null) pageZoneSelector = new PageZoneSelector(this);
        return pageZoneSelector;
    }
    public boolean isPageZoneSelectorActive(){
        if(pageZoneSelector == null) return false;
        return pageZoneSelector.isActive();
    }
    
    public VectorElementPageDrawer getVectorElementPageDrawer(){
        if(vectorElementPageDrawer == null) vectorElementPageDrawer = new VectorElementPageDrawer(this);
        return vectorElementPageDrawer;
    }
    public VectorElementPageDrawer getVectorElementPageDrawerNull(){
        return vectorElementPageDrawer;
    }
    public boolean isVectorEditMode(){
        return vectorElementPageDrawer != null && vectorElementPageDrawer.isEditMode();
    }
    
    public double getRatio(){
        return getWidth() / getHeight();
    }
    
    public Image getRenderedImage(){
        return hasRenderedImage() ? getBackground().getImages().get(0).getImage() : new WritableImage((int) getWidth(), (int) getHeight());
    }
    public boolean hasRenderedImage(){
        if(getBackground() != null && getBackground().getImages() != null){
            return !getBackground().getImages().isEmpty();
        }
        return false;
    }
    
    public void quitVectorEditMode(){
        if(isVectorEditMode()) vectorElementPageDrawer.getVectorElement().quitEditMode();
    }
    
    public static int getPageMargin(){
        if(MainWindow.mainScreen.isIsGridMode()) return PAGE_MARGIN_GRID;
        else return PAGE_MARGIN;
    }
    
}
