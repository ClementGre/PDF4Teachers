/*
 * Copyright (c) 2019-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PDFPagesRender {
    
    private record RenderPending(int pageNumber, int width, CallBackArg<Image> callBack) {}
    
    private final File file;
    public PDFPagesEditor editor;
    private PDDocument document;
    private PDFRenderer pdfRenderer;
    
    private final ArrayList<RenderPending> rendersPending = new ArrayList<>();
    
    public boolean advertisement = false;
    private boolean closed = false;
    
    public PDFPagesRender(File file) throws IOException{
        this.file = file;
        
        document = PDDocument.load(file);
        pdfRenderer = new PDFRenderer(document);
        editor = new PDFPagesEditor(document, file);
        
        setupThread();
    }
    private void setupThread(){
        
        new Thread(() -> {
            
            while(!closed){ // not closed
                
                if(rendersPending.size() != 0){ // Render
                    renderPage(rendersPending.get(0));
                    rendersPending.remove(0);
                    
                }else{ // Wait
                    PlatformUtils.sleepThread(100);
                }
            }
            
            // Close
            pdfRenderer = null;
            while(editor.isEdited()){ // document pages not saved
                PlatformUtils.sleepThread(100);
            }
            try{
                document.close();
            }catch(IOException e){ e.printStackTrace(); }
            document = null;
            
        }, "Page Renderer").start();
    
        // Save document pages each 10 seconds if needed
        new Thread(() -> {
            PlatformUtils.sleepThread(10000);
            while(!closed){
                PlatformUtils.runAndWait(() -> {
                    editor.saveEditsIfNeeded();
                    return null;
                });
                PlatformUtils.sleepThread(10000);
            }
        }, "Page Editor Saver").start();
    }
    
    private void renderPage(RenderPending renderPending){
        PDRectangle pageSize = getPageSize(renderPending.pageNumber);
        
        BufferedImage renderImage = new BufferedImage(renderPending.width, (int) (pageSize.getHeight() / pageSize.getWidth() * ((double) renderPending.width)), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = renderImage.createGraphics();
        graphics.setBackground(Color.WHITE);
        
        //document.setResourceCache();
        
        try{
            //PDDocument document = PDDocument.load(file);
            //PDFRenderer pdfRenderer = new PDFRenderer(document);
            pdfRenderer.renderPageToGraphics(renderPending.pageNumber, graphics,
                    (float) renderPending.width / pageSize.getWidth(),
                    (float) renderPending.width / pageSize.getWidth(),
                    RenderDestination.VIEW);
            
            if(document == null) Platform.runLater(() -> renderPending.callBack.call(null));
            //scale(pdfRenderer.renderImage(page, 3, ImageType.RGB), 1800);
            //document.close();
            
            graphics.dispose();
            
            Platform.runLater(() -> renderPending.callBack.call(SwingFXUtils.toFXImage(renderImage, null)));
        }catch(Exception e){
            e.printStackTrace();
            Platform.runLater(() -> renderPending.callBack.call(null));
        }
        
        renderImage.flush();
        System.gc(); // clear unused element in RAM
    }
    
    public void renderPage(int pageNumber, double size, CallBackArg<Image> callBack){
        // *1=595 | *1.5=892 |*2=1190
        rendersPending.add(new RenderPending(pageNumber, (int) (595 * 1.4 * size), callBack));
    }
    
    public BufferedImage renderPageBasic(int pageNumber, int width, int height){
        
        PDRectangle pageSize = getPageSize(pageNumber);
        
        BufferedImage renderImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = renderImage.createGraphics();
        graphics.setBackground(Color.WHITE);
        
        try{
            PDDocument document = PDDocument.load(file);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            pdfRenderer.renderPageToGraphics(pageNumber, graphics, width / pageSize.getWidth(), width / pageSize.getWidth(), RenderDestination.VIEW);
            scale(pdfRenderer.renderImage(pageNumber, 3, ImageType.RGB), 1800);
            document.close();
            graphics.dispose();
            
            return renderImage;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static void renderAdvertisement(){
        if(MainWindow.mainScreen.hasDocument(false)){
            if(!MainWindow.mainScreen.document.pdfPagesRender.advertisement){ // not already sended
                MainWindow.mainScreen.document.pdfPagesRender.advertisement = true;
                
                // send notifications here
                MainWindow.showNotification(AlertIconType.WARNING, TR.tr("document.loadErrorNotification"), 20);
            }
        }
    }
    
    public void close(){
        closed = true;
        editor.saveEditsIfNeeded();
    }
    
    public PDDocument getDocument(){
        return document;
    }
    
    public int getNumberOfPages(){
        return document.getNumberOfPages();
    }
    
    public PDRectangle getPageSize(int pageNumber){
        
        return getPageCropBox(pageNumber);
		/*PDPage page = document.getPage(pageNumber);
		PDRectangle pageSize;
		if(page.getRotation() == 90 || page.getRotation() == 270) pageSize = new PDRectangle(page.getBleedBox().getHeight(), page.getBleedBox().getWidth());
		else pageSize = page.getBleedBox();

		return pageSize;*/
    }
    
    public PDRectangle getPageCropBox(int pageNumber){
        PDPage page = document.getPage(pageNumber);
        PDRectangle pageSize;
        if(page.getRotation() == 90 || page.getRotation() == 270)
            pageSize = new PDRectangle(page.getCropBox().getHeight(), page.getCropBox().getWidth());
        else pageSize = page.getCropBox();
        
        return pageSize;
    }
    
    public BufferedImage scale(BufferedImage img, double width){
        
        if(img.getWidth() < width){
            return img;
        }
        
        int destWidth = (int) (width);
        int destHeight = (int) (img.getHeight() / ((double) img.getWidth()) * width);
        
        //créer l'image de destination
        GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage newImg = configuration.createCompatibleImage(destWidth, destHeight);
        
        Graphics2D graphics = newImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        //dessiner l'image de destination
        graphics.drawImage(img, 0, 0, destWidth, destHeight, 0, 0, img.getWidth(), img.getHeight(), null);
        graphics.dispose();
        
        return newImg;
    }
}
