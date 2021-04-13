package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeView;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jfxtras.styles.jmetro.JMetroStyleClass;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageRenderer extends Pane{
    
    public static int PAGE_HORIZONTAL_MARGIN = 30;
    
    private PageStatus status = PageStatus.HIDE;
    
    private int page;
    private ArrayList<Element> elements = new ArrayList<>();
    private double mouseX = 0;
    private double mouseY = 0;
    
    private ProgressBar loader = new ProgressBar();
    private ContextMenu menu = new ContextMenu();
    
    private int lastShowStatus = 1; // default status is hide (1)
    private double renderedZoomFactor;
    
    private PageEditPane pageEditPane;
    private PageZoneSelector pageCursorRecord;
    
    public PageRenderer(int page){
        this.page = page;
        setup();
    }
    
    private void setup(){
        setStyle("-fx-background-color: white;");
        
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
        
        // UPDATE MOUSE COORDINATES
        // Detect all mouse events, even ones of Elements because they dont consume the event.
        // (This is used to autoscroll when approaching the top/bottom of MainScreen)
        setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
            
            if(getCursor() == Cursor.CROSSHAIR){
                e.consume();
            }
        });
        
        setOnMouseEntered(e -> {
            MainWindow.mainScreen.document.setCurrentPage(page);
            if(pageEditPane == null) pageEditPane = new PageEditPane(this);
            else pageEditPane.setVisible(true);
            
            if(MainWindow.mainScreen.hasToPlace()) setCursor(Cursor.CROSSHAIR);
            else setCursor(Cursor.DEFAULT);
        });
        setOnMouseExited(e -> {
            if(pageEditPane == null) pageEditPane = new PageEditPane(this);
            pageEditPane.setVisible(false);
        });
        
        setOnMousePressed(e -> {
            MainWindow.mainScreen.setSelected(null);
            menu.hide(); menu.getItems().clear();
            
            if(MainWindow.mainScreen.hasToPlace()){
                GraphicElement element = MainWindow.mainScreen.getToPlace();
                element.initializePage(getPage(), e.getX(), e.getY());
                addElement(element, true);
                MainWindow.mainScreen.setSelected(element);
    
                setCursor(Cursor.CROSSHAIR);
            }else{
                if(e.getButton() == MouseButton.SECONDARY) showContextMenu(e.getX(), e.getScreenX(), e.getScreenY());
                else setCursor(Cursor.CLOSED_HAND);
            }
        });
        setOnMouseReleased(e -> {
            setCursor(Cursor.DEFAULT);
        });
        setOnMouseClicked(e -> {
            // ADD TextElement when double click
            if(e.getClickCount() == 2){
                SideBar.selectTab(MainWindow.textTab);
                MainWindow.textTab.newBtn.fire();
                Element selected = MainWindow.mainScreen.getSelected();
                if(selected != null){
                    if(selected instanceof TextElement){
                        selected.setRealX((int) (selected.getPage().getMouseX() * Element.GRID_WIDTH / selected.getPage().getWidth()));
                    }
                }
            }
        });
    }
    
    public void showContextMenu(double pageX, double screenX, double screenY){
        if(MainWindow.gradeTab.treeView.getRoot().getChildren().size() != 0){
            GradeTreeView.defineNaNLocations();
            GradeTreeItem logicalNextGrade = GradeTreeView.getNextLogicGrade();
            if(logicalNextGrade != null){
                CustomMenuItem menuItem = new CustomMenuItem(logicalNextGrade.getEditGraphics((int) MainWindow.textTab.treeView.getWidth() - 50, menu));
                menu.getItems().add(menuItem);
                menuItem.setOnAction((actionEvent) -> logicalNextGrade.getCore().setValue(logicalNextGrade.getCore().getTotal()));
            }
        
            GradeElement documentNextGradeElement = GradeTreeView.getNextGrade(page, (int) pageX);
            GradeTreeItem documentNextGrade = documentNextGradeElement == null ? null : documentNextGradeElement.getGradeTreeItem();
            if(documentNextGrade != null && logicalNextGrade != documentNextGrade){
                CustomMenuItem menuItem = new CustomMenuItem(documentNextGrade.getEditGraphics((int) MainWindow.textTab.treeView.getWidth() - 50, menu));
                menu.getItems().add(0, menuItem);
                menuItem.setOnAction((actionEvent) -> documentNextGradeElement.setValue(documentNextGradeElement.getTotal()));
            }
        
        }
    
        List<TextTreeItem> mostUsed = TextTreeView.getMostUseElements();
    
        for(int i = 0; i <= 7; i++){
            if(mostUsed.size() > i){
                TextTreeItem item = mostUsed.get(i);
            
                Pane pane = new Pane();
            
                Pane sub = new Pane();
            
                ScratchText name = new ScratchText(item.name.getText());
                name.setTextOrigin(VPos.TOP);
                name.setLayoutY(3);
                name.setFont(item.name.getFont());
                name.setFill(item.name.getFill());
            
                sub.setOnMouseClicked(event -> item.addToDocument(false));
            
                sub.setLayoutY(-6);
                sub.setPrefHeight(name.getLayoutBounds().getHeight() + 7);
                sub.setPrefWidth(Math.max(name.getLayoutBounds().getWidth(), MainWindow.textTab.treeView.getWidth() - 50));
            
                pane.setPrefHeight(name.getLayoutBounds().getHeight() + 7 - 14);
            
                sub.getChildren().add(name);
                pane.getChildren().add(sub);
            
                CustomMenuItem menuItem = new CustomMenuItem(pane);
                menu.getItems().add(menuItem);
            }
        }
    
        menu.show(this, screenX, screenY);
    }
    
    public void updatePosition(int totalHeight){
        if(totalHeight == -1) totalHeight = (int) getTranslateY();
        
        PDRectangle pageSize = MainWindow.mainScreen.document.pdfPagesRender.getPageSize(page);
        final double ratio = pageSize.getHeight() / pageSize.getWidth();
        
        setWidth(MainWindow.mainScreen.getPageWidth());
        setHeight(MainWindow.mainScreen.getPageWidth() * ratio);
        
        setMaxWidth(MainWindow.mainScreen.getPageWidth());
        setMinWidth(MainWindow.mainScreen.getPageWidth());
        
        setMaxHeight(MainWindow.mainScreen.getPageWidth() * ratio);
        setMinHeight(MainWindow.mainScreen.getPageWidth() * ratio);
        
        setTranslateX(PAGE_HORIZONTAL_MARGIN);
        setTranslateY(totalHeight);
        
        totalHeight = (int) (totalHeight + getHeight() + 30);
        
        if(MainWindow.mainScreen.document.totalPages > page + 1){
            MainWindow.mainScreen.document.pages.get(page + 1).updatePosition(totalHeight);
        }else{
            MainWindow.mainScreen.updateSize(totalHeight);
        }
        
        if(pageEditPane != null) pageEditPane.updatePosition();
        
    }
    
    public void updateShowStatus(){
        
        int firstTest = getShowStatus();
        switchVisibleStatus(firstTest);
        if(pageEditPane != null) pageEditPane.updateVisibility();
        /*Platform.runLater(() -> {
            if(firstTest == getShowStatus()) switchVisibleStatus(firstTest);
        });*/
    }
    
    public void updateZoom(){
        if(lastShowStatus != 0) return; // Verify that the page is visible
        if(status != PageStatus.RENDERED) return; // Verify that the page is rendered
        if(Math.abs(renderedZoomFactor - getNewRenderedZoomFactor()) > 0.2){
            status = PageStatus.RENDERING;
            render();
        }
        
    }
    
    public void updateRender(){
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        render();
    }
    
    public void remove(){
        switchVisibleStatus(2);
        getChildren().remove(loader);
        
        setOnMouseEntered(null);
        setOnMouseExited(null);
        
        setOnMousePressed(null);
        setOnMouseReleased(null);
        setOnMouseClicked(null);
        setOnMouseMoved(null);
        setOnMouseDragged(null);
        
        setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
    }
    
    public int getShowStatus(){ // 0 : Visible | 1 : Hide | 2 : Hard Hide
        int pageHeight = (int) (getHeight() * MainWindow.mainScreen.pane.getScaleX());
        int upDistance = (int) (MainWindow.mainScreen.pane.getTranslateY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY() * MainWindow.mainScreen.pane.getScaleX() + pageHeight);
        int downDistance = (int) (MainWindow.mainScreen.pane.getTranslateY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY() * MainWindow.mainScreen.pane.getScaleX());
        
        //if((upDistance + pageHeight) > 0 && (downDistance - pageHeight) < MainWindow.mainScreen.getHeight()){ // one page of space
        if((upDistance) > 0 && (downDistance) < MainWindow.mainScreen.getHeight()){ // pil poil
            return 0;
        }else{
            if((upDistance + pageHeight * 10) < 0 || (downDistance - pageHeight * 10) > MainWindow.mainScreen.getHeight())
                return 2;
            return 1;
        }
    }
    
    private void switchVisibleStatus(int showStatus){
        lastShowStatus = showStatus;
        if(showStatus == 0){
            setVisible(true);
            
            if(status == PageStatus.HIDE){
                status = PageStatus.RENDERING;
                loader.setVisible(true);
                setCursor(Cursor.WAIT);
                
                render();
            }else{
                // Update zoom render
                if(Math.abs(renderedZoomFactor - getNewRenderedZoomFactor()) > 0.2){
                    render();
                }
            }
        }else if(showStatus >= 1){
            
            // don't disable this page if the selected element is on this page.
            if(MainWindow.mainScreen.getSelected() != null){
                if(MainWindow.mainScreen.getSelected().getPageNumber() == getPage()){
                    return;
                }
            }
            setVisible(false);
            
            // Hard hide, delete render
            if(showStatus == 2){
                setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                status = PageStatus.HIDE;
            }
            
        }
    }
    
    private double getNewRenderedZoomFactor(){
        return Math.min(MainWindow.mainScreen.getZoomFactor(), 3);
    }
    
    private void render(){
        
        renderedZoomFactor = getNewRenderedZoomFactor();
        
        MainWindow.mainScreen.document.pdfPagesRender.renderPage(page, renderedZoomFactor, getWidth(), getHeight(), (background) -> {
            
            if(background == null){
                status = PageStatus.FAIL;
                return;
            }
            
            setBackground(background);
            
            setCursor(Cursor.DEFAULT);
            loader.setVisible(false);
            status = PageStatus.RENDERED;
        });
    }
    
    // COORDINATES
    
    // Bottom of the page coordinates in the Pane of MainScreen
    public double getBottomY(){
        return MainWindow.mainScreen.pane.getTranslateY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + (getHeight() + 15 + getTranslateY()) * MainWindow.mainScreen.pane.getScaleX();
    }
    
    public double getPreciseMouseY(){
        return (MainWindow.mainScreen.mouseY - (getBottomY() - getHeight() * MainWindow.mainScreen.pane.getScaleX())) / MainWindow.mainScreen.pane.getScaleX() + 15;
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
    
    public void addElement(Element element, boolean update){
        
        if(element != null){
            
            elements.add(element);
            getChildren().add(element);
            
            if(update) Edition.setUnsave();
            element.addedToDocument(!update);
        }
    }
    
    public void removeElement(Element element, boolean update){
        
        if(element != null){
            elements.remove(element);
            getChildren().remove(element);
            
            if(update) Edition.setUnsave();
            element.removedFromDocument(!update);
        }
    }
    
    public double getMouseX(){
        return Math.max(Math.min(mouseX, getWidth()), 0);
    }
    
    public double getMouseY(){
        return Math.max(Math.min(mouseY, getHeight()), 0);
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
            updateElementsPage();
        }
    }
    
    public void updateElementsPage(){
        for(Element element : elements){
            element.setPage(page);
            Edition.setUnsave();
        }
    }
    
    public ArrayList<Element> getElements(){
        return elements;
    }
    
    public void setStatus(PageStatus status){
        this.status = status;
    }
    
    public PageZoneSelector getPageCursorRecord(){
        if(pageCursorRecord == null) pageCursorRecord = new PageZoneSelector(this);
        return pageCursorRecord;
    }
}
