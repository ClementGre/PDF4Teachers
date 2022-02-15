/*
 * Copyright (c) 2019-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.MainScreen;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.ObservableChangedUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoEngine;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.document.render.display.PageZoneSelector;
import fr.clementgre.pdf4teachers.document.render.display.VectorElementPageDrawer;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.OKAlert;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.io.IOException;

import static fr.clementgre.pdf4teachers.document.render.display.PageRenderer.*;

public class MainScreen extends Pane {
    
    public Pane pane = new Pane();
    public ZoomOperator zoomOperator;
    
    public double paneMouseX = 0;
    public double paneMouseY = 0;
    
    public double mouseX = 0;
    public double mouseY = 0;
    
    private final IntegerProperty status = new SimpleIntegerProperty(Status.CLOSED);
    private final ObjectProperty<Element> selected = new SimpleObjectProperty<>();
    private final ObjectProperty<GraphicElement> toPlace = new SimpleObjectProperty<>();
    
    public Document document;
    public String failedEditFile = "";
    
    private final Label info = new Label();
    private final Hyperlink infoLink = new Hyperlink();
    
    public final DropShadow notSelectedShadow = new DropShadow();
    public final DropShadow selectedShadow = new DropShadow();
    
    public static class Status {
        public static final int CLOSED = 0;
        public static final int OPEN = 1;
        public static final int ERROR = 2;
        public static final int ERROR_EDITION = 3;
    }
    
    private static int dragNScrollFactorVertical = 0;
    private static int dragNScrollFactorHorizontal = 0;
    double dragStartX;
    double dragStartY;
    
    private final BooleanProperty isGridMode = new SimpleBooleanProperty(false);
    
    private static final Thread dragNScrollThread = new Thread(() -> {
        while(true){
            if(dragNScrollFactorVertical != 0){
                Platform.runLater(() -> {
                    if(dragNScrollFactorVertical < 0){
                        MainWindow.mainScreen.zoomOperator.scrollUp((dragNScrollFactorVertical + 50) / 2, true, false);
                    }else if(dragNScrollFactorVertical > 0){
                        MainWindow.mainScreen.zoomOperator.scrollDown(dragNScrollFactorVertical / 2, true, false);
                    }
                });
                try{
                    Thread.sleep(20);
                }catch(InterruptedException ex){ex.printStackTrace();}
            }else if(dragNScrollFactorHorizontal != 0){
                Platform.runLater(() -> {
                    if(dragNScrollFactorHorizontal < 0){
                        MainWindow.mainScreen.zoomOperator.scrollLeft((dragNScrollFactorHorizontal + 50) / 2, true, false);
                    }else if(dragNScrollFactorHorizontal > 0){
                        MainWindow.mainScreen.zoomOperator.scrollRight(dragNScrollFactorHorizontal / 2, true, false);
                    }
                });
                try{
                    Thread.sleep(20);
                }catch(InterruptedException ex){ex.printStackTrace();}
            }else{
                try{
                    Thread.sleep(200);
                }catch(InterruptedException ex){ex.printStackTrace();}
            }
            
        }
        
    }, "DragNScroll");
    
    public MainScreen(){
        setPrefWidth(Double.MAX_VALUE);
        setMaxWidth(Double.MAX_VALUE);
        
        // This fits zoom when the MainScreen's width is known (for documents opened before everything is initialized).
        InvalidationListener widthTempListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable){
                widthProperty().removeListener(this);
                zoomOperator.fitWidth(true);
            }
        };
        widthProperty().addListener(widthTempListener);
        
        setup();
        repaint();
    }
    
    public void repaint(){
        if(status.get() != Status.OPEN){
            info.setVisible(true);
            
            if(status.get() == Status.CLOSED){
                infoLink.setVisible(true);
                info.setText(TR.tr("footerBar.documentStatus.noDocument"));
                infoLink.setText(TR.tr("menuBar.tools.convertImages"));
                infoLink.setOnAction(e -> new ConvertDocument());
                
            }else if(status.get() == Status.ERROR){
                info.setText(TR.tr("document.status.unableToLoadDocument"));
                infoLink.setVisible(false);
                
            }else if(status.get() == Status.ERROR_EDITION){
                infoLink.setVisible(true);
                info.setText(TR.tr("document.status.unableToLoadEdit"));
                infoLink.setText(Main.dataFolder + "editions" + File.separator);
                infoLink.setOnAction(e -> PlatformUtils.openDirectory(failedEditFile));
            }
        }else{
            info.setVisible(false);
            infoLink.setVisible(false);
        }
    }
    
    private long lastFinishedScrollingTime = 0;
    private boolean hasScrollStartEndEvents = false;
    private long lastFinishedZoomingTime = 0;
    private boolean hasZoomStartEndEvents = false;
    public static boolean isRotating = false;
    
    private long lastScaleChangedMs = 0;
    public void setup(){
        
        setStyle("-fx-padding: 0; -fx-background-color: #484848;");
        setBorder(Border.EMPTY);
        
        // Pages Shadow
       
        notSelectedShadow.setColor(Color.BLACK);
        selectedShadow.setSpread(.90);
        selectedShadow.setColor(Color.web("#0078d7"));
        notSelectedShadow.radiusProperty().bind(Bindings.min(40, Bindings.divide(10, zoomProperty())));
        selectedShadow.radiusProperty().bind(Bindings.min(25, Bindings.divide(6, zoomProperty())));
        zoomProperty().addListener((observable, oldValue, newValue) -> {
            if(hasDocument(false) && document != null) document.updateSelectedPages();
        });
        
        pane.setStyle("-fx-background-color: #484848;");
        pane.setBorder(Border.EMPTY);
        getChildren().add(pane);
        
        info.setStyle("-fx-text-fill: white; -fx-font-size: 22;");
        info.setTextAlignment(TextAlignment.CENTER);
        
        info.translateXProperty().bind(widthProperty().divide(2).subtract(info.widthProperty().divide(2)));
        info.translateYProperty().bind(heightProperty().divide(2).subtract(info.heightProperty().divide(2)));
        getChildren().add(info);
        
        infoLink.setStyle("-fx-text-fill: white; -fx-font-size: 15;");
        infoLink.setLayoutY(60);
        infoLink.setTextAlignment(TextAlignment.CENTER);
        
        infoLink.translateXProperty().bind(widthProperty().divide(2).subtract(infoLink.widthProperty().divide(2)));
        infoLink.translateYProperty().bind(heightProperty().divide(2).subtract(infoLink.heightProperty().divide(2)));
        getChildren().add(infoLink);
        
        zoomOperator = new ZoomOperator(pane, this);
        
        // Update show status when scroll level change
        pane.translateYProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if(document != null){
                Platform.runLater(() -> {
                    if(document != null) document.updateShowsStatus();
                });
            }
        });
        pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null){
    
                // Redraw pages when zooming while being in grid mode
                if(isIsGridMode()){
                    document.updatePagesPosition();
                }
                
                // Execute action only when it is the last zoom update from 100ms
                lastScaleChangedMs = System.currentTimeMillis();
                PlatformUtils.runLaterOnUIThread(100, () -> {
                    if(System.currentTimeMillis() - lastScaleChangedMs >= 95){
                        lastScaleChangedMs = Long.MAX_VALUE; // Prevent other events to update zoom.
                        if(document != null){
                            document.updateShowsStatus();
                            document.updateZoom();
                        }
                    }
                });
            }
        });
        
        isGridModeProperty().bind(zoomPercentProperty().lessThan(41));
        isGridModeProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null){
                if(newValue) setSelected(null);
                document.clearSelectedPages();
                
                double lastVScroll = document.getLastScrollValue();
                document.updatePagesPosition();
                Platform.runLater(() -> {
                    if(document != null){
                        if(newValue){ // Entering GridView
                            zoomOperator.scrollByTranslateY(0, false);
                            zoomOperator.scrollByTranslateY(0, true);
                        }else{ // Leaving GridView
                            zoomOperator.scrollByTranslateY(-lastVScroll * zoomOperator.getScrollableHeight(zoomOperator.getAimScale()) + zoomOperator.getPaneShiftY(zoomOperator.getAimScale()));
                        }
                    }
                });
            }
        });
        
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Reset toPlace only if the user select something (and not de-select)
            if(newValue != null) setToPlace(null);
        });
        
        setOnZoomStarted(event -> {
            hasZoomStartEndEvents = true;
        });
        setOnZoomFinished(event -> {
            hasZoomStartEndEvents = false;
            lastFinishedZoomingTime = System.currentTimeMillis();
        });
        addEventFilter(ZoomEvent.ZOOM, (ZoomEvent e) -> {
            if(isRotating) return;
            
            // Trackpad detection, see https://stackoverflow.com/questions/31589678/how-to-detect-if-the-source-of-a-scroll-or-mouse-event-is-a-trackpad-or-a-mouse
            long timeDiff = System.currentTimeMillis() - lastFinishedZoomingTime;
            boolean ghostEvent = timeDiff < 1000; // I saw 500-700ms ghost events
            boolean trackpadZooming = hasZoomStartEndEvents || e.isInertia() || ghostEvent;
            
            if(getStatus() == Status.OPEN){
                zoomOperator.zoom(e.getZoomFactor(), e.getSceneX(), e.getSceneY(), trackpadZooming);
            }
        });
        
        setOnScrollStarted(event -> {
            hasScrollStartEndEvents = true;
        });
        setOnScrollFinished(event -> {
            hasScrollStartEndEvents = false;
            lastFinishedScrollingTime = System.currentTimeMillis();
        });
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if(isRotating) return;
            
            // Trackpad detection, see https://stackoverflow.com/questions/31589678/how-to-detect-if-the-source-of-a-scroll-or-mouse-event-is-a-trackpad-or-a-mouse
            long timeDiff = System.currentTimeMillis() - lastFinishedScrollingTime;
            boolean ghostEvent = timeDiff < 1000; // I saw 500-700ms ghost events
            boolean trackpadScrolling = hasScrollStartEndEvents || e.isInertia() || ghostEvent;
            
            if(e.isControlDown()){ // ZOOM
                
                if(getStatus() == Status.OPEN){
                    if(e.getDeltaY() < 0){
                        zoomOperator.zoom(1 + (e.getDeltaY() / 300), e.getSceneX(), e.getSceneY(), trackpadScrolling);
                    }else if(e.getDeltaY() > 0){
                        // Zoom factor needs to be the invert of de-zoom factor.
                        zoomOperator.zoom(1 / (1 - (e.getDeltaY() / 300)), e.getSceneX(), e.getSceneY(), trackpadScrolling);
                    }
                }
            }else{ // SCROLL
                
                if(Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) / 2){ // Accept side scrolling only if the scroll is not too vertical
                    
                    if(e.getDeltaX() != 0){
                        if(e.getDeltaX() > 0){
                            zoomOperator.scrollLeft((int) (e.getDeltaX() * 2.5), false, trackpadScrolling);
                        }else{
                            zoomOperator.scrollRight((int) (-e.getDeltaX() * 2.5), false, trackpadScrolling);
                        }
                    }
                }
                
                if(e.getDeltaY() != 0){
                    if(e.getDeltaY() > 0){
                        zoomOperator.scrollUp((int) (e.getDeltaY() * 2.5), false, trackpadScrolling);
                    }else{
                        zoomOperator.scrollDown((int) (-e.getDeltaY() * 2.5), false, trackpadScrolling);
                    }
                }
            }
        });
        
        heightProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null){
                Platform.runLater(() -> {
                    if(document != null) document.updatePagesPosition();
                });
            }
        });
        widthProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null){
                Platform.runLater(() -> {
                    if(document != null && isIsGridMode()) document.updatePagesPosition();
                });
            }
        });
        
        setOnMouseDragged(e -> {
            // GrabNScroll
            if(!(((Node) e.getTarget()).getParent() instanceof Element) && !(e.getTarget() instanceof Element) // Not an element
                    && !(e.getTarget() instanceof PageZoneSelector) && !(e.getTarget() instanceof VectorElementPageDrawer) // Not a page filter
                    && (!isIsGridMode() || !(e.getTarget() instanceof PageRenderer)) // Not in edit pages mode
                    && PaintTab.draggingElement == null){ // No dragging element
                
                double distY = e.getY() - dragStartY;
                double distX = e.getX() - dragStartX;
                
                if(distY > 0){
                    zoomOperator.scrollUp((int) distY, true, false);
                }else if(distY < 0){
                    zoomOperator.scrollDown((int) -distY, true, false);
                }
                
                if(distX > 0){
                    zoomOperator.scrollLeft((int) distX, true, false);
                }else if(distX < 0){
                    zoomOperator.scrollRight((int) -distX, true, false);
                }
            }else{ // DragNScroll with an Element
                double y = Math.max(1, Math.min(getHeight(), e.getY()));
                if(y < 50){
                    dragNScrollFactorVertical = (int) (y * -1);
                }else if(getHeight() - y < 50){
                    dragNScrollFactorVertical = (int) ((getHeight() - y) * -1 + 50);
                }else{
                    dragNScrollFactorVertical = 0;
                }
                double x = Math.max(1, Math.min(getWidth(), e.getX()));
                if(x < 50){
                    dragNScrollFactorHorizontal = (int) (x * -1);
                }else if(getWidth() - x < 50){
                    dragNScrollFactorHorizontal = (int) ((getWidth() - x) * -1 + 50);
                }else{
                    dragNScrollFactorHorizontal = 0;
                }
            }
            
            dragStartY = e.getY();
            dragStartX = e.getX();
            
            mouseY = e.getY();
            mouseX = e.getX();
        });
        setOnDragOver(e -> {
            if(e.isAccepted()){
                double y = Math.max(1, Math.min(getHeight(), e.getY()));
                if(y < 50){
                    dragNScrollFactorVertical = (int) (y * -1);
                }else if(getHeight() - y < 50){
                    dragNScrollFactorVertical = (int) ((getHeight() - y) * -1 + 50);
                }else{
                    dragNScrollFactorVertical = 0;
                }
                double x = Math.max(1, Math.min(getWidth(), e.getX()));
                if(x < 50){
                    dragNScrollFactorHorizontal = (int) (x * -1);
                }else if(getWidth() - x < 50){
                    dragNScrollFactorHorizontal = (int) ((getWidth() - x) * -1 + 50);
                }else{
                    dragNScrollFactorHorizontal = 0;
                }
                
                dragStartY = e.getY();
                dragStartX = e.getX();
                
                mouseY = e.getY();
                mouseX = e.getX();
            }
        });
        setOnDragExited(e -> {
            dragNScrollFactorVertical = 0;
            dragNScrollFactorHorizontal = 0;
        });
        setOnMousePressed(e -> {
            requestFocus();
            dragStartX = e.getX();
            dragStartY = e.getY();
            setSelected(null);
            if(hasDocument(false)) setCursor(Cursor.CLOSED_HAND);
        });
        setOnMouseClicked(e -> {
            if(hasDocument(false)) document.clearSelectedPages();
        });
        setOnMouseReleased(e -> {
            dragNScrollFactorVertical = 0;
            dragNScrollFactorHorizontal = 0;
            setCursor(Cursor.DEFAULT);
        });
        setOnMouseMoved(e -> {
            mouseY = e.getY();
            mouseX = e.getX();
        });
        setOnKeyPressed((e) -> {
            MainWindow.keyboardShortcuts.reportKeyPressedForMultipleUsesKeys(e);
        });
        pane.setOnMouseMoved(e -> {
            paneMouseY = e.getY();
            paneMouseX = e.getX();
        });
        pane.setOnMouseDragged(e -> {
            paneMouseY = e.getY();
            paneMouseX = e.getX();
        });
        
        // window's name
        status.addListener((observable, oldValue, newValue) -> {
            updateWindowName();
        });
        Edition.isSaveProperty().addListener((observable, oldValue, newValue) -> {
            updateWindowName();
        });
        
        // Start the Drag and Scroll Thread
        if(!dragNScrollThread.isAlive()) dragNScrollThread.start();
        
    }
    
    private void updateWindowName(){
        if(status.get() == Status.OPEN){
            Main.window.setTitle(document.getFile().getName() + (Edition.isSave() ? "" : "*") + " - PDF4Teachers");
        }else{
            Main.window.setTitle(TR.tr("mainWindow.title.noDocument") + " - PDF4Teachers");
        }
    }
    
    public void openFile(File file){
        
        if(!closeFile(!Main.settings.autoSave.getValue(), false)){
            return;
        }
        
        repaint();
        
        try{
            document = new Document(file);
        }catch(IOException e){
            e.printStackTrace();
            failOpen();
            return;
        }
        status.set(Status.OPEN);
        MainWindow.filesTab.files.getSelectionModel().select(file);
        
        zoomOperator.vScrollBar.setValue(0);
        document.showPages();
        try{
            document.loadEdition();
        }catch(Exception e){
            System.err.println("Error: Unable to load the edit file.");
            e.printStackTrace();
            closeFile(false, true);
            
            failedEditFile = Edition.getEditFile(file).getAbsolutePath();
            status.set(Status.ERROR_EDITION);
            repaint();
            
            
            return;
        }
        
        repaint();
        isRotating = false; // In case of a bug, it stayed to true
        
        if(MainWindow.userData.wasGridMode){
            zoomOperator.overviewWidth(true);
            Platform.runLater(() -> zoomOperator.updatePaneDimensions(0, 0.5));
        }else{
            double scrollValue = zoomOperator.vScrollBar.getValue();
            zoomOperator.fitWidth(true);
            Platform.runLater(() -> zoomOperator.updatePaneDimensions(scrollValue, 0.5));
        }
        
        AutoTipsManager.showByAction("opendocument");
    }
    
    public void failOpen(){
        
        document = null;
        status.set(Status.ERROR);
        repaint();
    }
    
    public boolean closeFile(boolean confirm, boolean forceNotToSave){
        setSelected(null);
        
        if(document != null){
            if(confirm){
                if(!document.save(false)){
                    return false;
                }
            }else if(!forceNotToSave) document.edition.save();
    
            MainWindow.userData.wasGridMode = isIsGridMode();
            
            MainWindow.gradeTab.treeView.clearElements(false, false);
            MainWindow.textTab.treeView.onCloseDocument();
            document.stopDocumentSaver();
            document.close();
            document = null;
        }
        
        // No need to clear the pane, the PageRenderer are removing themselves in their remove() method.
        
        zoomOperator.setPaneScale(1);
        
        pane.setPrefHeight(1);
        pane.setPrefWidth(1);
        
        status.set(Status.CLOSED);
        
        repaint();
        
        System.gc(); // clear unused element in RAM
        System.runFinalization();
        return true;
    }
    
    public double getCurrentPaneScale(){
        return pane.getScaleX();
    }
    
    public boolean hasDocument(boolean confirm){
        
        if(status.get() != Status.OPEN){
            if(confirm){
                new OKAlert(Alert.AlertType.ERROR, TR.tr("dialog.unableToPerform.title"),
                        TR.tr("dialog.unableToPerform.title"), TR.tr("footerBar.documentStatus.noDocument")).showAndWait();
            }
            return false;
        }
        return true;
    }
    
    // Navigation
    public void navigateBegin(){
        if(hasDocument(false)){
            zoomOperator.scrollUp(Integer.MAX_VALUE, false, false);
        }
    }
    public void navigateEnd(){
        if(hasDocument(false)){
            zoomOperator.scrollDown(Integer.MAX_VALUE, false, false);
        }
    }
    public void pageUp(){
        if(hasDocument(false)){
            PageRenderer firstTopVisiblePage = document.getFirstTopVisiblePage();
            
            PageRenderer topPage;
            if(firstTopVisiblePage == null){ // Navigate last page
                topPage = document.getPages().get(document.getPagesNumber() - 1);
            }else if(firstTopVisiblePage.getPage() == 0){ // Navigate begin
                navigateBegin();
                return;
            }else{ // Navigate to page
                topPage = document.getPages().get(firstTopVisiblePage.getPage() - 1);
            }
    
            zoomOperator.scrollToPage(topPage);
        }
    }
    public void pageDown(){
        if(hasDocument(false)){
            PageRenderer firstTopVisiblePage = document.getFirstTopVisiblePage();
            
            PageRenderer bottomPage;
            if(firstTopVisiblePage == null){ // Navigate end
                bottomPage = document.getPages().get(document.getPagesNumber() - 1);
            }else if(firstTopVisiblePage.getPage() == document.getPagesNumber() - 1){ // Navigate last page
                navigateEnd();
                return;
            }else{ // Navigate to page
                bottomPage = document.getPage(firstTopVisiblePage.getPage() + 1);
            }
            zoomOperator.scrollToPage(bottomPage);
        }
    }
    private final static int ARROW_NAV_FACTOR = 300;
    public void navigateUp(){
        if(hasDocument(false)){
            zoomOperator.scrollUp(ARROW_NAV_FACTOR, false, false);
        }
    }
    public void navigateDown(){
        if(hasDocument(false)){
            zoomOperator.scrollDown(ARROW_NAV_FACTOR, false, false);
        }
    }
    public void navigateLeft(){
        if(hasDocument(false)){
            zoomOperator.scrollLeft(ARROW_NAV_FACTOR, false, false);
        }
    }
    public void navigateRight(){
        if(hasDocument(false)){
            zoomOperator.scrollRight(ARROW_NAV_FACTOR, false, false);
        }
    }
    public void zoomMore(){
        if(hasDocument(false)){
            zoomOperator.zoomFactor(1.4, false, false);
        }
    }
    public void zoomLess(){
        if(hasDocument(false)){
            zoomOperator.zoomFactor(0.6, false, false);
        }
    }
    
    
    public Element getSelected(){
        return selected.get();
    }
    public ObjectProperty<Element> selectedProperty(){
        return selected;
    }
    public void setSelected(Element selected){
        //System.out.println("select " + (selected == null ? "null " : selected.getClass().getSimpleName()));
        this.selected.set(selected);
    }
    
    public GraphicElement getToPlace(){
        return toPlace.get();
    }
    public boolean hasToPlace(){
        return toPlace.get() != null;
    }
    public ObjectProperty<GraphicElement> toPlaceProperty(){
        return toPlace;
    }
    public void setToPlace(GraphicElement toPlace){
        this.toPlace.set(toPlace);
    }
    
    public IntegerProperty statusProperty(){
        return status;
    }
    public int getStatus(){
        return this.status.get();
    }
    
    public double getZoomFactor(){
        return pane.getScaleX();
    }
    public double getZoomPercent(){
        return getZoomFactor() * 100;
    }
    public DoubleBinding zoomPercentProperty(){
        return pane.scaleXProperty().multiply(100d);
    }
    public DoubleProperty zoomProperty(){
        return pane.scaleXProperty();
    }
    
    public int getPageWidth(){
        return PAGE_WIDTH;
    }
    public void addPage(PageRenderer page){
        pane.getChildren().add(page);
    }
    
    // For classic column view
    public void updateSize(int totalHeight){
        pane.setPrefHeight(totalHeight);
        pane.setPrefWidth(PAGE_WIDTH + (PAGE_MARGIN * 2));
    }
    // For grid view
    public void updateSize(int totalHeight, int totalWidth){
        if(isIsGridMode()){
            pane.setPrefWidth(totalWidth);
            pane.setPrefHeight(totalHeight);
        }
    }
    
    public boolean pasteText(String text){
        if(hasDocument(false)){
            MainWindow.textTab.newBtn.fire();
            MainWindow.textTab.txtArea.setText(text);
            if(getSelected() instanceof TextElement element){
                element.checkLocation(element.getPage().getMouseX(), element.getLayoutY(), false);
            }
            return true;
        }
        return false;
    }
    
    public UndoEngine getUndoEngine(){
        if(hasDocument(false) && document.hasUndoEngine()) return document.getUndoEngine();
        return null;
    }
    // The UndoEngine of the PDF pages editor
    public UndoEngine getPagesUndoEngine(){
        if(hasDocument(false)) return document.pdfPagesRender.editor.getUndoEngine();
        return null;
    }
    public UndoEngine getUndoEngineAuto(){
        if(isIsGridMode()) return getPagesUndoEngine();
        else return getUndoEngine();
    }
    
    public <T> boolean isNextUndoActionProperty(Property<T> property){
        if(getUndoEngine() != null && getUndoEngine().getUndoNextAction() instanceof ObservableChangedUndoAction action){
            return action.getObservableValue() == property;
        } return false;
    }
    public <T> boolean isNextPageUndoActionProperty(Property<T> property){
        if(getPagesUndoEngine() != null && getPagesUndoEngine().getUndoNextAction() instanceof ObservableChangedUndoAction action){
            return action.getObservableValue() == property;
        } return false;
    }
    
    public void registerNewAction(UndoAction action){
        if(action.getUndoType() == UType.NO_UNDO) return;
        if(getUndoEngine() != null){
            getUndoEngine().registerNewAction(action);
        }
    }
    // The UndoEngine of the PDF pages editor
    public void registerNewPageAction(UndoAction action){
        if(action.getUndoType() == UType.NO_UNDO) return;
        if(getPagesUndoEngine() != null){
            getPagesUndoEngine().registerNewAction(action);
        }
    }
    public void undo(){
        if(isIsGridMode()){
            if(getPagesUndoEngine() != null && Main.window.isFocused()) getPagesUndoEngine().undo();
        }else{
            if(getUndoEngine() != null && Main.window.isFocused()) getUndoEngine().undo();
        }
        
    }
    public void redo(){
        if(isIsGridMode()){
            if(getPagesUndoEngine() != null && Main.window.isFocused()) getPagesUndoEngine().redo();
        }else{
            if(getUndoEngine() != null && Main.window.isFocused()) getUndoEngine().redo();
        }
    }
    
    public boolean isIsGridMode(){
        return isGridMode.get();
    }
    public BooleanProperty isGridModeProperty(){
        return isGridMode;
    }
    
    public double getAvailableWidthInPaneContext(){
        return zoomOperator.getMainScreenWidth() / zoomOperator.getPaneScale();
    }
    public int getGridModePagesPerRow(){
        return (int) ((getAvailableWidthInPaneContext() - PAGE_MARGIN_GRID) / (PAGE_WIDTH + PAGE_MARGIN_GRID));
    }
    public int getGridModePagesInLastRow(){
        int rest = document.getPagesNumber() % getGridModePagesPerRow();
        return rest == 0 ? getGridModePagesPerRow() : rest;
    }
}