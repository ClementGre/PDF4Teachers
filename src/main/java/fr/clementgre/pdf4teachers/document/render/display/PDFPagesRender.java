/*
 * Copyright (c) 2019-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlertIconType;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBackArg;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
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
import java.nio.file.Files;
import java.util.ArrayList;

public class PDFPagesRender {
    
    private record RenderPending(PageRenderer page, int width, CallBackArg<Image> callBack) {}
    
    private final File file;
    public PDFPagesEditor editor;
    private PDDocument document;
    private PDFRenderer pdfRenderer;
    
    private final ArrayList<RenderPending> rendersPending = new ArrayList<>();
    
    public boolean advertisement;
    private boolean pauseRendering;
    private boolean pauseRenderingInner;
    private boolean shouldClose;
    private boolean isClosed;
    
    public PDFPagesRender(File file) throws IOException {
        this.file = file;
        
        loadDocument();
        editor = new PDFPagesEditor(this);
        setupThread();
    }
    private void loadDocument() throws IOException{
        Log.d("Loading PDF Document at path " + file.toPath());
        document = Loader.loadPDF(new RandomAccessReadBufferedFile(file));
        pdfRenderer = new PDFRenderer(document);
        resumeRendering();
        pauseRenderingInner = false;
    }
    /**
     * Since PDFBox v3.0, it is unsafe to overwrite a file directly using document.save(file).
     * This method saves the document to a temporary file, replaces the original file with the temporary file, and reopens the document.
     *
     * @param file The file to save the document to
     * @return True if the document was saved successfully, false if an error occurred
     */
    public boolean saveDocumentTo(File file){
        try{
            pauseRenderingInner = true;
            Log.i("Saving document to file " + file.toPath() + " using temporary file.");
            
            File tempFile = File.createTempFile("tempFile", ".pdf");
            document.save(tempFile);
            document.close();
            
            Files.move(tempFile.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            loadDocument();
        }catch(IOException e){
            Log.e(e);
            ErrorAlert alert = new ErrorAlert(TR.tr("dialog.error.unableToSavePDFPagesEdits"), e.getMessage(), false);
            alert.getButtonTypes().clear();
            alert.addIgnoreButton(ButtonPosition.CLOSE);
            alert.addDefaultButton(TR.tr("actions.retry"));
            
            if(alert.getShowAndWaitIsDefaultButton()) return saveDocumentTo(file);
            
            MainWindow.mainScreen.closeFile(false, false, true);
            return false;
        }
        return true;
    }
    private void setupThread(){
        new Thread(() -> {
            while(!shouldClose){ // not closed
                if(!pauseRendering && !pauseRenderingInner && !rendersPending.isEmpty() && !rendersPending.getFirst().page.isRemoved()){ // Render
                    if(rendersPending.getFirst().page.getPage() < getNumberOfPages()){
                        renderPage(rendersPending.getFirst());
                    }else
                        Log.w("Unable to render page " + rendersPending.getFirst().page.getPage() + " (index out of bounds : page doesn't exist)");
                    rendersPending.removeFirst();
                }else{ // Wait
                    PlatformUtils.sleepThread(100);
                }
            }
            
            // Close
            Log.d("Closing Page Renderer Thread and closing document...");
            pdfRenderer = null;
            while(editor.isEdited()){ // wait until document pages are saved
                PlatformUtils.sleepThread(100);
            }
            try{
                document.close();
            }catch(IOException e){ Log.eNotified(e); }
            document = null;
            isClosed = true;
            
        }, "Page Renderer").start();
        
        // Save document pages each 10 seconds if needed
        new Thread(() -> {
            PlatformUtils.sleepThread(10000);
            while(!shouldClose){
                PlatformUtils.runAndWait(() -> {
                    editor.saveEditsIfNeeded();
                    return null;
                });
                PlatformUtils.sleepThread(10000);
            }
        }, "Page Editor Saver").start();
    }
    
    private void renderPage(RenderPending renderPending){
        PDRectangle pageSize = getPageRotatedCropBox(renderPending.page.getPage());
        
        BufferedImage renderImage = new BufferedImage(Math.max(1, renderPending.width), (int) Math.max(1, pageSize.getHeight() / pageSize.getWidth() * ((double) renderPending.width)), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = renderImage.createGraphics();
        graphics.setBackground(Color.WHITE);
        
        try{
            pdfRenderer.renderPageToGraphics(renderPending.page.getPage(), graphics,
                    (float) renderPending.width / pageSize.getWidth(),
                    (float) renderPending.width / pageSize.getWidth(),
                    RenderDestination.VIEW);
            
            if(renderPending.page.isRemoved()){
                // Nothing
            }else if(document == null){
                Platform.runLater(() -> renderPending.callBack.call(null));
            }else{
                Platform.runLater(() -> renderPending.callBack.call(SwingFXUtils.toFXImage(renderImage, null)));
            }
            graphics.dispose();
        }catch(Exception e){
            Log.eNotified(e);
            Platform.runLater(() -> renderPending.callBack.call(null));
        }
        
        renderImage.flush();
        System.gc(); // clear unused element in RAM
    }
    
    public void renderPage(PageRenderer page, double size, CallBackArg<Image> callBack){
        // *1=595 | *1.5=892 |*2=1190
        rendersPending.add(new RenderPending(page, (int) Math.max(1, 595 * 1.4 * size), callBack));
    }
    
    public BufferedImage renderPageBasic(int pageNumber, int width, int height){
        
        PDRectangle pageSize = getPageRotatedCropBox(pageNumber);
        
        BufferedImage renderImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = renderImage.createGraphics();
        graphics.setBackground(Color.WHITE);
        
        try{
            pdfRenderer.renderPageToGraphics(pageNumber, graphics, width / pageSize.getWidth(), width / pageSize.getWidth(), RenderDestination.VIEW);
            scale(pdfRenderer.renderImage(pageNumber, 3, ImageType.RGB), 1800);
            graphics.dispose();
            
            return renderImage;
        }catch(Exception e){
            Log.eNotified(e);
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
        shouldClose = true;
        editor.saveEditsIfNeeded();
    }
    public boolean isClosed(){
        return isClosed;
    }
    
    public PDDocument getDocument(){
        return document;
    }
    
    public int getNumberOfPages(){
        return document.getNumberOfPages();
    }
    
    public void pauseRendering(){
        pauseRenderingInner = true;
    }
    public void resumeRendering(){
        pauseRenderingInner = false;
    }
    
    public File getFile(){
        return file;
    }
    
    public PDRectangle getPageRotatedCropBox(int pageNumber){
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
