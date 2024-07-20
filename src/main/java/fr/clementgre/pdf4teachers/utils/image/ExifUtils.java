/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.FilesUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class ExifUtils {
    
    private final Metadata metadata;
    
    private final File file;
    public ExifUtils(File file) throws ImageProcessingException, IOException{
        this.file = file;
        metadata = ImageMetadataReader.readMetadata(file);
    }
    
    /*public static void logImageExif(File file){
        Platform.runLater(() -> {
            Metadata metadata = null;
            try{
                metadata = ImageMetadataReader.readMetadata(file);
            }catch(ImageProcessingException | IOException e){
                Log.e(e);
            }
            Log.d(file.getName());
            for(Directory directory : metadata.getDirectories()){
                for(Tag tag : directory.getTags()){
                    if(tag.getTagName().toLowerCase().contains("rotate")
                    || tag.getTagName().toLowerCase().contains("date")
                    || tag.getDirectoryName().toLowerCase().contains("file")){
                        Log.d("[" + directory.getName() + "] - " + tag.getTagName() + " = " + tag.getDescription());
                    }
                    
                }
                if(directory.hasErrors()){
                    for(String error : directory.getErrors()){
                        System.err.format("ERROR: %s", error);
                        Log.b();
                    }
                }
            }
    
            Log.b();
            Log.t("--------------------------------------------");
            Log.b();
            
        });
    }*/
    
    public ImageTransform getImageExifRotation() throws MetadataException{
        ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if(exifIFD0Directory != null){
            if(exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)){
                int rotation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                return convertOrientationToTransform(rotation);
            }
        }
        return ImageTransform.NONE;
    }
    
    public Date getImageExifEditDate() throws MetadataException, IOException{
        
        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if(directory != null){
            if(directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)){
                return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            }
        }
        
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        if(attr != null && attr.lastModifiedTime() != null){
            return new Date(attr.lastModifiedTime().toMillis());
        }
        return new Date(0);
        
    }
    public long getImageSize(){
        return FilesUtils.getSize(file.toPath());
    }
    
    public static ImageTransform convertOrientationToTransform(int orientationInt){
        return switch(orientationInt){
            case 2 -> new ImageTransform(true, 0);
            case 3 -> new ImageTransform(false, 180);
            case 4 -> new ImageTransform(true, 180);
            case 5 -> new ImageTransform(true, 270);
            case 6 -> new ImageTransform(false, 90);
            case 7 -> new ImageTransform(true, 90);
            case 8 -> new ImageTransform(false, 270);
            default -> ImageTransform.NONE;
        };
    }
    
    public static class ImageTransform {
        public static final ImageTransform NONE = new ImageTransform(false, 0);
        private boolean flip;
        private int rotateAngle;
        
        public void applyTransformToGraphics2D(Graphics2D g, int width, int height){
            if(rotateAngle != 0)
                g.rotate(Math.toRadians(rotateAngle), width / 2d, height / 2d);
        }
        
        public ImageTransform(boolean flip, int rotateAngle){
            this.flip = flip;
            this.rotateAngle = rotateAngle;
        }
        public boolean isFlip(){
            return flip;
        }
        public void setFlip(boolean flip){
            this.flip = flip;
        }
        public int getRotateAngle(){
            return rotateAngle;
        }
        public void setRotateAngle(int rotateAngle){
            this.rotateAngle = rotateAngle;
        }
        
    }
    
    public static class BasicExifData {
        private long size;
        private Date editDate = new Date();
        private ImageTransform rotation = ImageTransform.NONE;
        
        public BasicExifData(int size, Date editDate, ImageTransform rotation){
            this.size = size;
            this.editDate = editDate;
            this.rotation = rotation;
        }
        
        public BasicExifData(File file){
            try{
                ExifUtils utils = new ExifUtils(file);
                size = utils.getImageSize();
                editDate = utils.getImageExifEditDate();
                rotation = utils.getImageExifRotation();
            }catch(ImageProcessingException | IOException | MetadataException e){
                Log.eNotified(e);
            }
        }
        
        public long getSize(){
            return size;
        }
        public void setSize(long size){
            this.size = size;
        }
        public Date getEditDate(){
            return editDate;
        }
        public void setEditDate(Date editDate){
            this.editDate = editDate;
        }
        public ImageTransform getRotation(){
            return rotation;
        }
        public void setRotation(ImageTransform rotation){
            this.rotation = rotation;
        }
    }
    
}
