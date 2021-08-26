/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.image;

import fr.clementgre.pdf4teachers.Main;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class ImageUtils {
    
    public static final List<String> ACCEPTED_EXTENSIONS = Arrays.asList("png", "jpeg", "jpg", "tiff", "gif", "bmp");
    
    public static ColorAdjust defaultFullDarkColorAdjust = new ColorAdjust();
    public static ColorAdjust defaultDarkColorAdjust = new ColorAdjust();
    public static ColorAdjust defaultGrayColorAdjust = new ColorAdjust();
    public static ColorAdjust defaultWhiteColorAdjust = new ColorAdjust();
    
    public static void setupListeners(){
        Main.settings.darkTheme.valueProperty().addListener((observable, oldValue, newValue) -> updateColorsAdjust());
        updateColorsAdjust();
    }
    
    private static void updateColorsAdjust(){
        if(Main.settings.darkTheme.getValue()){
            defaultFullDarkColorAdjust.setBrightness(1);
            defaultDarkColorAdjust.setBrightness(0.8);
            defaultGrayColorAdjust.setBrightness(0.8);
            defaultWhiteColorAdjust.setBrightness(0);
        }else{
            defaultFullDarkColorAdjust.setBrightness(-1);
            defaultDarkColorAdjust.setBrightness(0.25);
            defaultGrayColorAdjust.setBrightness(0);
            defaultWhiteColorAdjust.setBrightness(-1);
        }
    }
    
    public static ImageView buildImage(Image image, int width, int height){
        ImageView imageView = new ImageView(image);
        
        return getImageView(width, height, imageView);
    }
    
    public static ImageView buildImage(String imgPath, int width, int height){
        return buildImage(imgPath, width, height, null);
    }
    
    public static ImageView buildImage(String imgPath, int width, int height, Effect effect){
        Image image;
        ImageView imageView;
        try{
            image = new Image(imgPath);
            imageView = new ImageView(image);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            System.err.println("Image " + imgPath + " does not exist");
            System.err.println(e.getMessage());
            imageView = new ImageView();
        }
        
        if(effect != null) imageView.setEffect(effect);
        
        return getImageView(width, height, imageView);
    }
    public static ImageView buildImage(FileInputStream file, int width, int height){
        ImageView imageView = new ImageView(new Image(file));
        
        return getImageView(width, height, imageView);
    }
    
    private static ImageView getImageView(int width, int height, ImageView imageView){
        if(width == 0 && height == 0) return imageView;
        
        if(width == 0){
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
        }else if(height == 0){
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }else{
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
        }
        return imageView;
    }
    
    // Angle could only be a multiple of 90
    public static Image rotateImage(Image image, int angle){
        if(angle == 0) return image;
        
        return SwingFXUtils.toFXImage(
                rotateImage(SwingFXUtils.fromFXImage(image, null), angle),
                null);
    }
    
    // Angle could only be a multiple of 90
    public static BufferedImage rotateImage(BufferedImage image, int angle){
        if(angle == 0) return image;
        
        int width = image.getWidth();
        int height = image.getHeight();
        if(angle == 90 || angle == -90 || angle == 270 || angle == -270){
            width = height;
            height = image.getWidth();
        }
        
        BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        
        g.rotate(Math.toRadians(angle), width / 2d, height / 2d);
        
        if(angle == 90 || angle == -90 || angle == 270 || angle == -270){
            // Translate the diff between width and height size
            int diff = (image.getWidth() - image.getHeight()) / 2;
            g.translate(-diff, diff);
        }
        
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        
        return bImg;
    }
    
    
}
