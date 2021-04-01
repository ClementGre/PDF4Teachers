package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageLambdaData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

public class ImageGridElement extends ImageLambdaData{
    
    private final ObjectProperty<Image> image = new SimpleObjectProperty<>(null);
    private boolean rendering = false;
    
    public ImageGridElement(String imageId){
        super(imageId);
    }
    public ImageGridElement(String imageId, Image image){
        super(imageId);
        setImage(image);
    }
    
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
}
