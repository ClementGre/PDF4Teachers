/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.gallery;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageData;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.lists.ImageLambdaData;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.image.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GalleryManager {
    
    // PATHS
    
    public static List<File> getSavePathsFiles(){
        return getSavePaths().stream().map(File::new).collect(Collectors.toList());
    }
    public static List<String> getSavePaths(){
        return MainWindow.userData.galleryPaths.stream().filter((str) -> {
            try{
                return new File(str).isDirectory();
            }catch(SecurityException e){
                e.printStackTrace();
                return false;
            }
        }).distinct().collect(Collectors.toList());
    }
    public static void setSavePathsFiles(List<File> newPaths){
        setSavePaths(newPaths.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
    }
    public static void setSavePaths(List<String> newPaths){
        MainWindow.userData.galleryPaths = newPaths;
    }
    public static void removeSavePath(String path){
        List<String> paths = getSavePaths();
        paths.remove(path);
        setSavePaths(paths);
    }
    public static void addSavePath(String path){
        List<String> paths = getSavePaths();
        if(!paths.contains(path)){
            paths.add(path);
            setSavePaths(paths);
        }
    }
    
    // IMAGES
    
    public static ArrayList<ImageLambdaData> getImages(){
        ArrayList<ImageLambdaData> images = new ArrayList<>();
        for(File dir : getSavePathsFiles()){
            images.addAll(getImagesInDir(dir));
        }
        return images;
    }
    public static ArrayList<ImageLambdaData> getImagesInDir(File dir){
        ArrayList<ImageLambdaData> images = new ArrayList<>();
        for(File file : Objects.requireNonNull(dir.listFiles())){
            if(isAcceptableImage(file.getName()) && !file.isHidden()){
                images.add(new ImageLambdaData(dir.getAbsolutePath() + File.separator + file.getName()));
            }
        }
        return images;
    }
    
    public static ArrayList<ImageData> getFavoritesImages(){
        return null;
    }
    
    
    public static boolean isAcceptableImage(String filename){
        return ImageUtils.ACCEPTED_EXTENSIONS.stream().anyMatch((ext) -> filename.toLowerCase().endsWith(ext));
    }
    
}
