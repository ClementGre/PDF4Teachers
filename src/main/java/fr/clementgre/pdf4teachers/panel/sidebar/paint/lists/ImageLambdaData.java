/*
 * Copyright (c) 2021-2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.utils.StringUtils;

import java.io.File;
import java.util.Objects;

public class ImageLambdaData { // 2 child : ImageGridElement & ImageData

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

    public ImageData toImageData(){
        if(this instanceof ImageData imageData){
            return imageData;
        }
        return new ImageData(imageId, 0, 0, GraphicElement.RepeatMode.AUTO, GraphicElement.ResizeMode.CORNERS, 0, 0, null);
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
        return StringUtils.removeAfterLastOccurrence(getImageId(), File.separator);
    }
    public String getImageIdFileName(){
        return StringUtils.removeBeforeLastOccurrence(getImageId(), File.separator);
    }
}
