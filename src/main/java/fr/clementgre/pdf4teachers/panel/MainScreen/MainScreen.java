package fr.clementgre.pdf4teachers.panel.MainScreen;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertDocument;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.document.render.display.PageZoneSelector;
import fr.clementgre.pdf4teachers.interfaces.autotips.AutoTipsManager;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.OKAlert;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.io.IOException;

public class MainScreen extends Pane{
    
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
    
    public static class Status{
        public static final int CLOSED = 0;
        public static final int OPEN = 1;
        public static final int ERROR = 2;
        public static final int ERROR_EDITION = 3;
    }
    
    private static int dragNScrollFactor = 0;
    double dragStartX;
    double dragStartY;
    
    private static final Thread dragNScrollThread = new Thread(() -> {
        while(true){
            if(dragNScrollFactor != 0){
                Platform.runLater(() -> {
                    if(dragNScrollFactor < 0){
                        MainWindow.mainScreen.zoomOperator.scrollUp((dragNScrollFactor + 50) / 2, true, false);
                    }else if(dragNScrollFactor > 0){
                        MainWindow.mainScreen.zoomOperator.scrollDown(dragNScrollFactor / 2, true, false);
                    }
                });
                try{
                    Thread.sleep(20);
                }catch(InterruptedException ex){ ex.printStackTrace(); }
            }else{
                try{
                    Thread.sleep(200);
                }catch(InterruptedException ex){ ex.printStackTrace(); }
            }
            
        }
        
    }, "DragNScroll");
    
