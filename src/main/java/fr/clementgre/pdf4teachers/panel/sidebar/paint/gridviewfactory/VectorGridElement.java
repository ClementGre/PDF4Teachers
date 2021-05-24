package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import fr.clementgre.pdf4teachers.utils.exceptions.PathParseException;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

import java.util.Collections;

public class VectorGridElement{
    
    private SVGPath svgPath = new SVGPath();
    private int lastRenderWidth = 0;
    
    private VectorData vectorData;
    
    public VectorGridElement(VectorData vectorData){
        this.vectorData = vectorData;
        setup();
    }
    
    private void setup(){
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
    }
    
    /*public void toggleFavorite(){
        if(isFavorite()){
            MainWindow.paintTab.favouriteVectors.getList().removeItems(Collections.singletonList(this));
            vectorData = null;
        }else{
            vectorData = new VectorData(0, 0, GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS,
                    false, MainWindow.userData.vectorsLastFill, MainWindow.userData.vectorsLastStroke, (int) MainWindow.userData.vectorsLastStrokeWidth == 0 ? 4 : (int) MainWindow.userData.vectorsLastStrokeWidth,
                    "", false, false, 0, 0);

            MainWindow.paintTab.favouriteVectors.getList().addItems(Collections.singletonList(this));
        }
    }*/
    
    public void addToFavorite(){
        if(!isFavorite()){
            vectorData = new VectorData(0, 0, GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS,
                    false, MainWindow.userData.vectorsLastFill, MainWindow.userData.vectorsLastStroke, (int) MainWindow.userData.vectorsLastStrokeWidth == 0 ? 4 : (int) MainWindow.userData.vectorsLastStrokeWidth,
                    "", false, false, 0, 0);
            
            MainWindow.paintTab.favouriteVectors.getList().addItems(Collections.singletonList(this));
        }
    }
    public void removeFromList(VectorGridView gridView){
        gridView.removeItems(Collections.singletonList(this));
    }
    
    public void addToDocument(boolean link){
        if(MainWindow.mainScreen.hasDocument(false)) vectorData.addToDocument(link);
    }
    public void setAsToPlaceElement(boolean link){
        if(MainWindow.mainScreen.hasDocument(false)) vectorData.setAsToPlaceElement(link);
    }
    
    public boolean isFavorite(){
        return MainWindow.paintTab.favouriteVectors.getList().getAllItems().contains(this);
    }
    
    public boolean equals(VectorElement element){
        return vectorData.equals(element);
    }
    
    // SORTER
    
    public int compareUseWith(VectorGridElement element){
        return element.getVectorData().getUseCount() - vectorData.getUseCount();
    }
    public int compareLastUseTimeWith(VectorGridElement element){
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
    public int getLastRenderWidth(){
        return lastRenderWidth;
    }
    
    // RENDER SVG
    
    public void renderSvgPath(int renderWidth){
        lastRenderWidth = renderWidth;
        SVGPath noScaledSVGPath = new SVGPath();
        noScaledSVGPath.setContent(vectorData.getPath());
        
        float padding = (float) VectorElement.getSVGPadding(vectorData);
        float ratio = ((float) vectorData.getWidth()) / vectorData.getHeight();
        float width, height;
    
        if(ratio > 1){
            width = renderWidth;
            height = Math.max(width/ratio, width/3);
        }else{
            height = renderWidth;
            width = Math.max(height*ratio, height/3);
        }
        
        if(vectorData.getRepeatMode() == GraphicElement.RepeatMode.CROP){
            if(ratio > (noScaledSVGPath.getLayoutBounds().getWidth() / noScaledSVGPath.getLayoutBounds().getHeight())){ // Crop Y
                width = renderWidth * Math.min(ratio, 1);
                height = (float) (width / (noScaledSVGPath.getLayoutBounds().getWidth() / noScaledSVGPath.getLayoutBounds().getHeight()));
                System.out.println("Crop y : width=" + width + ", height=" + height + ", renderWidth=" + renderWidth + ", ratio=" + ratio);
            }else{ // Crop X
                height = renderWidth / Math.min(ratio, 1);
                width = (float) (height * (noScaledSVGPath.getLayoutBounds().getWidth() / noScaledSVGPath.getLayoutBounds().getHeight()));
                System.out.println("Crop x : width=" + width + ", height=" + height + ", renderWidth=" + renderWidth + ", ratio=" + ratio);
            }
        }
        
        try{
            if(vectorData.getRepeatMode() == GraphicElement.RepeatMode.MULTIPLY){
                svgPath.setContent(VectorElement.getRepeatedPath(vectorData.getPath(), noScaledSVGPath, width, height, padding*4, vectorData.isInvertX(), vectorData.isInvertY()));
            }else{
                svgPath.setContent(VectorElement.getScaledPath(vectorData.getPath(), noScaledSVGPath, width, height, padding*4, vectorData.isInvertX(), vectorData.isInvertY()));
            }
    
            updateSVGSpecs();
        }catch(PathParseException e){
            System.err.println(e.getMessage() + " (List Item)");
        }
    }
    
    public void updateSVGSpecs(){
        svgPath.setStroke(vectorData.getStroke());
        svgPath.setStrokeWidth(vectorData.getStrokeWidth());
        svgPath.setStrokeMiterLimit(Math.max(1, vectorData.getStrokeWidth()));
        svgPath.setFill(vectorData.isDoFill() ? vectorData.getFill() : null);
    
        double padding = /*VectorElement.getSVGPadding(vectorData)*/ 0;
        
        double clipPadding = /*VectorElement.getClipPadding(vectorData) == 0 ? 0 : */0;
        Rectangle clip = new Rectangle(-padding - clipPadding, -padding - clipPadding,
                lastRenderWidth + clipPadding*2, lastRenderWidth + clipPadding*2);
        svgPath.setClip(clip);
    
        svgPath.setLayoutX(padding + (lastRenderWidth - Math.min(svgPath.getLayoutBounds().getWidth(), clip.getWidth()))/2);
        svgPath.setLayoutY(padding + (lastRenderWidth - Math.min(svgPath.getLayoutBounds().getHeight(), clip.getHeight()))/2);
    }
}
