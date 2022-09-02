/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.exceptions.PathParseException;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

public class VectorGridElement{
    
    private final SVGPath svgPath = new SVGPath();
    private final Scale svgScale = new Scale();
    private double lastDisplayWidth = 0;
    
    private VectorData vectorData;
    
    private static final int RENDER_WIDTH = 75;
    private boolean fake = false;
    float width, height = 1;
    
    public VectorGridElement(VectorData vectorData){
        this.vectorData = vectorData;
        setup();
    }
    public VectorGridElement(boolean fake){
        if(!fake) throw new IllegalArgumentException("You should use the (VectorData vectorData) constructor if this VectorGridElement isn't fake.");
        this.fake = true;
    }
    
    private void setup(){
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
        
        AtomicLong lastDisplayChangesTime = new AtomicLong();
        vectorData.setDisplayChangesCallback(() -> {
            lastDisplayChangesTime.set(System.currentTimeMillis());
            PlatformUtils.runLaterOnUIThread(500, () -> {
                if(lastDisplayChangesTime.get() < System.currentTimeMillis()-490){
                    renderSvgPath();
                    layoutSVGPath(lastDisplayWidth);
                }
            });
        });
        vectorData.setSpecsChangesCallback(this::updateSVGSpecs);
    }
    
    public void addToFavorite(VectorGridView gridView){
        MainWindow.paintTab.favouriteVectors.getList().addItems(Collections.singletonList(clone()));
        if(Main.settings.listsMoveAndDontCopy.getValue()) removeFromList(gridView);
    }
    public void addToLast(VectorGridView gridView){
        MainWindow.paintTab.lastVectors.getList().addItems(Collections.singletonList(clone()));
        if(Main.settings.listsMoveAndDontCopy.getValue()) removeFromList(gridView);
    }
    
    public void removeFromList(VectorGridView gridView){
        gridView.removeItems(Collections.singletonList(this));
    }
    
    public VectorElement addToDocument(boolean link){
        if(MainWindow.mainScreen.hasDocument(false)) return vectorData.addToDocument(link);
        return null;
    }
    public void setAsToPlaceElement(boolean link){
        if(MainWindow.mainScreen.hasDocument(false)) vectorData.setAsToPlaceElement(link);
    }
    
    public boolean equals(VectorElement element){
        return vectorData.equals(element);
    }
    
    // SORTER
    
    public int compareUseWith(VectorGridElement element){
        if(isFake()) return -1;
        return element.getVectorData().getUseCount() - vectorData.getUseCount();
    }
    public int compareLastUseTimeWith(VectorGridElement element){
        if(isFake()) return -1;
        long val = (element.getVectorData().getLastUse() - vectorData.getLastUse());
        return val > 0 ? 1 : (val < 0 ? -1 : 0);
    }
    
    // Getters / Setters
    
    public VectorData getVectorData(){
        return vectorData;
    }
    public SVGPath getSvgPath(){
        return svgPath;
    }
    public double getLastDisplayWidth(){
        return lastDisplayWidth;
    }
    
