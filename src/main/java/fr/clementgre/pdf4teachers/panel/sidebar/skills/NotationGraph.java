/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;


import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.utils.image.ColorUtils;
import javafx.embed.swing.SwingFXUtils;
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
    
    private static final String STYLE = "-fx-background-radius: 5px; -fx-alignment: center;";
    
    public NotationGraph(Notation.NotationType notationType, Notation notation, boolean italic){
        setMinSize(20, 20);
        setMaxSize(20, 20);
        
        updateGraph(notationType, notation, italic);
    }
    
    public void updateGraph(Notation.NotationType notationType, Notation notation, boolean italic){
        getChildren().clear();
        
        String backgroundColor = "#FFFFFF";
        String foregroundColor = "#000000";
        if(!Main.settings.darkTheme.getValue()){
            backgroundColor = "#000000";
            foregroundColor = "#FFFFFF";
        }
        String fontStyle = italic ? "italic" : "normal";
        
        if(notationType == Notation.NotationType.CHAR){
            
            String text = notation.getAcronym();
            if(text.isBlank()) text = "?";
            else if(text.length() > 1) text = text.substring(0, 2);
            text = text.toUpperCase();
        
            Label label = new Label(text);
            label.setStyle("-fx-alignment: center; -fx-text-fill: " + foregroundColor + "; -fx-font: " + fontStyle + " bold 11px 'Open Sans';");
            label.setMinSize(20, 20);
            label.setMaxSize(20, 20);
            
            setStyle(STYLE + "-fx-background-color: " + backgroundColor + ";");
            getChildren().add(label);
        
        }else if(notationType == Notation.NotationType.COLOR){
            setStyle(STYLE + "-fx-background-color: " + ColorUtils.toRGBHex(ColorUtils.parseWebOr(notation.getData(), Color.DARKGREEN)) + ";");
        
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
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
        
            double reduceCoeff = Math.min(20/image.getWidth(), 20/image.getHeight());
            double w = image.getWidth() * reduceCoeff;
            double h = image.getHeight() * reduceCoeff;
        
            double topBottom = (20 - h) / 2;
            double leftRight = (20 - w) / 2;
            imageView.setLayoutX(leftRight);
            imageView.setLayoutY(topBottom);
    
            setStyle(STYLE);
            getChildren().add(imageView);
        }
    }
    
}
