package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.svg.SVGUtils;
import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VectorElement extends GraphicElement{
    
    private SVGPath noScaledSvgPath = new SVGPath();
    private SVGPath svgPath = new SVGPath();
    
    private StringProperty path = new SimpleStringProperty();
    private BooleanProperty doFill = new SimpleBooleanProperty();
    private ObjectProperty<Color> fill = new SimpleObjectProperty<>();
    private ObjectProperty<Color> stroke = new SimpleObjectProperty<>();
    private IntegerProperty strokeWidth = new SimpleIntegerProperty();
    
    private BooleanProperty invertX = new SimpleBooleanProperty();
    private BooleanProperty invertY = new SimpleBooleanProperty();
    
    private BooleanProperty isEditMode = new SimpleBooleanProperty(false);
    
    public VectorElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode,
                         boolean doFill, Color fill, Color stroke, int strokeWidth, String path, boolean invertX, boolean invertY){
        super(x, y, pageNumber, hasPage, width, height, repeatMode, resizeMode);
    
        this.path.set(path);
        this.doFill.set(doFill);
        this.fill.set(fill);
        this.stroke.set(stroke);
        this.strokeWidth.set(strokeWidth);
        this.invertX.set(invertX);
        this.invertY.set(invertY);
        noScaledSvgPath.setContent(getPath());
        
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
            noScaledSvgPath.setContent(getPath());
            svgPath.setContent(newValue);
            onSizeChanged();
        });
        
        fill.addListener((observable, oldValue, newValue) -> updateFill());
        doFill.addListener((observable, oldValue, newValue) -> updateFill());
        
        stroke.addListener((observable, oldValue, newValue) -> updateStroke());
        strokeWidth.addListener((observable, oldValue, newValue) -> {
            updateStroke();
            onSizeChanged();
        });
        
        resizeModeProperty().addListener((o, oldValue, newValue) -> onSizeChanged());
        
        invertXProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
        invertYProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
    }
    
    private void updateFill(){
        if(isDoFill()){
            svgPath.setFill(getFill());
        }
        else{
            svgPath.setFill(null);
        }
    }
    private void updateStroke(){
        svgPath.setStroke(getStroke());
        svgPath.setStrokeWidth(getStrokeWidth());
        svgPath.setStrokeMiterLimit(getStrokeWidth());
    }
    
    @Override
    protected void onMouseRelease(){
        super.onMouseRelease();
    }
    
    @Override
    protected void setupMenu(){
        super.setupMenu();
    }
    
    private void onSizeChanged(){
        if(getWidth() == 0 || getHeight() == 0) return;
    
        double padding = getSVGPadding();
        svgPath.setLayoutX(padding);
        svgPath.setLayoutY(padding);
        svgPath.setContent(getScaledPath((float) getLayoutBounds().getWidth(), (float) getLayoutBounds().getHeight(), (float) padding));
    }
    
    public String getScaledPath(float wantedWidth, float wantedHeight, float padding){
        double finalCurrentWidth = Math.max(1d, noScaledSvgPath.getLayoutBounds().getWidth());
        double finalCurrentHeight = Math.max(1d, noScaledSvgPath.getLayoutBounds().getHeight());
        
        return SVGUtils.transformPath(getPath(),
                (float) ((wantedWidth-padding*2) / finalCurrentWidth),
                (float) ((wantedHeight-padding*2) / finalCurrentHeight),
                (float) -noScaledSvgPath.getBoundsInLocal().getMinX(),
                (float) -noScaledSvgPath.getBoundsInLocal().getMinY(),
                isInvertX(), isInvertY(),
                (float) finalCurrentWidth,
                (float) finalCurrentHeight);
    }
    
    public double getSVGPadding(){
        return STROKE_DEFAULT.getWidths().getTop() + (getResizeMode() == ResizeMode.OPPOSITE_CORNERS ? 0 : getStrokeWidth()/2d);
    }
    
    // ACTIONS
    
    @Override
    public void select(){
        super.selectPartial();
        
    }
    
    @Override
    public void addedToDocument(boolean silent){
    
    }
    
    @Override
    public void removedFromDocument(boolean silent){
    
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
        
        return data;
    }
    
    public static void readYAMLDataAndCreate(HashMap<String, Object> data, int page){
        VectorElement element = readYAMLDataAndGive(data, true, page);
        if(MainWindow.mainScreen.document.pages.size() > element.getPageNumber())
            MainWindow.mainScreen.document.pages.get(element.getPageNumber()).addElement(element, false);
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
        
        RepeatMode repeatMode = RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        ResizeMode resizeMode = ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        
        return new VectorElement(x, y, page, hasPage, width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY);
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
                isDoFill(), getFill(), getStroke(), getStrokeWidth(), getPath(), isInvertX(), isInvertY());
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
}
