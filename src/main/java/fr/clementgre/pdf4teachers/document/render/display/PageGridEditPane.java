/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.menus.NodeMenu;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class PageGridEditPane extends Pane {
    
    private final Timeline timeline = new Timeline(60);
    
    private final PageRenderer page;
    public PageGridEditPane(PageRenderer page){
        this.page = page;
    
        setLayoutX(0);
        setLayoutY(0);
        setPrefWidth(PageRenderer.PAGE_WIDTH);
    }
    
    public void remove(){
        page.getChildren().remove(this);
    }
    public void updateZoom(){
        if(!getChildren().isEmpty() && getOpacity() != 0) {
            show(false);
        }
    }
    public void show(boolean fadeIn){
        
        double factor = (MainWindow.mainScreen.getZoomFactor() - .1) / 3 * 2.5 + .15;
        // .1 .4 -> .15 .4
        
        int rectDim = (int) (30/factor);
        int padding = (int) (30/factor / 5);
    
        Region leftRect = new Region();
        Region leftIcon = SVGPathIcons.generateImage(SVGPathIcons.UNDO, "#e4e4e4", padding, rectDim);
        setupCorner(rectDim, factor, leftRect, leftIcon, false);
        
        Region rightRect = new Region();
        Region rightIcon = SVGPathIcons.generateImage(SVGPathIcons.REDO, "#e4e4e4", padding, rectDim);
        setupCorner(rectDim, factor, rightRect, rightIcon, true);
        
        getChildren().setAll(leftRect, rightRect, leftIcon, rightIcon);
        if(!page.getChildren().contains(this)) {
            page.getChildren().add(this);
        }
        toFront();
        if(fadeIn) {
            fadeIn();
        } else {
            setOpacity(1);
        }
    }
    
    private void setupCorner(int rectDim, double factor, Region rect, Region icon, boolean right){
        rect.setCursor(Cursor.HAND);
        if(right) {
            rect.setLayoutX(PageRenderer.PAGE_WIDTH - rectDim);
        }
        rect.setPrefWidth(rectDim);
        rect.setPrefHeight(rectDim);
        rect.setStyle("-fx-background-color: rgba(0, 0, 0, .5); -fx-background-radius: 0 0 " + (right ? 0 : 5/factor) + " " + (right ? 5/factor : 0) + ";");
        
        icon.setMouseTransparent(true);
        icon.setLayoutX(right ? PageRenderer.PAGE_WIDTH - rectDim : 0);
        
        rect.setOnMouseEntered(e -> icon.setEffect(new ColorAdjust(0, 0, 1, 0)));
        rect.setOnMouseExited(e -> icon.setEffect(null));
    
        rect.setOnMouseClicked(e -> {
            e.consume();
            rotate(right);
        });
        rect.setOnMousePressed(Event::consume);
        rect.setOnMouseReleased(Event::consume);
        rect.setOnMouseDragged(Event::consume);
        rect.setOnDragDetected(Event::consume);
    }
    
    
    private void fadeIn(){
        timeline.stop();
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(100), new KeyValue(opacityProperty(), 1)));
        timeline.play();
        timeline.setOnFinished(null);
    }
    private void fadeOut(){
        timeline.stop();
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(100), new KeyValue(opacityProperty(), 0)));
        timeline.play();
        timeline.setOnFinished((e) -> page.getChildren().remove(this));
    }
    public void hide(){
        fadeOut();
    }
    
    private void rotate(boolean right){
        if(!MainWindow.mainScreen.hasDocument(false)) {
            return;
        }
    
        Document doc = MainWindow.mainScreen.document;
        if(!doc.isPageSelected(page)) {
            doc.selectPage(page.getPage());
        }
        
        int i = 0;
        for(int page : doc.getSelectedPages()){
            doc.pdfPagesRender.editor.rotatePage(doc.getPage(page), right, i == 0 ? UType.UNDO : UType.NO_COUNT, true);
            i++;
        }
    }
    
    public void triggerRightClickMenu(MouseEvent e){
        ContextMenu menu = new ContextMenu();
    
        NodeMenuItem delete;
        if(MainWindow.mainScreen.document.getSelectedPages().size() == 1) {
            delete = new NodeMenuItem(TR.tr("actions.delete"), false);
        } else {
            delete = new NodeMenuItem(TR.tr("document.pageActions.deleteSelected.title"), false);
        }
        
        
        delete.setKeyCombinaison(new KeyCodeCombination(KeyCode.DELETE));
        delete.setOnAction(event -> MainWindow.mainScreen.document.pdfPagesRender.editor.deleteSelectedPages());
        
        Menu capture = new NodeMenu(TR.tr("document.pageActions.capture.title"));
        capture.getItems().addAll(PageEditPane.getCaptureMenu(page, false));
        
        menu.getItems().addAll(delete, capture);
        
        NodeMenuItem.setupMenu(menu);
        menu.show(Main.window, e.getScreenX(), e.getScreenY());
    }
}
