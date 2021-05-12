package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.ScratchText;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.*;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeView;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.TextWrapper;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.dnd.DragSourceDragEvent;
import java.util.ArrayList;
import java.util.List;

public class PageRenderer extends Pane{
    
    public static final int PAGE_WIDTH = 596;
    public static final int PAGE_HORIZONTAL_MARGIN = 30;
    public static final int PAGE_VERTICAL_MARGIN = 30;
    
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
    
    private GraphicElement placingElement = null;
    
    private double shiftY = 0;
    private double defaultTranslateY = 0;
    
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
        
        translateYProperty().addListener(e -> {
            updateShowStatus();
        });
    
        //////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// ZOOM //////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        MainWindow.mainScreen.zoomProperty().addListener((observable, oldValue, newValue) -> {
            if(isEditPagesMode()) setCursor(Cursor.MOVE);
            else if(MainWindow.mainScreen.hasToPlace()) setCursor(Cursor.CROSSHAIR);
            else setCursor(Cursor.DEFAULT);
        });
        
        //////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////// DRAGGING ////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
            
            if(isEditPagesMode()){
                
                double translateY = getTranslateY() + e.getY() - shiftY;
                if(getPage() == 0) translateY = Math.max(translateY, defaultTranslateY);
                if(getPage() == MainWindow.mainScreen.document.totalPages - 1) translateY = Math.min(translateY, defaultTranslateY);
                setTranslateY(translateY);
                
                if(getTranslateY() - defaultTranslateY > getHeight()*3/4 && getPage() != MainWindow.mainScreen.document.totalPages - 1){
                    if(getTranslateY() - defaultTranslateY > (getHeight()+PAGE_VERTICAL_MARGIN)*2){ // Descend multiple pages
                        MainWindow.mainScreen.document.pdfPagesRender.editor.movePage(this,
                                StringUtils.clamp((int) ((getTranslateY() - defaultTranslateY) / (getHeight()+PAGE_VERTICAL_MARGIN)), 1, MainWindow.mainScreen.document.totalPages-1-getPage()));
                    }else{ // Descend one page
                        MainWindow.mainScreen.document.pdfPagesRender.editor.descendPage(this);
                    }
                }else if(getTranslateY() - defaultTranslateY < -getHeight()*3/4 && getPage() != 0){
                    if(getTranslateY() - defaultTranslateY < -(getHeight()+PAGE_VERTICAL_MARGIN)*2){ // Ascend multiple pages
                        MainWindow.mainScreen.document.pdfPagesRender.editor.movePage(this,
                                StringUtils.clamp((int) ((getTranslateY() - defaultTranslateY) / (getHeight()+PAGE_VERTICAL_MARGIN)), -getPage(), -1));
                    }else{ // Ascend one page
                        MainWindow.mainScreen.document.pdfPagesRender.editor.ascendPage(this);
                    }
                }
            }else{
                if(placingElement != null){
                    e.consume();
                    placingElement.simulateDragToResize(e.getX()-placingElement.getLayoutX(), e.getY()-placingElement.getLayoutY(), e.isShiftDown());
                }
            }
            
        });
    
        //////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////// ENTER / EXIT //////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setOnMouseEntered(e -> {
            MainWindow.mainScreen.document.setCurrentPage(page);
            if(pageEditPane == null) pageEditPane = new PageEditPane(this);
            else pageEditPane.setVisible(true);
            
            if(isEditPagesMode()) setCursor(Cursor.MOVE);
            else if(MainWindow.mainScreen.hasToPlace()) setCursor(Cursor.CROSSHAIR);
            else setCursor(Cursor.DEFAULT);
            
        });
        setOnMouseExited(e -> {
            if(pageEditPane == null) pageEditPane = new PageEditPane(this);
            pageEditPane.setVisible(false);
        });
    
        //////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////// PRESS / RELEASE ////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setOnMousePressed(e -> {
            requestFocus();
            toFront();
            shiftY = e.getY();
            defaultTranslateY = getTranslateY();
            MainWindow.mainScreen.setSelected(null);
            menu.hide(); menu.getItems().clear();
            
            if(isEditPagesMode()) return;
            
            if(MainWindow.mainScreen.hasToPlace()){
                placingElement = MainWindow.mainScreen.getToPlace();
                placingElement.initializePage(getPage(), e.getX(), e.getY());
                addElement(placingElement, true);
                MainWindow.mainScreen.setSelected(placingElement);
                placingElement.requestFocus();
                placingElement.incrementUsesAndLastUse();
                
                int shiftX = (int) placingElement.getLayoutX();
                int shiftY = (int) placingElement.getLayoutY();
                placingElement.setupMousePressVars(e.getX()-shiftX, e.getY()-shiftY, null, true);
                placingElement.simulateDragToResize(e.getX()-placingElement.getLayoutX(), e.getY()-placingElement.getLayoutY(), e.isShiftDown());
                
                setCursor(Cursor.CROSSHAIR);
            }else{
                if(e.getButton() == MouseButton.SECONDARY) showContextMenu(e.getY(), e.getScreenX(), e.getScreenY());
                else setCursor(Cursor.CLOSED_HAND);
            }
        });
        setOnMouseReleased(e -> {
            if(getTranslateY() != defaultTranslateY) animateTranslateY(defaultTranslateY);
            
            if(placingElement != null && !isEditPagesMode()){
                e.consume();
                if(placingElement.getWidth() < 10 && placingElement.getHeight() < 10){
                    placingElement.simulateReleaseFromResize();
                    placingElement.defineSizeAuto();
                }else{
                    placingElement.simulateReleaseFromResize();
                }
                placingElement = null;
            }
            if(isEditPagesMode()) setCursor(Cursor.MOVE);
            else setCursor(Cursor.DEFAULT);
        });
        setOnMouseClicked(e -> {
            // ADD TextElement when double click
            if(e.getClickCount() == 2 && !isEditPagesMode()){
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
    
        //////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////// ROTATE /////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        
        setEventHandler(RotateEvent.ROTATE, e -> {
            double rotate = e.getTotalAngle()*2;
            if(rotate > 85) rotate = 90;
            else if(rotate < -85) rotate = -90;
            else if(rotate > -3 && rotate < 3) rotate = 0;
            setVisibleRotate(rotate, false);
        });
        setEventHandler(RotateEvent.ROTATION_FINISHED, e -> {
            double rotate = e.getTotalAngle()*2;
            if(rotate < -45){
                setVisibleRotate(-90, true, () -> {
                    MainWindow.mainScreen.document.pdfPagesRender.editor.rotateLeftPage(this, false);
                    setRotate(0);
                });
            }else if(rotate > 45){
                setVisibleRotate(90, true, () -> {
                    MainWindow.mainScreen.document.pdfPagesRender.editor.rotateRightPage(this, false);
                    setRotate(0);
                });
            }else{
                setVisibleRotate(0, true);
            }
        });
        
    }
    public static boolean isEditPagesMode(){
        return MainWindow.mainScreen.getZoomPercent() < 41;
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
            if(finished != null) timeline.setOnFinished(e -> finished.call());
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
    
        for(int i = 0; i <= 7; i++){
            if(mostUsed.size() > i){
                TextTreeItem item = mostUsed.get(i);
                
                NodeMenuItem menuItem = new NodeMenuItem();
                
                ScratchText name = new ScratchText(new TextWrapper(item.name.getText().replace("\n", ""), null, 175).wrapFirstLine());
                name.setTextOrigin(VPos.TOP);
                name.setFont(item.name.getFont());
                name.setFill(item.name.getFill());
                
                menuItem.setLeftData(name);
                menuItem.setOnAction((e) -> {
                    item.addToDocument(false);
                    MainWindow.textTab.selectItem();
                });
                menu.getItems().add(menuItem);
            }
        }
    
        NodeMenuItem.setupMenu(menu);
        menu.show(this, screenX, screenY);
    }
    
    public void updatePosition(int totalHeight, boolean animated){
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
        if(animated) animateTranslateY(totalHeight);
        else setTranslateY(totalHeight);
        
        defaultTranslateY = totalHeight;
        
        totalHeight = (int) (totalHeight + getHeight() + PAGE_VERTICAL_MARGIN);
        
        if(MainWindow.mainScreen.document.totalPages > page + 1){
            MainWindow.mainScreen.document.pages.get(page + 1).updatePosition(totalHeight, animated);
        }else{
            MainWindow.mainScreen.updateSize(totalHeight);
        }
        
        if(pageEditPane != null) pageEditPane.updatePosition();
        
    }
    private void animateTranslateY(double translateY){
        Timeline timeline = new Timeline(60);
        timeline.getKeyFrames().clear();
        double durationFactor = Math.abs(translateY - getTranslateY()) / 800;
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200 * durationFactor), new KeyValue(translateYProperty(), translateY))
        );
        timeline.play();
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
        if(Math.abs(renderedZoomFactor - getRenderingZoomFactor()) > 0.2){
            status = PageStatus.RENDERING;
            render(null);
        }
        
    }
    
    public void updateRender(){
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        render(null);
    }
    public void updateRenderAsync(CallBack callback, boolean clearRender){
        if(clearRender) setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        render(callback);
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
            setVisible(false);
            
            // Hard hide, delete render
            if(showStatus == 2){
                setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                status = PageStatus.HIDE;
            }
            
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
        
        MainWindow.mainScreen.document.pdfPagesRender.renderPage(page, renderedZoomFactor, getWidth(), getHeight(), (background) -> {
            
            if(background == null){
                status = PageStatus.FAIL;
                return;
            }
            
            setBackground(background);
            
            setCursor(Cursor.DEFAULT);
            loader.setVisible(false);
            status = PageStatus.RENDERED;
            if(callBack != null) callBack.call();
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
