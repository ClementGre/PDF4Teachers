/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.ObservableChangedUndoAction;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class GrabLine extends Region {
    
    private static final Region line = new Region();
    
    private boolean maxed = false;
    private boolean wasInEditPagesModeWhenMousePressed = false;
    private boolean dragAlreadyDetected = false;
    private double shiftX = 0;
    
    public static final double LINE_WIDTH = 3.5;
    public static final double LINE_OUTER = 1;
    
    public GrabLine(TextElement element, boolean maxed){
        
        /// BORDER ///
        line.setBackground(new Background(new BackgroundFill(Color.color(0 / 255.0, 100 / 255.0, 255 / 255.0, .8),
                new CornerRadii(2), Insets.EMPTY)));
        setMaxed(maxed);
        
        /// POSITION ///
        getChildren().add(line);
        setPadding(new javafx.geometry.Insets(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, 0));
        setPrefWidth(LINE_WIDTH);
        setPrefHeight(0);
        
        line.prefHeightProperty().bind(Bindings.createDoubleBinding(
                () -> element.getHeight() >= 67 ? 50 : element.getHeight() * .75, element.heightProperty()));
        line.layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> element.getHeight() / 2 - line.getHeight() / 2, element.heightProperty(), line.heightProperty()));
        
        line.layoutXProperty().bind(element.widthProperty().subtract(LINE_WIDTH - LINE_OUTER));
        line.setPrefWidth(LINE_WIDTH);
        
        
        /// OTHER ///
        
        line.setPadding(new Insets(-2));
        line.setCursor(Cursor.E_RESIZE);
        
        /// EVENTS ///
        
        line.setOnMousePressed(e -> {
            wasInEditPagesModeWhenMousePressed = PageRenderer.isEditPagesMode();
            if(wasInEditPagesModeWhenMousePressed) return;
            e.consume();
            dragAlreadyDetected = false;
            if(e.getClickCount() == 1){
                shiftX = e.getX();
                element.menu.hide();
            }
        });
        line.setOnMouseDragged(e -> {
            e.consume();
            if(wasInEditPagesModeWhenMousePressed) return;
            
            if(!dragAlreadyDetected){
                MainWindow.mainScreen.registerNewAction(new ObservableChangedUndoAction<>(element, element.textMaxWidthProperty(), element.getTextMaxWidth(), UType.UNDO));
                dragAlreadyDetected = true;
            }
            
            double width = localToParent(line.localToParent(e.getX(), 0)).getX() - shiftX;
            width = StringUtils.clamp(width, 10, PageRenderer.PAGE_WIDTH);
            
            element.setTextMaxWidth(width / PageRenderer.PAGE_WIDTH * 100d);
            element.checkLocation(false);
            element.setPrefWidth(width);
        });
        line.setOnMouseReleased(e -> {
            if(wasInEditPagesModeWhenMousePressed) return;
            e.consume();
            Edition.setUnsave("TextElementResize");
            
            double width = localToParent(line.localToParent(e.getX(), 0)).getX() - shiftX;
            width = StringUtils.clamp(width, 10, PageRenderer.PAGE_WIDTH);
            
            element.setTextMaxWidth(width / PageRenderer.PAGE_WIDTH * 100d);
            element.checkLocation(false);
            element.setPrefWidth(USE_COMPUTED_SIZE);
        });
    }
    
    // change color to red if maxed == true (to show that text is wrapped).
    public void setMaxed(boolean maxed){
        if(this.maxed != maxed){
            this.maxed = maxed;
            
            if(maxed){
                line.setBackground(new Background(new BackgroundFill(Color.color(154 / 255.0, 0 / 255.0, 0 / 255.0, .8),
                        new CornerRadii(2), Insets.EMPTY)));
            }else{
                line.setBackground(new Background(new BackgroundFill(Color.color(0 / 255.0, 100 / 255.0, 255 / 255.0, .8),
                        new CornerRadii(2), Insets.EMPTY)));
            }
        }
    }
    
}