    public MainScreen(){
        
        //if(Main.isOSX()) PageRenderer.PAGE_HORIZONTAL_MARGIN = 15;
        
        setPrefWidth(Double.MAX_VALUE);
        
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
    
    public void setup(){
        
        setStyle("-fx-padding: 0; -fx-background-color: #484848;");
        setBorder(Border.EMPTY);
        
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
                Platform.runLater(() -> {
                    if(document != null) document.updateShowsStatus();
                });
            }
        });
        
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Reset toPlace only if the user select something (and not de-select)
            if(newValue != null) setToPlace(null);
        });
        
        addEventFilter(ZoomEvent.ZOOM, (ZoomEvent e) -> {
            if(getStatus() == Status.OPEN){
                zoomOperator.zoom(e.getZoomFactor(), e.getSceneX(), e.getSceneY(), true);
            }
        });
        
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if(e.isControlDown()){ // ZOOM
                
                if(getStatus() == Status.OPEN){
                    if(e.getDeltaY() < 0){
                        zoomOperator.zoom(1 + e.getDeltaY() / 200, e.getSceneX(), e.getSceneY(), true);
                    }else if(e.getDeltaY() > 0){
                        zoomOperator.zoom(1 + e.getDeltaY() / 200, e.getSceneX(), e.getSceneY(), true);
                    }
                }
            }else{ // SCROLL
                
                if(Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) / 2){ // Accept side scrolling only if the scroll is not too vertical
                    
                    if(e.getDeltaX() != 0){
                        if(e.getDeltaX() > 0){
                            zoomOperator.scrollLeft((int) (e.getDeltaX() * 2.5), false, true);
                        }else{
                            zoomOperator.scrollRight((int) (-e.getDeltaX() * 2.5), false, true);
                        }
                    }
                }
                
                if(e.getDeltaY() != 0){
                    if(e.getDeltaY() > 0){
                        zoomOperator.scrollUp((int) (e.getDeltaY() * 2.5), false, true);
                    }else{
                        zoomOperator.scrollDown((int) (-e.getDeltaY() * 2.5), false, true);
                    }
                }
                
                
            }
            
        });
        heightProperty().addListener((observable, oldValue, newValue) -> {
            if(document != null){
                Platform.runLater(() -> {
                    if(document != null) document.updateShowsStatus();
                });
            }
        });
        
        setOnMouseDragged(e -> {
            if(!(((Node) e.getTarget()).getParent() instanceof Element) && !(e.getTarget() instanceof Element) && !(e.getTarget() instanceof PageZoneSelector)
                    && (!PageRenderer.isEditPagesMode() || !(e.getTarget() instanceof PageRenderer))){ // GrabNScroll
    
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
                    dragNScrollFactor = (int) (y * -1);
                }else if(getHeight() - y < 50){
                    dragNScrollFactor = (int) ((getHeight() - y) * -1 + 50);
                }else{
                    dragNScrollFactor = 0;
                }
            }
            
            dragStartY = e.getY();
            dragStartX = e.getX();
            
            mouseY = e.getY();
            mouseX = e.getX();
        });
        setOnMousePressed(e -> {
            requestFocus();
            dragStartX = e.getX();
            dragStartY = e.getY();
            setSelected(null);
            if(hasDocument(false)) setCursor(Cursor.CLOSED_HAND);
        });
        setOnMouseReleased(e -> {
            dragNScrollFactor = 0;
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
            Main.window.setTitle("PDF4Teachers - " + document.getFile().getName() + (Edition.isSave() ? "" : "*"));
        }else{
            Main.window.setTitle(TR.tr("mainWindow.title.noDocument"));
        }
    }
    
    public void openFile(File file){
        
        if(!closeFile(!Main.settings.autoSave.getValue())){
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
            document = null;
            closeFile(false);
            failedEditFile = Edition.getEditFile(file).getAbsolutePath();
            status.set(Status.ERROR_EDITION);
            repaint();
            return;
        }
    
        repaint();
        
        double scrollValue = zoomOperator.vScrollBar.getValue();
        zoomOperator.fitWidth(true);
        Platform.runLater(() -> {
            zoomOperator.updatePaneHeight(scrollValue, 0.5);
        });
        AutoTipsManager.showByAction("opendocument");
    }
    
    public void failOpen(){
        
        document = null;
        status.set(Status.ERROR);
        repaint();
    }
    
    public boolean closeFile(boolean confirm){
        
        if(document != null){
            
            if(confirm){
                if(!document.save()){
                    return false;
                }
            }else document.edition.save();
    
            MainWindow.gradeTab.treeView.clearElements(false, false);
            MainWindow.textTab.treeView.onCloseDocument();
            document.stopDocumentSaver();
            document.close();
            document = null;
        }
        
        pane.getChildren().clear();
        
        pane.setScaleX(1);
        pane.setScaleY(1);
        
        pane.setPrefHeight(1);
        pane.setPrefWidth(1);
        
        status.set(Status.CLOSED);
        selected.set(null);
        
        repaint();
        
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
                topPage = document.getPages().get(document.getPagesNumber()-1);
            }else if(firstTopVisiblePage.getPage() == 0){ // Navigate begin
                navigateBegin(); return;
            }else{ // Navigate to page
                topPage = document.getPages().get(firstTopVisiblePage.getPage()-1);
            }
    
            int toScroll = (int) ((pane.getTranslateY()-zoomOperator.getPaneShiftY()) + (topPage.getTranslateY() - PageRenderer.PAGE_VERTICAL_MARGIN+5)*pane.getScaleX());
            zoomOperator.scroll(toScroll, false, false);
        }
    }
    public void pageDown(){
        if(hasDocument(false)){
            PageRenderer firstTopVisiblePage = document.getFirstTopVisiblePage();
    
            PageRenderer bottomPage;
            if(firstTopVisiblePage == null){ // Navigate end
                bottomPage = document.getPages().get(document.getPagesNumber()-1);
            }else if(firstTopVisiblePage.getPage() == document.getPagesNumber()-1){ // Navigate last page
                navigateEnd(); return;
            }else{ // Navigate to page
                bottomPage = document.getPage(firstTopVisiblePage.getPage()+1);
            }
            
            int toScroll = (int) ((pane.getTranslateY()-zoomOperator.getPaneShiftY()) + (bottomPage.getTranslateY() - PageRenderer.PAGE_VERTICAL_MARGIN+5)*pane.getScaleX());
            zoomOperator.scroll(toScroll, false, false);
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
        return pane.scaleXProperty().multiply(100);
    }
    public DoubleProperty zoomProperty(){
        return pane.scaleXProperty();
    }
    
    public int getPageWidth(){
        return PageRenderer.PAGE_WIDTH;
    }
    public void addPage(PageRenderer page){
        pane.getChildren().add(page);
    }
    public void updateSize(int totalHeight){
        
        pane.setPrefWidth(PageRenderer.PAGE_WIDTH + (PageRenderer.PAGE_HORIZONTAL_MARGIN * 2));
        pane.setPrefHeight(totalHeight);
    }
    
    public void pasteText(String text){
        if(hasDocument(false)){
            MainWindow.textTab.newBtn.fire();
            MainWindow.textTab.txtArea.setText(text);
        }
    }
}