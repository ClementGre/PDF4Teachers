/*
 * Copyright (c) 2020-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import fr.clementgre.pdf4teachers.utils.objects.PositionDimensions;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class PageZoneSelector extends Pane {
    
    public enum SelectionZoneType {
        LIGHT_ON_DARK,
        PDF_ON_DARK
    }
    
    private PageRenderer page;
    private SelectionZoneType selectionZoneType;
    private Region selectionZone = new Region();
    
    private double startX;
    private double startY;
    private CallBackArg<PositionDimensions> callBack;
    
    private boolean isActive;
    
    public PageZoneSelector(PageRenderer page){
        super();
        setCache(false);
        this.page = page;
        
        setCursor(Cursor.CROSSHAIR);
        setVisible(false);
        
        prefWidthProperty().bind(page.widthProperty());
        prefHeightProperty().bind(page.heightProperty());
        
        getChildren().add(selectionZone);
        page.getChildren().add(this);
    }
    
    public void setupSelectionZoneOnce(CallBackArg<PositionDimensions> callBack){
        this.callBack = callBack;
        this.isActive = true;
        
        selectionZone.setPrefWidth(0);
        selectionZone.setPrefHeight(0);
        selectionZone.setVisible(false);
        
        setOnMousePressed((e) -> {
            e.consume();
            if(e.getButton() != MouseButton.PRIMARY){
                end(false);
            }else{
                startX = e.getX();
                startY = e.getY();
                selectionZone.setLayoutX(startX);
                selectionZone.setLayoutY(startY);
                selectionZone.setPrefWidth(0);
                selectionZone.setPrefHeight(0);
                selectionZone.setVisible(true);
            }
        });
        
        setOnMouseDragged(this::updateSelectionPositionDimensions);
        
        setOnKeyPressed((e) -> {
            if(e.getCode() == KeyCode.ESCAPE){
                e.consume();
                this.callBack = null; // so the callback won't be called, considered as exited.
                end(false);
            }
        });
        
        setOnMouseReleased((e) -> {
            e.consume();
            updateSelectionPositionDimensions(e);
            end(true);
        });
        
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue) end(false);
        });
        
    }
    
    private void updateSelectionPositionDimensions(MouseEvent e){
        
        if(startX < 0) startX = 0;
        if(startY < 0) startY = 0;
        
        double x = e.getX() > getWidth() + 1 ? getWidth() + 1 : (e.getX() < -1 ? -1 : e.getX());
        double y = e.getY() > getHeight() + 1 ? getHeight() + 1 : (e.getY() < -1 ? -1 : e.getY());
        double width = x - startX;
        double height = y - startY;
        
        if(width < 0){
            width = -width;
            selectionZone.setLayoutX(startX - width);
        }
        if(height < 0){
            height = -height;
            selectionZone.setLayoutY(startY - height);
        }
        selectionZone.setPrefWidth(width);
        selectionZone.setPrefHeight(height);
        
        if(selectionZoneType == SelectionZoneType.PDF_ON_DARK && page.hasRenderedImage()){
            Image image = page.getRenderedImage();
            BackgroundPosition backgroundPosition = new BackgroundPosition(Side.LEFT, -selectionZone.getLayoutX(), false, Side.TOP, -selectionZone.getLayoutY(), false);
            BackgroundSize backgroundSize = new BackgroundSize(page.getWidth(), page.getHeight(), false, false, false, false);
            BackgroundImage background = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, backgroundPosition, backgroundSize);
            selectionZone.setBackground(new Background(background));
        }
    }
    
    public PositionDimensions getSelectionPositionDimensions(){
        return new PositionDimensions(selectionZone.getWidth() - 2, selectionZone.getHeight() - 2, selectionZone.getLayoutX() + 1, selectionZone.getLayoutY() + 1);
    }
    
    private void end(boolean callCallBack){
        if(callCallBack && selectionZone.getWidth() > 10 && selectionZone.getHeight() > 10 && callBack != null){
            callBack.call(getSelectionPositionDimensions());
        }
        setOnMousePressed(null);
        setOnMouseDragged(null);
        setOnMouseReleased(null);
        setOnKeyPressed(null);
        setBackground(null);
        selectionZone.setVisible(false);
        setDoShow(false);
        
        callBack = null;
        isActive = false;
    }
    public void delete(){
        // In case of leak of this class, the others classes won't be leaked too :
        getChildren().clear();
        selectionZone.setBackground(null);
        selectionZone = null;
        callBack = null;
        isActive = false;
        page = null;
    }
    
    public void setDoShow(boolean visible){
        setVisible(visible);
        if(visible){
            toFront();
            requestFocus();
        }
    }
    
    public void setSelectionZoneType(SelectionZoneType type){
        selectionZoneType = type;
        
        if(type == SelectionZoneType.LIGHT_ON_DARK){
            selectionZone.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
            selectionZone.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.8), CornerRadii.EMPTY, Insets.EMPTY)));
            
        }else if(type == SelectionZoneType.PDF_ON_DARK){
            Image image = page.getRenderedImage();
            BackgroundPosition backgroundPosition = new BackgroundPosition(Side.LEFT, -startX, false, Side.TOP, -startY, false);
            BackgroundSize backgroundSize = new BackgroundSize(page.getWidth(), page.getHeight(), false, false, false, false);
            BackgroundImage background = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, backgroundPosition, backgroundSize);
            
            selectionZone.setBackground(new Background(background));
            selectionZone.setBorder(new Border(new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
            setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.6), CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }
    
    public PageRenderer getPage(){
        return page;
    }
    
    public void setPage(PageRenderer page){
        this.page = page;
    }
    
    public boolean isActive(){
        return isActive;
    }
}
