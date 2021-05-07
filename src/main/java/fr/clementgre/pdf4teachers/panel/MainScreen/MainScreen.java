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
import fr.clementgre.pdf4teachers.components.dialogs.alerts.OKAlert;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    
    private int pageWidth = 596;
    
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
                        MainWindow.mainScreen.zoomOperator.scrollUp((dragNScrollFactor + 50) / 2, true);
                    }else if(dragNScrollFactor > 0){
                        MainWindow.mainScreen.zoomOperator.scrollDown(dragNScrollFactor / 2, true);
                    }
                });
                try{
                    Thread.sleep(20);
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }else{
                try{
                    Thread.sleep(200);
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
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
            setToPlace(null);
        });
        
        addEventFilter(ZoomEvent.ZOOM, (ZoomEvent e) -> {
            if(getStatus() == Status.OPEN){
                zoomOperator.zoom(e.getZoomFactor(), e.getSceneX(), e.getSceneY());
            }
        });
        
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if(e.isControlDown()){ // ZOOM
                
                if(getStatus() == Status.OPEN){
                    if(e.getDeltaY() < 0){
                        zoomOperator.zoom(1 + e.getDeltaY() / 200, e.getSceneX(), e.getSceneY());
                    }else if(e.getDeltaY() > 0){
                        zoomOperator.zoom(1 + e.getDeltaY() / 200, e.getSceneX(), e.getSceneY());
                    }
                }
            }else{ // SCROLL
                
                if(Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) / 2){ // Accept side scrolling only if the scroll is not too vertical
                    
                    if(e.getDeltaX() != 0){
                        if(e.getDeltaX() > 0){
                            zoomOperator.scrollLeft((int) (e.getDeltaX() * 2.5), false);
                        }else{
                            zoomOperator.scrollRight((int) (-e.getDeltaX() * 2.5), false);
                        }
                    }
                }
                
                if(e.getDeltaY() != 0){
                    if(e.getDeltaY() > 0){
                        zoomOperator.scrollUp((int) (e.getDeltaY() * 2.5), false);
                    }else{
                        zoomOperator.scrollDown((int) (-e.getDeltaY() * 2.5), false);
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
            if(!(((Node) e.getTarget()).getParent() instanceof Element) && !(e.getTarget() instanceof Element) && !(e.getTarget() instanceof PageZoneSelector)){ // GrabNScroll
                double distY = e.getY() - dragStartY;
                double distX = e.getX() - dragStartX;
                
                if(distY > 0){
                    zoomOperator.scrollUp((int) distY, true);
                }else if(distY < 0){
                    zoomOperator.scrollDown((int) -distY, true);
                }
                
                if(distX > 0){
                    zoomOperator.scrollLeft((int) distX, true);
                }else if(distX < 0){
                    zoomOperator.scrollRight((int) -distX, true);
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
        
        document.showPages();
        try{
            document.loadEdition();
        }catch(Exception e){
            System.err.println("ERREUR : Impossible de changer l'édition");
            e.printStackTrace();
            closeFile(false);
            failedEditFile = Edition.getEditFile(file).getAbsolutePath();
            status.set(Status.ERROR_EDITION);
        }
        
        repaint();
        Platform.runLater(() -> zoomOperator.updatePaneHeight(0, 0.5));
        AutoTipsManager.showByAction("opendocument");
    }
    
    public void failOpen(){
        
        document = null;
        status.set(Status.ERROR);
        repaint();
    }
    
    public boolean closeFile(boolean confirm){
        
        if(document != null){
            
            if(!Edition.isSave()){
                if(confirm){
                    if(!document.save()){
                        return false;
                    }
                }else document.edition.save();
            }
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
    
    public int getPageWidth(){
        return pageWidth;
    }
    public void addPage(PageRenderer page){
        pane.getChildren().add(page);
    }
    public void updateSize(int totalHeight){
        
        pane.setPrefWidth(pageWidth + (PageRenderer.PAGE_HORIZONTAL_MARGIN * 2));
        pane.setPrefHeight(totalHeight);
    }
}