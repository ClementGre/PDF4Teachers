package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridCell;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VectorData{
    
    private int width;
    private int height;
    private GraphicElement.RepeatMode repeatMode;
    private GraphicElement.ResizeMode resizeMode;
    private boolean doFill;
    private Color fill;
    private Color stroke;
    private int strokeWidth;
    private String path;
    private boolean invertX;
    private boolean invertY;
    private long lastUse;
    private int useCount;
    
    public VectorData(int width, int height, GraphicElement.RepeatMode repeatMode, GraphicElement.ResizeMode resizeMode,
                      boolean doFill, Color fill, Color stroke, int strokeWidth,
                      String path, boolean invertX, boolean invertY, long lastUse, int useCount){
        
        this.width = width;
        this.height = height;
        this.repeatMode = repeatMode;
        this.resizeMode = resizeMode;
        this.doFill = doFill;
        this.fill = fill;
        this.stroke = stroke;
        this.strokeWidth = strokeWidth;
        this.path = path;
        this.invertX = invertX;
        this.invertY = invertY;
        this.lastUse = lastUse;
        this.useCount = useCount;
    }
    
    public boolean equals(VectorElement element){
        return doFill == element.isDoFill() && strokeWidth == element.getStrokeWidth() && repeatMode == element.getRepeatMode() && resizeMode == element.getResizeMode()
                && fill.toString().equals(element.getFill().toString()) && stroke.toString().equals(element.getStroke().toString()) && path.equals(element.getPath());
    }
    
    public void addToDocument(boolean link){
        PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
        
        VectorElement element = new VectorElement((int) (60 * Element.GRID_WIDTH / page.getWidth()), (int) (page.getMouseY() * Element.GRID_HEIGHT / page.getHeight()), page.getPage(), true,
                width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, link ? this : null);
        
        page.addElement(element, true);
        element.centerOnCoordinatesY();
        MainWindow.mainScreen.setSelected(element);
        
        incrementUsesAndLastUse();
    }
    public void setAsToPlaceElement(boolean link){
        VectorElement element = new VectorElement(0, 0, 0, false, 1, 1,
                repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, link ? this : null);
        
        MainWindow.mainScreen.setToPlace(element);
    }
    
    public void incrementUsesAndLastUse(){
        lastUse = System.currentTimeMillis();
        useCount++;
        VectorGridCell.updateListsSort();
    }
    
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
        data.put("width", width);
        data.put("height", height);
        data.put("repeatMode", getRepeatMode().name());
        data.put("resizeMode", getResizeMode().name());
    
        data.put("doFill", doFill);
        data.put("fill", fill.toString());
        data.put("stroke", stroke.toString());
        data.put("strokeWidth", strokeWidth);
        data.put("path", path);
        data.put("invertX", invertX);
        data.put("invertY", invertY);
        
        data.put("lastUse", lastUse);
        data.put("useCount", useCount);
        
        return data;
    }
    
    public static VectorData readYAMLDataAndGive(HashMap<String, Object> data){
        
        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        GraphicElement.RepeatMode repeatMode = GraphicElement.RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        GraphicElement.ResizeMode resizeMode = GraphicElement.ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        String imageId = Config.getString(data, "imageId");
        int useCount = (int) Config.getLong(data, "useCount");
        long lastUse = Config.getLong(data, "lastUse");
    
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
        
        return new VectorData(width, height, repeatMode, resizeMode, doFill, fill, stroke, strokeWidth, path, invertX, invertY, lastUse, useCount);
    }
    
    public int getWidth(){
        return width;
    }
    public void setWidth(int width){
        this.width = width;
    }
    public int getHeight(){
        return height;
    }
    public void setHeight(int height){
        this.height = height;
    }
    
    public GraphicElement.RepeatMode getRepeatMode(){
        return repeatMode;
    }
    public void setRepeatMode(GraphicElement.RepeatMode repeatMode){
        this.repeatMode = repeatMode;
    }
    public GraphicElement.ResizeMode getResizeMode(){
        return resizeMode;
    }
    public void setResizeMode(GraphicElement.ResizeMode resizeMode){
        this.resizeMode = resizeMode;
    }
    
    public boolean isDoFill(){
        return doFill;
    }
    public void setDoFill(boolean doFill){
        this.doFill = doFill;
    }
    public Color getFill(){
        return fill;
    }
    public void setFill(Color fill){
        this.fill = fill;
    }
    
    public Color getStroke(){
        return stroke;
    }
    public void setStroke(Color stroke){
        this.stroke = stroke;
    }
    public int getStrokeWidth(){
        return strokeWidth;
    }
    public void setStrokeWidth(int strokeWidth){
        this.strokeWidth = strokeWidth;
    }
    
    public String getPath(){
        return path;
    }
    public void setPath(String path){
        this.path = path;
    }
    
    public boolean isInvertX(){
        return invertX;
    }
    public void setInvertX(boolean invertX){
        this.invertX = invertX;
    }
    public boolean isInvertY(){
        return invertY;
    }
    public void setInvertY(boolean invertY){
        this.invertY = invertY;
    }
    
    public long getLastUse(){
        return lastUse;
    }
    public void setLastUse(long lastUse){
        this.lastUse = lastUse;
    }
    public int getUseCount(){
        return useCount;
    }
    public void setUseCount(int useCount){
        this.useCount = useCount;
    }
}
