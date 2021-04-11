package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import javafx.scene.paint.Color;

public class VectorData{
    
    private GraphicElement.RepeatMode repeatMode;
    private GraphicElement.ResizeMode resizeMode;
    private boolean doFill;
    private Color fill;
    private Color stroke;
    private int strokeWidth;
    private String path;
    
    public VectorData(GraphicElement.RepeatMode repeatMode, GraphicElement.ResizeMode resizeMode,
                      boolean doFill, Color fill, Color stroke, int strokeWidth, String path){

        this.repeatMode = repeatMode;
        this.resizeMode = resizeMode;
        this.doFill = doFill;
        this.fill = fill;
        this.stroke = stroke;
        this.strokeWidth = strokeWidth;
        this.path = path;
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
}
