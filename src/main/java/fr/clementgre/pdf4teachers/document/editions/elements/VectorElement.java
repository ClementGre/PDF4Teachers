package fr.clementgre.pdf4teachers.document.editions.elements;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.image.SVGUtils;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Effect;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VectorElement extends GraphicElement{
    
    private Region region = new Region();
    private SVGPath svgPath = new SVGPath();
    
    private StringProperty path = new SimpleStringProperty();
    private BooleanProperty doFill = new SimpleBooleanProperty();
    private ObjectProperty<Color> fill = new SimpleObjectProperty<>();
    private ObjectProperty<Color> stroke = new SimpleObjectProperty<>();
    private IntegerProperty strokeWidth = new SimpleIntegerProperty();
    
    public VectorElement(int x, int y, int pageNumber, boolean hasPage, int width, int height, RepeatMode repeatMode, ResizeMode resizeMode,
                         boolean doFill, Color fill, Color stroke, int strokeWidth, String path){
        super(x, y, pageNumber, hasPage, width, height, repeatMode, resizeMode);
    
        this.path.set(path);
        this.doFill.set(doFill);
        this.fill.set(fill);
        this.stroke.set(stroke);
        this.strokeWidth.set(strokeWidth);
        
        if(hasPage && getPage() != null){
            updateFill();
            updateStroke();
            svgPath.setContent(getPath());
    
            widthProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
            heightProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
            
            region.setShape(svgPath);
            setupGeneral(region);
        }
    }
    
    public void initializePage(int pageNumber, double x, double y){
        this.pageNumber = pageNumber;
        
        updateFill();
        updateStroke();
        svgPath.setContent(getPath());
    
        widthProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
        heightProperty().addListener((observable, oldValue, newValue) -> onSizeChanged());
    
        region.setShape(svgPath);
        setupGeneral(region);
        checkLocation(x, y, false);
    }
    
    // SETUP / EVENT CALL BACK
    
    @Override
    protected void setupBindings(){
        super.setupBindings();
    
        path.addListener((observable, oldValue, newValue) -> {
            svgPath.setContent(newValue);
        });
        
        fill.addListener((observable, oldValue, newValue) -> updateFill());
        doFill.addListener((observable, oldValue, newValue) -> updateFill());
        
        stroke.addListener((observable, oldValue, newValue) -> updateStroke());
        strokeWidth.addListener((observable, oldValue, newValue) -> updateStroke());
        
        region.prefWidthProperty().bind(widthProperty());
        region.prefHeightProperty().bind(heightProperty());
    }
    
    private void updateFill(){
        if(isDoFill()){
            svgPath.setFill(getFill());
            region.setBackground(new Background(new BackgroundFill(getFill(), CornerRadii.EMPTY, Insets.EMPTY)));
        }
        else{
            svgPath.setFill(null);
            region.setBackground(null);
        }
    }
    private void updateStroke(){
        svgPath.setStroke(getStroke());
        svgPath.setStrokeWidth(getStrokeWidth());
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        if(getStrokeWidth() == 0) region.setBorder(null);
        else region.setBorder(new Border(new BorderStroke(getStroke(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(getStrokeWidth()))));
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
//        System.out.println("FACTORS : ");
//        System.out.println(getWidth() + " / " + svgPath.getLayoutBounds().getWidth() + " --> " + getWidth() / svgPath.getLayoutBounds().getWidth());
//        System.out.println(getHeight() + " / " + svgPath.getLayoutBounds().getHeight() + " --> " + getHeight() / svgPath.getLayoutBounds().getHeight());
//        System.out.println("----------------------");
        //svgPath.setContent(SVGUtils.scalePath(getPath(), getLayoutBounds().getWidth() / svgPath.getLayoutBounds().getWidth(), getHeight() / svgPath.getLayoutBounds().getHeight()));
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
    
        RepeatMode repeatMode = RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        ResizeMode resizeMode = ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        
        return new VectorElement(x, y, page, hasPage, width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path);
    }
    
    // SPECIFIC METHODS
    
    
    @Override
    public void incrementUsesAndLastUse(){
    
    }
    
    @Override
    public double getRatio(){
        return 1;
    }
    
    @Override
    public void defineSizeAuto(){
    
    }
    
    @Override
    public float getAlwaysHeight(){
        return 0;
    }
    
    @Override
    public Element clone(){
        return null;
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
}
