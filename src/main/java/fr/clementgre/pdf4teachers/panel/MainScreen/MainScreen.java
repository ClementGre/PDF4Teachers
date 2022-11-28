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
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static fr.clementgre.pdf4teachers.document.render.display.PageRenderer.PAGE_WIDTH;

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
    
    private final BooleanProperty isGridView = new SimpleBooleanProperty(false);
    private final BooleanProperty isMultiPagesMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isEditPagesMode = new SimpleBooleanProperty(false);
    
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
                }catch(InterruptedException ex){Log.eNotified(ex);}
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
                }catch(InterruptedException ex){Log.eNotified(ex);}
            }else{
                try{
                    Thread.sleep(200);
                }catch(InterruptedException ex){Log.eNotified(ex);}
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
                zoomOperator.fitWidth(true, false);
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
                info.setStyle("-fx-text-fill: white; -fx-font: 30 'Arial Rounded MT Bold' !important;");
                info.setText(TR.tr("footerBar.documentStatus.noDocument"));
                infoLink.setText(TR.tr("menuBar.tools.convertImages"));
                infoLink.setOnAction(e -> new ConvertDocument());
                
            }else if(status.get() == Status.ERROR){
                info.setStyle("-fx-text-fill: white; -fx-font: 23 'Arial Rounded MT Bold' !important;");
                info.setText(TR.tr("document.status.unableToLoadDocument"));
                infoLink.setVisible(false);
                
            }else if(status.get() == Status.ERROR_EDITION){
                infoLink.setVisible(true);
                info.setStyle("-fx-text-fill: white; -fx-font: 23 'Arial Rounded MT Bold' !important;");
                info.setText(TR.tr("document.status.unableToLoadEdit"));
                infoLink.setText(Main.dataFolder + "editions" + File.separator);
                infoLink.setOnAction(e -> PlatformUtils.openFile(failedEditFile));
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
        
        
        setBorder(Border.EMPTY);
    
        isGridView.bind(isMultiPagesMode.or(isEditPagesMode));
        
        // Pages Shadow
       
        notSelectedShadow.setColor(Color.BLACK);
        selectedShadow.setSpread(.90);
        selectedShadow.setColor(Color.web("#0078d7"));
        notSelectedShadow.radiusProperty().bind(Bindings.min(40, Bindings.divide(10, zoomProperty())));
        selectedShadow.radiusProperty().bind(Bindings.min(25, Bindings.divide(6, zoomProperty())));
        zoomProperty().addListener((observable, oldValue, newValue) -> {
            if(hasDocument(false) && document != null) document.updateSelectedPages();
        });
    
        updateTheme();
        Main.settings.darkTheme.valueProperty().addListener((observable, oldValue, newValue) -> updateTheme());
        
        
        pane.setBorder(Border.EMPTY);
        getChildren().add(pane);
        
        info.setStyle("-fx-text-fill: white; -fx-font: 30 'Arial Rounded MT Bold' !important;");
        info.setTextAlignment(TextAlignment.CENTER);
        
        info.translateXProperty().bind(widthProperty().divide(2).subtract(info.widthProperty().divide(2)));
        info.translateYProperty().bind(heightProperty().divide(2).subtract(info.heightProperty().divide(2)));
        getChildren().add(info);
        
        infoLink.setStyle("-fx-text-fill: white; -fx-font: 17 'Arial Rounded MT Bold' !important;");
        infoLink.layoutYProperty().bind(info.heightProperty().divide(2).add(20));
        infoLink.setTextAlignment(TextAlignment.CENTER);
        
        infoLink.translateXProperty().bind(widthProperty().divide(2).subtract(infoLink.widthProperty().divide(2)));
        infoLink.translateYProperty().bind(heightProperty().divide(2).subtract(infoLink.heightProperty().divide(2)));
        getChildren().add(infoLink);
        
        zoomOperator = new ZoomOperator(pane, this);
    
        // Update show status when scroll level change
        pane.translateYProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if(document != null && document.getPages().size() != 0){
                Platform.runLater(() -> {
                    if(document != null) document.updateShowsStatus();
                });
            }
        });
        pane.scaleXProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null && document.getPages().size() != 0){
    
                // Redraw pages when zooming while being in grid mode
                if(isGridView()){
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
        
        isEditPagesModeProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null && document.getPages().size() != 0){
                double lastVScroll = document.getLastScrollValue();
                
                if(newValue){
                    setSelected(null);
                    AutoTipsManager.showByAction("enterEditPagesMode");
                }
                document.clearSelectedPages();
    
                if(newValue) zoomOperator.overviewWidth(true);
                else zoomOperator.fitWidth(true, false);
                
                document.updatePagesPosition(); // Anomation : 200ms
                Platform.runLater(() -> {
                    if(document != null){
                        if(newValue){ // Entering GridView
                            zoomOperator.vScrollBar.setValue(0);
                        }else{ // Leaving GridView
                            zoomOperator.vScrollBar.setValue(lastVScroll);
                        }
                    }
                });
            }
        });
        isGridViewProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null && document.getPages().size() != 0){
                document.updatePagesPosition();
            }
        });
        
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Reset toPlace only if the user select something (and not de-select)
            if(newValue != null) setToPlace(null);
        });
        
        setOnZoomStarted(event -> hasZoomStartEndEvents = true);
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
        
        setOnScrollStarted(event -> hasScrollStartEndEvents = true);
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
                    if(document != null && isGridView()) document.updatePagesPosition();
                });
            }
        });
        
        setOnMouseDragged(e -> {
            // GrabNScroll
            if(!(((Node) e.getTarget()).getParent() instanceof Element && e.getButton() != MouseButton.MIDDLE) && !(e.getTarget() instanceof Element && e.getButton() != MouseButton.MIDDLE) // Not an element
                    && !(e.getTarget() instanceof PageZoneSelector) && !(e.getTarget() instanceof VectorElementPageDrawer && e.getButton() != MouseButton.MIDDLE) // Not a page filter
                    && (!isEditPagesMode() || !(e.getTarget() instanceof PageRenderer)) // Not in edit pages mode
                    && PaintTab.draggingElement == null){ // No dragging element
                
                onDragForScroll(e);
                
            }else if(e.getButton() == MouseButton.PRIMARY){ // DragNScroll with an Element
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
            initDragOrigin(e);
            setSelected(null);
            if(hasDocument(false)){
                setCursor(Cursor.CLOSED_HAND);
            }
        });
        // consumed by PageRenderer, but can be called if click is released outside the page
        setOnMouseClicked(e -> {
            // prevent dragged clicks by using checking e.isStillSincePress()
            if(e.isStillSincePress() && hasDocument(false)) document.clearSelectedPages();
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
        setOnKeyPressed((e) -> MainWindow.keyboardShortcuts.reportKeyPressedForMultipleUsesKeys(e));
        pane.setOnMouseMoved(e -> {
            paneMouseY = e.getY();
            paneMouseX = e.getX();
        });
        pane.setOnMouseDragged(e -> {
            paneMouseY = e.getY();
            paneMouseX = e.getX();
        });
        
        // window's name
        status.addListener((observable, oldValue, newValue) -> updateWindowName());
        Edition.isSaveProperty().addListener((observable, oldValue, newValue) -> updateWindowName());
        
        // Start the Drag and Scroll Thread
        if(!dragNScrollThread.isAlive()) dragNScrollThread.start();
        
    }
    
    private void updateTheme(){
        if(Main.settings.darkTheme.getValue()){ // Dark theme
            setStyle("-fx-padding: 0; -fx-background-color: #3a3a3e;");
            pane.setStyle("-fx-background-color: #3a3a3e;");
        }else{ // Light theme
            setStyle("-fx-padding: 0; -fx-background-color: #414145;");
            pane.setStyle("-fx-background-color: #414145;");
        }
    }
    
    public void initDragOrigin(MouseEvent e){
        dragStartX = e.getX();
        dragStartY = e.getY();
    }
    public void onDragForScroll(MouseEvent e){
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
    }
    
    
    private void updateWindowName(){
        if(status.get() == Status.OPEN){
            Main.window.setTitle(document.getFile().getName() + (Edition.isSave() ? "" : "*") + " - PDF4Teachers");
        }else{
            Main.window.setTitle(TR.tr("mainWindow.title.noDocument") + " - PDF4Teachers");
        }
    }
    public void openFile(File file){
        openFile(file,  false);
    }
    public void openFile(File file, boolean resetScrollValue){
        
        if(!closeFile(!Main.settings.autoSave.getValue(), false)){
            return;
        }
    
        setIsMultiPagesMode(MainWindow.userData.multiPagesMode);
        setIsEditPagesMode(MainWindow.userData.editPagesMode);
        
        repaint();
        
        try{
            document = new Document(file);
        }catch(IOException e){
            Log.eNotified(e);
            failOpen();
            return;
        }
        status.set(Status.OPEN);
        MainWindow.filesTab.files.getSelectionModel().select(file);
    
        if(MainWindow.userData.editPagesMode) zoomOperator.overviewWidth(true);
        else zoomOperator.fitWidth(true, false);
        
        zoomOperator.vScrollBar.setValue(0);
        document.showPages();
        try{
            document.loadEdition(!resetScrollValue);
        }catch(Exception e){
            Log.eNotified(e, "Unable to load the edit file.");
            closeFile(false, true);
            
            failedEditFile = Edition.getEditFile(file).getAbsolutePath();
            status.set(Status.ERROR_EDITION);
            repaint();
            
            return;
        }
        
        repaint();
        isRotating = false; // In case of a bug, it stayed to true
        
        if(MainWindow.userData.editPagesMode){
            zoomOperator.overviewWidth(true);
            Platform.runLater(() -> zoomOperator.updatePaneDimensions(0, 0.5));
        }else{
            double scrollValue = zoomOperator.vScrollBar.getValue();
            zoomOperator.fitWidth(true, false);
            Platform.runLater(() -> zoomOperator.updatePaneDimensions(scrollValue, 0.5));
        }
        
        AutoTipsManager.showByAction("opendocument");
    }
    
    public boolean openFiles(List<File> toOpenFiles, boolean openDocument){
        MainWindow.filesTab.openFiles(toOpenFiles);
        if(openDocument && toOpenFiles.size() == 1){
            if(FilesUtils.getExtension(toOpenFiles.get(0).getName()).equalsIgnoreCase("pdf")){
                Platform.runLater(() -> openFile(toOpenFiles.get(0)));
                return true;
            }
        }
        return false;
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
            }else if(!forceNotToSave) document.edition.save(true);
    
            MainWindow.userData.multiPagesMode = isMultiPagesMode();
            MainWindow.userData.editPagesMode = isEditPagesMode();
            
            MainWindow.gradeTab.treeView.clearElements(false, false);
            MainWindow.textTab.treeView.onCloseDocument();
            document.stopDocumentSaver();
            document.close();
            document = null;
            SideBar.selectTab(MainWindow.filesTab);
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
    
    public boolean hasDocument(boolean errorDialog){
        
        if(status.get() != Status.OPEN){
            if(errorDialog){
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
        //Log.d("select " + (selected == null ? "null " : selected.getClass().getSimpleName()));
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
        pane.setPrefWidth(PAGE_WIDTH + (PageRenderer.getPageMargin() * 2));
    }
    // For grid view
    public void updateSize(int totalHeight, int totalWidth){
        if(isGridView()){
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
        if(isEditPagesMode()) return getPagesUndoEngine();
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
        if(isEditPagesMode()){
            if(getPagesUndoEngine() != null && Main.window.isFocused()) getPagesUndoEngine().undo();
        }else{
            if(getUndoEngine() != null && Main.window.isFocused()) getUndoEngine().undo();
        }
        
    }
    public void redo(){
        if(isEditPagesMode()){
            if(getPagesUndoEngine() != null && Main.window.isFocused()) getPagesUndoEngine().redo();
        }else{
            if(getUndoEngine() != null && Main.window.isFocused()) getUndoEngine().redo();
        }
    }
    
    public boolean isGridView(){
        return isGridView.get();
    }
    public BooleanProperty isGridViewProperty(){
        return isGridView;
    }
    
    public boolean isEditPagesMode(){
        return isEditPagesMode.get();
    }
    public BooleanProperty isEditPagesModeProperty(){
        return isEditPagesMode;
    }
    public void setIsEditPagesMode(boolean isEditPagesMode){
        this.isEditPagesMode.set(isEditPagesMode);
    }
    
    public boolean isMultiPagesMode(){
        return isMultiPagesMode.get();
    }
    public BooleanProperty isMultiPagesModeProperty(){
        return isMultiPagesMode;
    }
    public void setIsMultiPagesMode(boolean isMultiPagesMode){
        this.isMultiPagesMode.set(isMultiPagesMode);
    }
    
    public double getAvailableWidthInPaneContext(){
        return zoomOperator.getMainScreenWidth() / zoomOperator.getPaneScale();
    }
    public int getGridModePagesPerRow(){
        return Math.max(1, (int) ((getAvailableWidthInPaneContext() - PageRenderer.getPageMargin()) / (PAGE_WIDTH + PageRenderer.getPageMargin())));
    }
    public int getGridModePagesInLastRow(){
        int rest = document.getPagesNumber() % getGridModePagesPerRow();
        return rest == 0 ? getGridModePagesPerRow() : rest;
    }
}