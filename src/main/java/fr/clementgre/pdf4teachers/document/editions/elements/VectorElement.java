package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.menus.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorData;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.VectorListPane;
import fr.clementgre.pdf4teachers.utils.exceptions.PathParseException;
import fr.clementgre.pdf4teachers.utils.svg.SVGUtils;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class VectorElement extends GraphicElement{
    
    private final SVGPath noScaledSvgPath = new SVGPath();
    private SVGPath svgPath = new SVGPath();
    
    private final StringProperty path = new SimpleStringProperty();
    private final BooleanProperty doFill = new SimpleBooleanProperty();
    private final ObjectProperty<Color> fill = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> stroke = new SimpleObjectProperty<>();
    private final IntegerProperty strokeWidth = new SimpleIntegerProperty();
    
    private final IntegerProperty arrowLength = new SimpleIntegerProperty(0);
    
    private final BooleanProperty invertX = new SimpleBooleanProperty();
    private final BooleanProperty invertY = new SimpleBooleanProperty();
    
    private final BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    
    private VectorData linkedVectorData;
    
    private Bounds withoutArrowBounds;
    
    public VectorElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode,
                         boolean doFill, Color fill, Color stroke, int strokeWidth, String path, boolean invertX, boolean invertY, int arrowLength){
        this(x, y, pageNumber, hasPage, width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, arrowLength, null);
    }
    public VectorElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode,
                         boolean doFill, Color fill, Color stroke, int strokeWidth, String path, boolean invertX, boolean invertY, int arrowLength, VectorData linkedVectorData){
        super(x, y, pageNumber, width, height, repeatMode, resizeMode);
    
        this.path.set(path);
        this.doFill.set(doFill);
        this.fill.set(fill);
        this.stroke.set(stroke);
        this.strokeWidth.set(strokeWidth);
        this.invertX.set(invertX);
        this.invertY.set(invertY);
        this.arrowLength.set(arrowLength);
        noScaledSvgPath.setContent(getPath());
    
        if(linkedVectorData != null) setLinkedVectorData(linkedVectorData);
        
        if(hasPage && getPage() != null) setupPage(true);
    }
    
    private void setupPage(boolean checkSize){
        updateFill();
        updateStroke();
        onSizeChanged();
    
        widthProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
        heightProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
        
        noScaledSvgPath.setContent(getPath());
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setFillRule(FillRule.NON_ZERO);
        setupGeneral(svgPath);
    
        if(checkSize && getRealWidth() == 0 && getRealHeight() == 0){
            defineSizeAuto();
        }else{
            checkLocation(getRealX() * getPage().getWidth() / GRID_WIDTH, getRealY() * getPage().getHeight() / GRID_HEIGHT,
                    getRealWidth() * getPage().getWidth() / GRID_WIDTH, getRealHeight() * getPage().getHeight() / GRID_HEIGHT, false);
        }
    }
    
    public void initializePage(int pageNumber, double x, double y){
        this.pageNumber = pageNumber;
        
        setupPage(false);
        checkLocation(x, y, false);
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        super.setupBindings();
        
        path.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setPath(newValue);
            noScaledSvgPath.setContent(getPath());
            svgPath.setContent(newValue);
            onSizeChanged();
        });
        
        fill.addListener((observable, oldValue, newValue) ->{
            if(linkedVectorData != null) linkedVectorData.setFill(newValue);
            updateFill();
        });
        doFill.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setDoFill(newValue);
            updateFill();
        });
        
        stroke.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setStroke(newValue);
            updateStroke();
        });
        strokeWidth.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setStrokeWidth(newValue.intValue());
            updateStroke();
            onSizeChanged();
        });
        
        resizeModeProperty().addListener((o, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setResizeMode(newValue);
            onSizeChanged();
        });
        repeatModeProperty().addListener((o, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setRepeatMode(newValue);
            onSizeChanged();
        });
        
        invertXProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setInvertX(newValue);
            onSizeChanged();
        });
        invertYProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setInvertY(newValue);
            onSizeChanged();
        });
        
        realWidthProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setWidth(newValue.intValue());
        });
        realHeightProperty().addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setHeight(newValue.intValue());
        });
        
        arrowLength.addListener((observable, oldValue, newValue) -> {
            if(linkedVectorData != null) linkedVectorData.setArrowLength(newValue.intValue());
            onSizeChanged();
        });
    }
    
    private void updateFill(){
        svgPath.setFill(isDoFill() ? getFill() : null);
    }
    private void updateStroke(){
        svgPath.setStroke(getStroke());
        svgPath.setStrokeWidth(getStrokeWidth());
        svgPath.setStrokeMiterLimit(Math.max(1, getStrokeWidth()));
    }
    
    @Override
    protected void onMouseRelease(){
        super.onMouseRelease();
    }
    
    @Override
    protected void setupMenu(){
        super.setupMenu();
    
        NodeMenuItem addToFavorites = new NodeMenuItem(TR.tr("elementMenu.addToFavouriteList"), false);
        NodeMenuItem addToLast = new NodeMenuItem(TR.tr("elementMenu.addToPreviousList"), false);
    
        addToFavorites.setOnAction((event) -> {
            linkedVectorData = VectorListPane.addFavoriteVector(this);
        });
        addToLast.setOnAction((event) -> {
            linkedVectorData = VectorListPane.addLastVector(this);
        });
    
        menu.getItems().addAll(addToFavorites, addToLast);
    }
    
    private void onSizeChanged(){
        if(getWidth() == 0 && getHeight() == 0) return;
    
        double padding = getSVGPadding();
        svgPath.setLayoutX(padding);
        svgPath.setLayoutY(padding);
        
        try{
            if(getRepeatMode() == GraphicElement.RepeatMode.CROP){
                if(getLayoutBounds().getWidth() > getLayoutBounds().getHeight()*getRatio()){ // Crop Y
                    svgPath.setContent(getScaledPath((float) getLayoutBounds().getWidth(), (float) (getLayoutBounds().getWidth()/getRatio()), (float) padding));
                }else{ // Crop X
                    svgPath.setContent(getScaledPath((float) (getLayoutBounds().getHeight()*getRatio()), (float) getLayoutBounds().getHeight(), (float) padding));
                }
            }else if(getRepeatMode() == RepeatMode.MULTIPLY){
                svgPath.setContent(getRepeatedPath((float) getLayoutBounds().getWidth(), (float) getLayoutBounds().getHeight(), (float) padding));
            }else{
                svgPath.setContent(getScaledPath((float) getLayoutBounds().getWidth(), (float) getLayoutBounds().getHeight(), (float) padding));
            }
        }catch(PathParseException e){
            System.err.println(e.getMessage());
        }
        
        svgPath.setClip(new Rectangle(-padding - getClipPadding(), -padding - getClipPadding(),
                getLayoutBounds().getWidth() + getClipPadding()*2, getLayoutBounds().getHeight() + getClipPadding()*2));
        
    }
    
    public String getScaledPath(float wantedWidth, float wantedHeight, float padding) throws PathParseException{
        return getScaledPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), arrowLength.get());
    }
    public String getScaledPathScaled(float wantedWidth, float wantedHeight, float padding, double pageWidth) throws PathParseException{
        return getScaledPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), (float) getArrowLengthScaled(pageWidth));
    }
    public static String getScaledPath(String path, SVGPath sourceSVG, float wantedWidth, float wantedHeight, float padding, boolean invertX, boolean invertY, float arrowLength) throws PathParseException{
        double finalCurrentWidth = Math.max(1d, sourceSVG.getLayoutBounds().getWidth());
        double finalCurrentHeight = Math.max(1d, sourceSVG.getLayoutBounds().getHeight());
        
        String scaledPath = "M0 0 " + SVGUtils.transformPath(path,
                (float) ((wantedWidth-padding*2) / finalCurrentWidth),
                (float) ((wantedHeight-padding*2) / finalCurrentHeight),
                (float) -sourceSVG.getBoundsInLocal().getMinX(),
                (float) -sourceSVG.getBoundsInLocal().getMinY(),
                invertX, invertY,
                (float) finalCurrentWidth,
                (float) finalCurrentHeight);
        
        if(arrowLength > 0){
            return SVGUtils.addArrowsToPath(scaledPath, arrowLength);
        }else{
            return scaledPath;
        }
    }
    public static record ScaledVectorInfo(String path, Bounds withoutArrowBounds){}
    
    public String getRepeatedPath(float wantedWidth, float wantedHeight, float padding) throws PathParseException{
        return getRepeatedPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), getArrowLength());
    }
    public String getRepeatedPathScaled(float wantedWidth, float wantedHeight, float padding, double pageWidth) throws PathParseException{
        return getRepeatedPath(getPath(), noScaledSvgPath, wantedWidth, wantedHeight, padding, isInvertX(), isInvertY(), (float) getArrowLengthScaled(pageWidth));
    }
    public static String getRepeatedPath(String path, SVGPath sourceSVG, float wantedWidth, float wantedHeight, float padding, boolean invertX, boolean invertY, float arrowLength) throws PathParseException{
        double finalCurrentWidth = Math.max(1d, sourceSVG.getLayoutBounds().getWidth());
        double finalCurrentHeight = Math.max(1d, sourceSVG.getLayoutBounds().getHeight());
        double ratio = sourceSVG.getLayoutBounds().getWidth() / sourceSVG.getLayoutBounds().getHeight();
        
        StringBuilder newPath = new StringBuilder("M0 0 ");
        
        if(wantedWidth > wantedHeight*ratio){ // Multiply X
    
            float unitWidth = (float) ((wantedHeight - padding * 2) * ratio);
            float startX = 0;
            while(startX < wantedWidth && unitWidth > 0){
                newPath.append(SVGUtils.transformPath(path,
                        (float) ((unitWidth) / finalCurrentWidth),
                        (float) ((wantedHeight - padding * 2) / finalCurrentHeight),
                        (float) (-sourceSVG.getBoundsInLocal().getMinX() + (invertX ? -1 : 1) * startX/(wantedHeight - padding * 2)*finalCurrentHeight),
                        (float) -sourceSVG.getBoundsInLocal().getMinY(),
                        invertX, invertY,
                        (float) finalCurrentWidth,
                        (float) finalCurrentHeight));
    
                startX += unitWidth;
            }
            
        }else{ // Multiply Y
            
            float unitHeight = (float) ((wantedWidth - padding * 2) / ratio);
            float startY = 0;
            while(startY < wantedHeight && unitHeight > 0){
                newPath.append(SVGUtils.transformPath(path,
                        (float) ((wantedWidth - padding * 2) / finalCurrentWidth),
                        (float) ((unitHeight) / finalCurrentHeight),
                        (float) -sourceSVG.getBoundsInLocal().getMinX(),
                        (float) (-sourceSVG.getBoundsInLocal().getMinY() + (invertY ? -1 : 1) * startY/(wantedWidth - padding * 2)*finalCurrentWidth),
                        invertX, invertY,
                        (float) finalCurrentWidth,
                        (float) finalCurrentHeight));
        
                startY += unitHeight;
            }
            
        }
        if(arrowLength > 0){
            return SVGUtils.addArrowsToPath(newPath.toString(), arrowLength);
        }
        return newPath.toString();
    }
    
    public double getSVGPadding(){
        return getResizeMode() == ResizeMode.OPPOSITE_CORNERS ? 0 : getStrokeWidth()/2d;
    }
    public static double getSVGPadding(VectorData vectorData){
        return vectorData.getResizeMode() == ResizeMode.OPPOSITE_CORNERS ? 0 : vectorData.getStrokeWidth()/2d;
    }
    public double getSVGPaddingScaled(double pageWidth){
        return getResizeMode() == ResizeMode.OPPOSITE_CORNERS ? 0 : getStrokeWidthScaled(pageWidth)/2d;
    }
    public double getClipPadding(){
        if(getRepeatMode() == GraphicElement.RepeatMode.CROP || getRepeatMode() == RepeatMode.MULTIPLY){
            return 0;
        }else{
            double padding = getStrokeWidth();
            if(getArrowLength() != 0){ // Arrows need special clip padding since the forks can overflow the element region.
                padding += getArrowLength();
            }
            return padding;
        }
    }
    public static double getClipPadding(VectorData vectorData){
        if(vectorData.getRepeatMode() == GraphicElement.RepeatMode.CROP || vectorData.getRepeatMode() == RepeatMode.MULTIPLY){
            return 0;
        }else{
            double padding = vectorData.getStrokeWidth();
            if(vectorData.getArrowLength() != 0){ // Arrows need special clip padding since the forks can overflow the element region.
                padding += vectorData.getArrowLength();
            }
            return padding;
        }
    }
    public double getClipPaddingScaled(double pageWidth){
        if(getRepeatMode() == GraphicElement.RepeatMode.CROP || getRepeatMode() == RepeatMode.MULTIPLY){
            return 0;
        }else{
            double padding = getStrokeWidthScaled(pageWidth);
            if(getArrowLength() != 0){ // Arrows need special clip padding since the forks can overflow the element region.
                padding += getArrowLengthScaled(pageWidth);
            }
            return padding;
        }
    }
    
    // ACTIONS
    @Override
    public void select(){
        super.select();
        
    }
    
    @Override
    public void addedToDocument(boolean markAsUnsave){
    
    }
    
    @Override
    public void removedFromDocument(boolean markAsUnsave){
        super.removedFromDocument(markAsUnsave);
        prefHeightProperty().unbind();
        prefWidthProperty().unbind();
    }
    
    // READER AND WRITERS
    
    @Override
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = super.getYAMLPartialData();
        data.put("doFill", doFill.get());
        data.put("fill", fill.get().toString());
        data.put("stroke", stroke.get().toString());
        data.put("strokeWidth", strokeWidth.get());
        data.put("path", path.get());
        data.put("invertX", invertX.get());
        data.put("invertY", invertY.get());
        data.put("arrowLength", arrowLength.get());
        
        return data;
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page){
        VectorElement element = readYAMLDataAndGive(data, true, page);
        if(MainWindow.mainScreen.document.getPagesNumber() > element.getPageNumber())
            MainWindow.mainScreen.document.getPage(element.getPageNumber()).addElement(element, false);
    }
    
    public static VectorElement readYAMLDataAndGive(HashMap<String, Object> data, boolean hasPage, int page){
        
        int x = (int) Config.getLong(data, "x");
        int y = (int) Config.getLong(data, "y");
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        boolean doFill = Config.getBoolean(data, "doFill");
        Color fill = Color.DARKGRAY;
        Color stroke = Color.BLACK;
        try{
            fill = Color.valueOf(Config.getString(data, "fill"));
            stroke = Color.valueOf(Config.getString(data, "stroke"));
        }catch(IllegalArgumentException e){
            System.err.println("Error: Unable to parse VectorElement color: " + e.getMessage());
        }
        int strokeWidth = (int) Config.getLong(data, "strokeWidth");
        String path = Config.getString(data, "path");
    
        boolean invertX = Config.getBoolean(data, "invertX");
        boolean invertY = Config.getBoolean(data, "invertY");
        
        int arrowLength = (int) Config.getLong(data, "arrowLength");
        
        RepeatMode repeatMode = RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        ResizeMode resizeMode = ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        
        return new VectorElement(x, y, page, hasPage, width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, arrowLength);
    }
    
    // SPECIFIC METHODS
    
    
    @Override
    public void incrementUsesAndLastUse(){
    
    }
    
    @Override
    public double getRatio(){
        return noScaledSvgPath.getLayoutBounds().getWidth() / noScaledSvgPath.getLayoutBounds().getHeight();
    }
    
    @Override
    public void defineSizeAuto(){
        double imgWidth = noScaledSvgPath.getLayoutBounds().getWidth();
        double imgHeight = noScaledSvgPath.getLayoutBounds().getHeight();
        double width = Math.min(getPage().getWidth()/3, imgWidth);
        double height = imgHeight * width/imgWidth;
    
        checkLocation(getRealX() * getPage().getWidth() / GRID_WIDTH, getRealY() * getPage().getHeight() / GRID_HEIGHT,
                width, height, false);
    }
    
    @Override
    public float getAlwaysHeight(){
        return 0;
    }
    
    @Override
    public Element clone(){
        return new VectorElement(getRealX(), getRealY(), getPageNumber(), true, getRealWidth(), getRealHeight(), getRepeatMode(), getResizeMode(),
                isDoFill(), getFill(), getStroke(), getStrokeWidth(), getPath(), isInvertX(), isInvertY(), getArrowLength());
    }

    public void setLinkedVectorData(VectorData vectorData){
        this.linkedVectorData = vectorData;
    }
    
    // GETTER / SETTER
    
    public SVGPath getSvgPath(){
        return svgPath;
    }
    public void setSvgPath(SVGPath svgPath){
        this.svgPath = svgPath;
    }
    public String getPath(){
        return path.get();
    }
    public StringProperty pathProperty(){
        return path;
    }
    public void setPath(String path){
        this.path.set(path);
    }
    public boolean isDoFill(){
        return doFill.get();
    }
    public BooleanProperty doFillProperty(){
        return doFill;
    }
    public void setDoFill(boolean doFill){
        this.doFill.set(doFill);
    }
    public Color getFill(){
        return fill.get();
    }
    public ObjectProperty<Color> fillProperty(){
        return fill;
    }
    public void setFill(Color fill){
        this.fill.set(fill);
    }
    public Color getStroke(){
        return stroke.get();
    }
    public ObjectProperty<Color> strokeProperty(){
        return stroke;
    }
    public void setStroke(Color stroke){
        this.stroke.set(stroke);
    }
    public int getStrokeWidth(){
        return strokeWidth.get();
    }
    public double getStrokeWidthScaled(double pageWidth){
        return ((double) strokeWidth.get()) / PageRenderer.PAGE_WIDTH * pageWidth;
    }
    public IntegerProperty strokeWidthProperty(){
        return strokeWidth;
    }
    public void setStrokeWidth(int strokeWidth){
        this.strokeWidth.set(strokeWidth);
    }
    public boolean isInvertX(){
        return invertX.get();
    }
    public BooleanProperty invertXProperty(){
        return invertX;
    }
    public void setInvertX(boolean invertX){
        this.invertX.set(invertX);
    }
    public boolean isInvertY(){
        return invertY.get();
    }
    public BooleanProperty invertYProperty(){
        return invertY;
    }
    public void setInvertY(boolean invertY){
        this.invertY.set(invertY);
    }
    public boolean isIsEditMode(){
        return isEditMode.get();
    }
    public BooleanProperty isEditModeProperty(){
        return isEditMode;
    }
    public void setIsEditMode(boolean isEditMode){
        this.isEditMode.set(isEditMode);
    }
    public SVGPath getNoScaledSvgPath(){
        return noScaledSvgPath;
    }
    public int getArrowLength(){
        return arrowLength.get();
    }
    public double getArrowLengthScaled(double pageWidth){
        return ((double) arrowLength.get()) / PageRenderer.PAGE_WIDTH * pageWidth;
    }
    public IntegerProperty arrowLengthProperty(){
        return arrowLength;
    }
    public void setArrowLength(int arrowLength){
        this.arrowLength.set(arrowLength);
    }
    
    public void invertInversions(){
        setInvertX(!isInvertX());
        setInvertY(!isInvertY());
    }
}
