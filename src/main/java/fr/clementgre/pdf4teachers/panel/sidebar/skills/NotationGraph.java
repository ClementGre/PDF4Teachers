/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;


import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.image.ColorUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class NotationGraph extends Pane {
    
    private String style = "-fx-background-radius: 5px; -fx-alignment: center;";
    private double size = 20;
    private double scale = 1;
    
    public NotationGraph(){
        this(1);
    }
    public NotationGraph(double scale){
        this.scale = scale;
        this.size = 20*scale;
        style = "-fx-background-radius: " + (scale*5) + "px; -fx-alignment: center;";
        setMinSize(size, size);
        setMaxSize(size, size);
    }
    
    public NotationGraph(double scale, Notation.NotationType notationType, Notation notation, boolean forceWhiteBackground){
        this.scale = scale;
        this.size = 20*scale;
        style = "-fx-background-radius: " + (scale*5) + "px; -fx-alignment: center;";
        setMinSize(size, size);
        setMaxSize(size, size);
        
        updateGraph(notationType, notation, forceWhiteBackground);
    }
    
    public NotationGraph(Notation.NotationType notationType, Notation notation, boolean forceWhiteBackground){
        setMinSize(size, size);
        setMaxSize(size, size);
        
        updateGraph(notationType, notation, forceWhiteBackground);
    }
    
    public void updateGraph(Notation.NotationType notationType, Notation notation, boolean forceWhiteBackground){
        getChildren().clear();
        
        String backgroundColor = "#FFFFFF";
        String foregroundColor = "#000000";
        if(!Main.settings.darkTheme.getValue() || forceWhiteBackground){
            backgroundColor = "#000000";
            foregroundColor = "#FFFFFF";
        }
        String fontStyle = notation.isDefaultNotation() ? "italic" : "normal";
        
        if(notationType == Notation.NotationType.CHAR || notation.isDefaultNotation()){
            
            String text = notation.getAcronym();
            if(text.isBlank()) text = "?";
            else if(text.length() > 1) text = text.substring(0, 2);
            text = text.toUpperCase();
        
            Label label = new Label(text);
            label.setStyle("-fx-alignment: center; -fx-text-fill: " + foregroundColor + "; -fx-font: " + fontStyle + " bold " + (scale*11) + "px 'Open Sans' !important;");
            label.setMinSize(size, size);
            label.setMaxSize(size, size);
            
            setStyle(style + "-fx-background-color: " + backgroundColor + ";");
            getChildren().add(label);
        
        }else if(notationType == Notation.NotationType.COLOR){
            setStyle(style + "-fx-background-color: " + ColorUtils.toRGBHex(ColorUtils.parseWebOr(notation.getData(), Color.DARKGREEN)) + ";");
        
        }else if(notationType == Notation.NotationType.ICON){
            Image image;
            try{
                byte[] bytes = Base64.getDecoder().decode(notation.getData());
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
                image = SwingFXUtils.toFXImage(bufferedImage, null);
            }catch(IllegalArgumentException | IOException | NullPointerException e){
                image = ImageElement.getNotFoundImage();
            }
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
        
            double reduceCoeff = Math.min(size/image.getWidth(), size/image.getHeight());
            double w = image.getWidth() * reduceCoeff;
            double h = image.getHeight() * reduceCoeff;
        
            double topBottom = (size - h) / 2;
            double leftRight = (size - w) / 2;
            imageView.setLayoutX(leftRight);
            imageView.setLayoutY(topBottom);
    
            setStyle(style);
            getChildren().add(imageView);
        }
    }
    
    public Image isolatedSnapshot(){
        return PlatformUtils.runAndWait(() ->{
            new Scene(this, size, size);
            
            SnapshotParameters sn = new SnapshotParameters();
            sn.setFill(Color.TRANSPARENT);
            return snapshot(sn, null);
        });
    }
}