    // RENDER SVG
    private void renderSvgPath(){
        
        SVGPath noScaledSVGPath = new SVGPath();
        noScaledSVGPath.setContent(vectorData.getPath());
        
        float padding = 0;
        float ratio = ((float) vectorData.getWidth()) / vectorData.getHeight();
    
        svgPath.getTransforms().setAll(svgScale);
        
        if(ratio > 1){
            width = RENDER_WIDTH;
            height = Math.max(width/ratio, width/3);
        }else{
            height = RENDER_WIDTH;
            width = Math.max(height*ratio, height/3);
        }

        if(vectorData.getRepeatMode() == GraphicElement.RepeatMode.CROP){
            if(ratio > (noScaledSVGPath.getLayoutBounds().getWidth() / noScaledSVGPath.getLayoutBounds().getHeight())){ // Crop Y
                width = RENDER_WIDTH * Math.min(ratio, 1);
                height = (float) (width / (noScaledSVGPath.getLayoutBounds().getWidth() / noScaledSVGPath.getLayoutBounds().getHeight()));
            }else{ // Crop X
                height = RENDER_WIDTH / Math.max(ratio, 1);
                width = (float) (height * (noScaledSVGPath.getLayoutBounds().getWidth() / noScaledSVGPath.getLayoutBounds().getHeight()));
            }
        }

        try{
            if(vectorData.getRepeatMode() == GraphicElement.RepeatMode.MULTIPLY){
                svgPath.setContent(VectorElement.getRepeatedPath(vectorData.getPath(), noScaledSVGPath, width, height, padding,
                        vectorData.isInvertX(), vectorData.isInvertY(), vectorData.getArrowLength(), -1));
            }else{
                svgPath.setContent(VectorElement.getScaledPath(vectorData.getPath(), noScaledSVGPath, width, height, padding,
                        vectorData.isInvertX(), vectorData.isInvertY(), vectorData.getArrowLength(), -1));
            }
    
            if(vectorData.getRepeatMode() == GraphicElement.RepeatMode.CROP){
                if(ratio > (noScaledSVGPath.getLayoutBounds().getWidth() / noScaledSVGPath.getLayoutBounds().getHeight())){ // Crop Y
                    height = width / ratio;
                }else{ // Crop X
                    width = height * ratio;
                }
            }
    
            updateSVGSpecs();
        }catch(PathParseException e){
            Log.e("PathParseException: " + e.getMessage() + " (List Item)");
        }
    }
    public void layoutSVGPath(double displayWidth){
        lastDisplayWidth = displayWidth;
        if(svgPath.getContent().isEmpty()) renderSvgPath();
        updateSVGSpecs();
    
        double padding = 1 + vectorData.getStrokeWidth()/2f / RENDER_WIDTH * displayWidth;
        double clipPadding = 0;
        if(vectorData.getArrowLength() != 0) clipPadding += ((double) vectorData.getArrowLength()) / RENDER_WIDTH * displayWidth;
        displayWidth = displayWidth - padding*2;
        
        // SCALE
        double scale = displayWidth / RENDER_WIDTH;
        svgScale.setX(scale);
        svgScale.setY(scale);
    
        // LAYOUT
        double svgWidth = Math.min(width, RENDER_WIDTH);
        double svgHeight = Math.min(height, RENDER_WIDTH);
        
        double notRectShapeTransformX = (RENDER_WIDTH - svgWidth)/2 * scale;
        double notRectShapeTransformY = (RENDER_WIDTH - svgHeight)/2 * scale;
        
        svgPath.setLayoutX(padding + notRectShapeTransformX);
        svgPath.setLayoutY(padding + notRectShapeTransformY);
        
        // CLIP
        Rectangle clip = new Rectangle(
                (-padding+1)/scale -clipPadding,
                (-padding+1)/scale -clipPadding,
                svgWidth + (2*padding-2)/scale + 2*clipPadding,
                svgHeight + (2*padding-2)/scale + 2*clipPadding
        );
        svgPath.setClip(clip);
    
    
    }
    
    public void updateSVGSpecs(){
        svgPath.setStroke(StyleManager.shiftColorWithTheme(vectorData.getStroke(), .3, .8));
        svgPath.setStrokeWidth(vectorData.getStrokeWidth());
        svgPath.setStrokeMiterLimit(Math.max(1, vectorData.getStrokeWidth()));
        if(vectorData.isDoFill()){
            svgPath.setFill(vectorData.getStrokeWidth() == 0 ? StyleManager.shiftColorWithTheme(vectorData.getFill(), .3, .8) : vectorData.getFill());
        }else{
            svgPath.setFill(null);
        }
        
    }
    
    @Override
    public VectorGridElement clone(){
        return new VectorGridElement(vectorData.clone());
    }
    
    public void resetUseData(){
        vectorData.resetUseData();
    }
    
    public boolean isFake(){
        return fake;
    }
}
