package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;

public class ImageData extends ImageLambdaData{
    
    private int width;
    private int height;
    private GraphicElement.RepeatMode repeatMode;
    private GraphicElement.ResizeMode resizeMode;
    private GraphicElement.RotateMode rotateMode;
    private long lastUse;
    private int useCount;
    
    public ImageData(String imageId, int width, int height, GraphicElement.RepeatMode repeatMode, GraphicElement.ResizeMode resizeMode, GraphicElement.RotateMode rotateMode, long lastUse, int useCount){
        super(imageId);
        this.width = width;
        this.height = height;
        this.repeatMode = repeatMode;
        this.resizeMode = resizeMode;
        this.rotateMode = rotateMode;
        this.lastUse = lastUse;
        this.useCount = useCount;
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
    public GraphicElement.RotateMode getRotateMode(){
        return rotateMode;
    }
    public void setRotateMode(GraphicElement.RotateMode rotateMode){
        this.rotateMode = rotateMode;
    }
    public String getImageId(){
        return imageId;
    }
    public void setImageId(String imageId){
        this.imageId = imageId;
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