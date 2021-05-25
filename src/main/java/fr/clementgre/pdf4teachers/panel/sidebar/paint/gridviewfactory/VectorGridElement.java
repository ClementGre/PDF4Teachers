package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
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
    
    private SVGPath svgPath = new SVGPath();
    private Scale svgScale = new Scale();
    private double lastDisplayWidth = 0;
    
    private VectorData vectorData;
    
    private static final int RENDER_WIDTH = 75;
    float width, height = 1;
    
    public VectorGridElement(VectorData vectorData){
        this.vectorData = vectorData;
        setup();
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
                svgPath.setContent(VectorElement.getRepeatedPath(vectorData.getPath(), noScaledSVGPath, width, height, padding, vectorData.isInvertX(), vectorData.isInvertY()));
            }else{
                svgPath.setContent(VectorElement.getScaledPath(vectorData.getPath(), noScaledSVGPath, width, height, padding, vectorData.isInvertX(), vectorData.isInvertY()));
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
            System.err.println(e.getMessage() + " (List Item)");
        }
    }
    public void layoutSVGPath(double displayWidth){
        lastDisplayWidth = displayWidth;
        if(svgPath.getContent().isEmpty()) renderSvgPath();
        updateSVGSpecs();
    
        double padding = 1 + vectorData.getStrokeWidth()/2f / RENDER_WIDTH * displayWidth;
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
        Rectangle clip = new Rectangle((-padding+1)/scale, (-padding+1)/scale,
                svgWidth + (2*padding-2)/scale, svgHeight + (2*padding-2)/scale);
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
}
