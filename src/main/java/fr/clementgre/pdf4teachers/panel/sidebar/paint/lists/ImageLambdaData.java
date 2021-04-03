package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.utils.StringUtils;

import java.io.File;
import java.util.Objects;

public class ImageLambdaData{

    protected String imageId;
    
    public ImageLambdaData(String imageId){
        this.imageId = imageId;
    }
    
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ImageLambdaData that = (ImageLambdaData) o;
        return Objects.equals(imageId, that.imageId);
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(imageId);
    }
    
    public String getImageId(){
        return imageId;
    }
    public void setImageId(String imageId){
        this.imageId = imageId;
    }
    
    public String getImageIdDirectory(){
        return StringUtils.removeAfterLastRegex(getImageId(), File.separator);
    }
    public String getImageIdFileName(){
        return StringUtils.removeBeforeLastRegex(getImageId(), File.separator);
    }
}
