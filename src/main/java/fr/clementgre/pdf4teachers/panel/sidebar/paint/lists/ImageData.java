package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridCell;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ImageData extends ImageLambdaData{
    
    private int width;
    private int height;
    private GraphicElement.RepeatMode repeatMode;
    private GraphicElement.ResizeMode resizeMode;
    private long lastUse;
    private int useCount;
    
    public ImageData(String imageId, int width, int height, GraphicElement.RepeatMode repeatMode, GraphicElement.ResizeMode resizeMode, long lastUse, int useCount){
        super(imageId);
        this.width = width;
        this.height = height;
        this.repeatMode = repeatMode;
        this.resizeMode = resizeMode;
        this.lastUse = lastUse;
        this.useCount = useCount;
    }
    
    public ImageElement addToDocument(boolean link){
        ImageData linkedImage = link ? this : null;
        PageRenderer page = MainWindow.mainScreen.document.getLastCursorOverPageObject();
        
        ImageElement element = new ImageElement(page.getNewElementXOnGrid(true), page.getNewElementYOnGrid(), page.getPage(), true,
                width, height, repeatMode, resizeMode, imageId, linkedImage);
    
        page.addElement(element, true, UType.UNDO);
        element.centerOnCoordinatesY();
        MainWindow.mainScreen.setSelected(element);
        Main.window.requestFocus();
        
        incrementUsesAndLastUse();
        return element;
    }
    public void setAsToPlaceElement(boolean link){
        ImageData linkedImage = link ? this : null;
        ImageElement element = new ImageElement(0, 0, 0, false, 1, 1, repeatMode, resizeMode, imageId, linkedImage);
        element.updateImage(true);
        
        MainWindow.mainScreen.setToPlace(element);
        PlatformUtils.runLaterOnUIThread(200, () -> { // Allow the time to double click if wanted
            Main.window.requestFocus();
        });
    }
    
    public void incrementUsesAndLastUse(){
        lastUse = System.currentTimeMillis();
        useCount++;
        ImageGridCell.updateGalleryAndFavoritesSort();
    }
    
    public LinkedHashMap<Object, Object> getYAMLData(){
        LinkedHashMap<Object, Object> data = new LinkedHashMap<>();
        data.put("width", width);
        data.put("height", height);
        data.put("repeatMode", getRepeatMode().name());
        data.put("resizeMode", getResizeMode().name());
        data.put("imageId", getImageId());
        data.put("lastUse", lastUse);
        data.put("useCount", useCount);
        
        return data;
    }
    
    public static ImageData readYAMLDataAndGive(HashMap<String, Object> data){

        int width = (int) Config.getLong(data, "width");
        int height = (int) Config.getLong(data, "height");
        GraphicElement.RepeatMode repeatMode = GraphicElement.RepeatMode.valueOf(Config.getString(data, "repeatMode"));
        GraphicElement.ResizeMode resizeMode = GraphicElement.ResizeMode.valueOf(Config.getString(data, "resizeMode"));
        String imageId = Config.getString(data, "imageId");
        int useCount = (int) Config.getLong(data, "useCount");
        long lastUse = Config.getLong(data, "lastUse");
        
        return new ImageData(imageId, width, height, repeatMode, resizeMode, lastUse, useCount);
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
    
    public void resetUseData(){
        lastUse = System.currentTimeMillis();
        useCount = 0;
    }
}
