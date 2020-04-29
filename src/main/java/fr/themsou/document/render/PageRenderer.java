package fr.themsou.document.render;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.panel.leftBar.notes.NoteTreeItem;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.panel.leftBar.texts.LBTextTab;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.utils.Builders;
import fr.themsou.utils.CallBack;
import fr.themsou.windows.MainWindow;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PageRenderer extends Pane{

    PageStatus status = PageStatus.HIDE;

    private ImageView renderView = new ImageView();

    private int page;
    private ArrayList<Element> elements = new ArrayList<>();
    private double mouseX = 0;
    private double mouseY = 0;
    Image img;

    private ProgressBar loader = new ProgressBar();
    ContextMenu menu = new ContextMenu();

    public PageRenderer(int page){
        this.page = page;
        setStyle("-fx-background-color: white;");

        // LOADER
        loader.setPrefWidth(300);
        loader.setPrefHeight(20);
        loader.translateXProperty().bind(widthProperty().divide(2).subtract(loader.widthProperty().divide(2)));
        loader.translateYProperty().bind(heightProperty().divide(2).subtract(loader.heightProperty().divide(2)));
        loader.setVisible(false);
        getChildren().add(loader);
        getChildren().add(0, renderView);

        // BINDINGS & SIZES SETUP
        PDRectangle pageSize = MainWindow.mainScreen.document.pdfPagesRender.getPageSize(page);
        final double ratio = pageSize.getHeight() / pageSize.getWidth();

        setWidth(MainWindow.mainScreen.getPageWidth());
        setHeight(MainWindow.mainScreen.getPageWidth() * ratio);

        setMaxWidth(MainWindow.mainScreen.getPageWidth());
        setMinWidth(MainWindow.mainScreen.getPageWidth());

        setMaxHeight(MainWindow.mainScreen.getPageWidth() * ratio);
        setMinHeight(MainWindow.mainScreen.getPageWidth() * ratio);

        // BORDER
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        setEffect(ds);

        // UPDATE MOUSE COORDINATES
        setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        setOnMouseDragged(e -> {

            mouseX = e.getX();
            mouseY = e.getY();
        });
        setOnMouseEntered(event -> MainWindow.mainScreen.document.setCurrentPage(page));

        Builders.setMenuSize(menu);


        setOnMousePressed(e -> {
            e.consume();

            MainWindow.mainScreen.setSelected(null);
            MainWindow.lbNoteTab.treeView.getSelectionModel().select(null);
            menu.hide();
            menu.getItems().clear();
            if(e.getButton() == MouseButton.SECONDARY){

                NoteTreeView.defineNaNLocations();
                NoteTreeItem note = NoteTreeView.getNextNote(page, (int) e.getY());
                if(note != null) menu.getItems().add(new CustomMenuItem(note.getEditGraphics((int) MainWindow.lbTextTab.treeView.getWidth()-50)));

                List<TextTreeItem> mostUsed = LBTextTab.getMostUseElements();

                for(int i = 0; i <= 3; i++){
                    if(mostUsed.size() > i){
                        TextTreeItem item = mostUsed.get(i);

                        Pane pane = new Pane();

                        Pane sub = new Pane();

                        Text name = new Text(item.name.getText());
                        name.setTextOrigin(VPos.TOP);
                        name.setLayoutY(3);
                        name.setFont(item.name.getFont());
                        name.setFill(item.name.getTextFill());

                        sub.setOnMouseClicked(event -> item.addToDocument(false, false));

                        sub.setLayoutY(-6);
                        sub.setPrefHeight(name.getLayoutBounds().getHeight()+7);
                        sub.setPrefWidth(Math.max(name.getLayoutBounds().getWidth(), MainWindow.lbTextTab.treeView.getWidth() - 50));

                        pane.setPrefHeight(name.getLayoutBounds().getHeight()+7-14);

                        sub.getChildren().add(name);
                        pane.getChildren().add(sub);

                        CustomMenuItem menuItem = new CustomMenuItem(pane);
                        menu.getItems().add(menuItem);
                    }
                }

                menu.show(this, e.getScreenX(), e.getScreenY());
            }
        });

    }

    public void updateShowStatus(){

        int firstTest = getShowStatus();
        switchVisibleStatus(firstTest);
        /*Platform.runLater(() -> {
            if(firstTest == getShowStatus()) switchVisibleStatus(firstTest);
        });*/

    }
    public void remove(){
        switchVisibleStatus(2);
        getChildren().remove(renderView);

        setOnMouseEntered(null);
        setOnMousePressed(null);
        setOnMouseMoved(null);
        setOnMouseDragged(null);
    }
    public int getShowStatus(){ // 0 : Visible | 1 : Hide | 2 : Hard Hide
        int pageHeight = (int) (getHeight()* MainWindow.mainScreen.pane.getScaleX());
        int upDistance = (int) (MainWindow.mainScreen.pane.getTranslateY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY()* MainWindow.mainScreen.pane.getScaleX() + pageHeight);
        int downDistance = (int) (MainWindow.mainScreen.pane.getTranslateY() - MainWindow.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY()* MainWindow.mainScreen.pane.getScaleX());

        if((upDistance + pageHeight) > 0 && (downDistance - pageHeight) < MainWindow.mainScreen.getHeight()){
            return 0;
        }else{
            if((upDistance + pageHeight*10) < 0 || (downDistance - pageHeight*10) > MainWindow.mainScreen.getHeight()) return 2;
            return 1;
        }
    }
    private void switchVisibleStatus(int showStatus){
        if(showStatus == 0){
            setVisible(true);

            if(status == PageStatus.HIDE){
                render();
            }
        }else if(showStatus >= 1){

            setVisible(false);
            if(showStatus == 2){
                renderView.setImage(null);
                img = null;
                status = PageStatus.HIDE;
                for(Node node : getChildren()) node.setVisible(false);
            }

        }

    }
    private void render(){
        status = PageStatus.RENDERING;
        loader.setVisible(true);
        setCursor(Cursor.WAIT);

        MainWindow.mainScreen.document.pdfPagesRender.renderPage(page, image -> {

            if(image == null || renderView == null){
                status = PageStatus.FAIL;
                return;
            }
            img = SwingFXUtils.toFXImage(image, null);
            renderView.setImage(img);
            renderView.setStyle("");

            renderView.fitHeightProperty().bind(heightProperty());
            renderView.fitWidthProperty().bind(widthProperty());

            for(Node node : getChildren()){
                node.setVisible(true);
            }

            setCursor(Cursor.DEFAULT);
            loader.setVisible(false);
            status = PageStatus.RENDERED;
        });
    }

    public void clearElements(){
        getChildren().clear();
        if(status == PageStatus.RENDERED){
            getChildren().add(renderView);
        }
        elements = new ArrayList<>();
    }

    public void switchElementPage(Element element, PageRenderer page){

        if(element != null){

            elements.remove(element);
            getChildren().remove(element);

            element.setPage(page);

            page.elements.add(element);
            page.getChildren().add((Shape) element);
        }
    }
    public void addElementSimple(Element element){

        if(element != null){
            elements.add(element);
            getChildren().add((Shape) element);

            if(element instanceof NoteElement){
                MainWindow.lbNoteTab.treeView.addElement((NoteElement) element);
            }

            if(status != PageStatus.RENDERED){
                ((Shape) element).setVisible(false);
            }
        }
    }
    public void addElement(Element element, boolean update){

        if(element != null){

            elements.add(element);
            getChildren().add((Shape) element);
            Edition.setUnsave();

            if(element instanceof TextElement){
                if(update) MainWindow.lbTextTab.addOnFileElement((TextElement) element);
            }else if(element instanceof NoteElement){
                MainWindow.lbNoteTab.treeView.addElement((NoteElement) element);
            }

            if(status != PageStatus.RENDERED) ((Shape) element).setVisible(false);

        }
    }
    public void removeElement(Element element, boolean update){

        if(element != null){
            elements.remove(element);
            getChildren().remove((Shape) element);
            Edition.setUnsave();

            if(element instanceof TextElement){
                if(update) MainWindow.lbTextTab.removeOnFileElement((TextElement) element);
            }else if(element instanceof NoteElement){
                MainWindow.lbNoteTab.treeView.removeElement((NoteElement) element);
            }

        }
    }

    public double getMouseX(){
        return Math.max(Math.min(mouseX, getWidth()), 0);
    }
    public double getMouseY(){
        return Math.max(Math.min(mouseY, getWidth()), 0);
    }

    public double getRealMouseX(){
        return mouseX;
    }
    public double getRealMouseY(){
        return mouseY;
    }

    public int getPage() {
        return page;
    }
    public ArrayList<Element> getElements() {
        return elements;
    }
}
