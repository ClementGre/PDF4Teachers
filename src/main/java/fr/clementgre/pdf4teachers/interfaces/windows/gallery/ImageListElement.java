package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageLambdaData;
import javafx.scene.image.Image;

public class ImageListElement extends ImageLambdaData{
    
    private Image image;
    
    public ImageListElement(String imageId){
        super(imageId);
    }
    public ImageListElement(String imageId, Image image){
        super(imageId);
        this.image = image;
    }
    
    public Image getImage(){
        return image;
    }
    
    public void setImage(Image image){
        this.image = image;
    }
}
