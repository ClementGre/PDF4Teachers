/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills;


import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class NotationGraph extends Pane {
    
    public NotationGraph(Notation.NotationType notationType, String data){
        if(notationType == Notation.NotationType.CHAR){
            
            if(data.isBlank()) data = "?";
            else if(data.length() > 1) data = data.substring(0, 2);
            data = data.toUpperCase();
            
            Label label = new Label(data);
            label.setStyle("-fx-text-fill: black; -fx-alignment: center; -fx-font: sans-serif, 'Open Sans'; -fx-font-size: 11; -fx-font-weight: bold;");
            label.setMinSize(20, 20);
            label.setMaxSize(20, 20);
            setStyle("-fx-background-color: white;");
            
            getChildren().add(label);
            
        }else if(notationType == Notation.NotationType.COLOR){
            setStyle("-fx-background-color: " + data + ";");
            
        }else if(notationType == Notation.NotationType.ICON){
            Image image;
            try{
                byte[] bytes = Base64.getDecoder().decode(data);
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
            
            getChildren().add(imageView);
        }
        
        setStyle(getStyle() + "-fx-background-radius: 5px; -fx-alignment: center;");
        setMinSize(20, 20);
        setMaxSize(20, 20);
    }
    
}
