package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

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
    
    public ImageGridElement(String imageId){
        super(imageId);
        loadExifData();
    }
    public ImageGridElement(String imageId, Image image){
        super(imageId);
        setImage(image);
        loadExifData();
    }
    
    public void loadExifData(){
        exifData = new ExifUtils.BasicExifData(new File(imageId));
    }
    
    // SORTER
    
    public int compareTimeWith(ImageGridElement element){
        int value = element.getExifData().getEditDate().compareTo(exifData.getEditDate());
    
        if(value == 0) return compareDirectoryWith(element);
        return value;
    }
    public int compareUseWith(ImageGridElement element){
        int value = 0; // TODO
    
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
}
