/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces.windows.splitpdf;

import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplitEngine {
    
    private final SplitWindow splitWindow;
    private final ArrayList<Color> colors = new ArrayList<>();
    
    // Stores the page indice of the first and last page of each section (ordered)
    private ArrayList<Integer> sectionsBounds = new ArrayList<>();
    
    public SplitEngine(SplitWindow splitWindow){
        this.splitWindow = splitWindow;
    }
    
    public void process(){
    
    }
    
    public void updateDetectedPages(CallBack callBack){
        new Thread(() -> {
            for(PageRenderer page : MainWindow.mainScreen.document.getPages()){
                BufferedImage image = MainWindow.mainScreen.document.pdfPagesRender.renderPageBasic(page.getPage(), 12, (int) (12 / page.getRatio()));
                colors.add(averageColor(image));
            }
            final List<Color> uniqueColors = getColorsUnique();
            Platform.runLater(() -> {
                splitWindow.getCustomColors().setAll(uniqueColors);
                callBack.call();
            });
        }, "Detect noticeable pages").start();
    }
    
    private List<Color> getColorsUnique(){
    
        List<Color> colors = new ArrayList<>(this.colors);
        
        // Remove similar colors
        for(int i = 0; i < colors.size(); i++){
            Color color = colors.get(i);
            colors = colors.stream()
                    .filter((match) -> getColorDiff(color, match) > .1 || color == match)
                    .toList();
        }
        
        return colors;
    }
    
    public int countMatchPages(){
        if(colors.isEmpty()) return -1;
        
        Color match = splitWindow.getColor();
        double sensibility = splitWindow.getSensibility();
        sectionsBounds.clear();
        MainWindow.mainScreen.document.clearSelectedPages();
        
        int i = 0;
        boolean doLastPageDetected = true;
        for(Color color : colors){
    
            double diff = getColorDiff(color, match);
            if(diff < sensibility){
                if(!doLastPageDetected) sectionsBounds.add(i-1); // End of section
                doLastPageDetected = true;
                MainWindow.mainScreen.document.addSelectedPage(i);
            }else{
                if(doLastPageDetected) sectionsBounds.add(i); // Start of section
                doLastPageDetected = false;
            }
            i++;
        }
        if(!doLastPageDetected) sectionsBounds.add(i-1);
        
        if(sectionsBounds.isEmpty()) sectionsBounds = new ArrayList<>(Arrays.asList(0, colors.size()-1));
        
        return sectionsBounds.size()/2;
    }
    
    // Colors difference between 0 and 1
    private double getColorDiff(Color c1, Color c2){
        return (Math.abs(c1.getRed() - c2.getRed()) + Math.abs(c1.getGreen() - c2.getGreen()) + Math.abs(c1.getBlue() - c2.getBlue())) / 3;
    }
    
    
    public static Color averageColor(BufferedImage bi) {
        long sumr = 0, sumg = 0, sumb = 0;
        
        for(int x = 0; x < bi.getWidth(); x++){
            for (int y = 0; y < bi.getHeight(); y++){
                java.awt.Color pixel = new java.awt.Color(bi.getRGB(x, y));
                sumr += pixel.getRed();
                sumg += pixel.getGreen();
                sumb += pixel.getBlue();
            }
        }
        float num = bi.getWidth() * bi.getHeight() * 255; // Switching from range 0-255 to range 0-1
        return Color.color(sumr / num, sumg / num, sumb / num);
    }
}
