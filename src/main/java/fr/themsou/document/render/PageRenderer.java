package fr.themsou.document.render;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.main.Main;
import fr.themsou.utils.CallBack;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class PageRenderer extends Pane {

    PageStatus status = PageStatus.HIDE;

    private ImageView renderView;
    private int page;
    private ArrayList<Element> elements = new ArrayList<>();
    public double mouseX = 0;
    public double mouseY = 0;

    private ProgressBar loader = new ProgressBar();

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

        // BINDINGS & SIZES SETUP
        PDRectangle pageSize = Main.mainScreen.document.pdfPagesRender.getPageSize(page);
        final double ratio = pageSize.getHeight() / pageSize.getWidth();

        setWidth(Main.mainScreen.getPageWidth());
        setHeight(Main.mainScreen.getPageWidth() * ratio);

        setMaxWidth(Main.mainScreen.getPageWidth());
        setMinWidth(Main.mainScreen.getPageWidth());

        setMaxHeight(Main.mainScreen.getPageWidth() * ratio);
        setMinHeight(Main.mainScreen.getPageWidth() * ratio);

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
        setOnMouseEntered(event -> Main.mainScreen.document.setCurrentPage(page));

        // Show Status
        Main.mainScreen.pane.translateYProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            updateShowStatus();
        });

    }

    public void updateShowStatus(){

        int pageHeight = (int) (getHeight()*Main.mainScreen.pane.getScaleX());
        int upDistance = (int) (Main.mainScreen.pane.getTranslateY() - Main.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY()*Main.mainScreen.pane.getScaleX() + pageHeight);
        int downDistance = (int) (Main.mainScreen.pane.getTranslateY() - Main.mainScreen.zoomOperator.getPaneShiftY() + getTranslateY()*Main.mainScreen.pane.getScaleX());

        if((upDistance + pageHeight) > 0 && (downDistance - pageHeight) < Main.mainScreen.getHeight()){
            setVisible(true);
            if(status == PageStatus.HIDE) render();
        }else{
            setVisible(false);
            if((upDistance + pageHeight*10) < 0 || (downDistance - pageHeight*10) > Main.mainScreen.getHeight()){
                getChildren().remove(renderView);
                status = PageStatus.HIDE;
                for(Node node : getChildren()){
                    node.setVisible(false);
                }
            }
        }

    }
    private void render(){
        status = PageStatus.RENDERING;
        loader.setVisible(true);
        setCursor(Cursor.WAIT);

        Main.mainScreen.document.pdfPagesRender.renderPage(page, new CallBack<>() {
            @Override public void call(BufferedImage image){

                if(image == null){
                    status = PageStatus.FAIL;
                    return;
                }

                renderView = new ImageView(SwingFXUtils.toFXImage(image, null));

                renderView.fitHeightProperty().bind(heightProperty());
                renderView.fitWidthProperty().bind(widthProperty());

                for(Node node : getChildren()){
                    node.setVisible(true);
                }

                setCursor(Cursor.DEFAULT);
                loader.setVisible(false);
                getChildren().add(0, renderView);
                status = PageStatus.RENDERED;
            }
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
                Main.lbNoteTab.treeView.addElement((NoteElement) element);
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
                if(update) Main.lbTextTab.addOnFileElement((TextElement) element);
            }else if(element instanceof NoteElement){
                Main.lbNoteTab.treeView.addElement((NoteElement) element);
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
                if(update) Main.lbTextTab.removeOnFileElement((TextElement) element);
            }else if(element instanceof NoteElement){
                Main.lbNoteTab.treeView.removeElement((NoteElement) element);
            }

        }
    }

    public int getPage() {
        return page;
    }
    public ArrayList<Element> getElements() {
        return elements;
    }
}
