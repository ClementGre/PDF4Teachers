package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageLambdaData;
import fr.clementgre.pdf4teachers.utils.image.ExifUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.File;

public class ImageGridElement extends ImageLambdaData{
    
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(null);
    private boolean rendering = false;
    private ExifUtils.BasicExifData exifData;
    
    private ImageData linkedImageData;
    
    public ImageGridElement(String imageId){
        super(imageId);
        loadExifData();
        setup();
    }
    public ImageGridElement(String imageId, ImageData linkedImageData){
        super(imageId);
        this.linkedImageData = linkedImageData;
        loadExifData();
        setup();
    }
    public ImageGridElement(String imageId, ImageData linkedImageData, Image image){
        super(imageId);
        this.linkedImageData = linkedImageData;
        setImage(image);
        loadExifData();
        setup();
    }
    public ImageGridElement(String imageId, Image image){
        super(imageId);
        setImage(image);
        loadExifData();
        setup();
    }
    
    public void loadExifData(){
        exifData = new ExifUtils.BasicExifData(new File(imageId));
    }
    
    private void setup(){
    
    }
    
    public void toggleFavorite(){
        // TODO : ADD/REMOVE item to favourites and update linkedData
    }
    public void addToDocument(){
        toImageData().addToDocument();
    }
    
    // SORTER
    
    public int compareTimeWith(ImageGridElement element){
        int value = element.getExifData().getEditDate().compareTo(exifData.getEditDate());
    
        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareUseWith(ImageGridElement element){
        int value = 0;
        if(hasLinkedImageData()){
            if(element.hasLinkedImageData()){
                value = getLinkedImageData().getUseCount() - element.getLinkedImageData().getUseCount();
            }
        }
    
        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareLastUseTimeWith(ImageGridElement element){
        int value = 0;
        if(hasLinkedImageData()){
            if(element.hasLinkedImageData()){
                value = (int) (getLinkedImageData().getLastUse() - element.getLinkedImageData().getLastUse());
            }
        }
        
        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareSizeWith(ImageGridElement element){
        int value = Long.compare(element.getExifData().getSize(), exifData.getSize());
    
        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareDirectoryWith(ImageGridElement element){
        int value = getImageIdDirectory().compareTo(element.getImageIdDirectory());
    
        if(value == 0) value = getImageIdFileName().compareTo(element.getImageIdFileName());
        return value;
    }
    public int compareNameWith(ImageGridElement element){
        int value = getImageIdFileName().compareTo(element.getImageIdFileName());
        
        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    
    
    
    // GETTERS / SETTERS
    
    public Image getImage(){
        return image.get();
    }
    public ObjectProperty<Image> imageProperty(){
        return image;
    }
    public void setImage(Image image){
        this.image.set(image);
    }
    public void setRendering(boolean rendering){
        this.rendering = rendering;
    }
    public boolean isRendering(){
        return rendering;
    }
    public ExifUtils.BasicExifData getExifData(){
        return exifData;
    }
    public void setExifData(ExifUtils.BasicExifData exifData){
        this.exifData = exifData;
    }
    public ImageData getLinkedImageData(){
        return linkedImageData;
    }
    public boolean hasLinkedImageData(){
        return linkedImageData != null;
    }
    public void setLinkedImageData(ImageData linkedImageData){
        this.linkedImageData = linkedImageData;
    }
}
