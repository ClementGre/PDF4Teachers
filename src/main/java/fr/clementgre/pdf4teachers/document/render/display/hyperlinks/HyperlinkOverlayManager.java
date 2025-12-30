/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display.hyperlinks;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages hyperlink overlays for a PageRenderer
 */
public class HyperlinkOverlayManager {
    
    private final PageRenderer pageRenderer;
    private final List<HyperlinkOverlay> overlays = new ArrayList<>();
    private boolean visible = false;
    
    public HyperlinkOverlayManager(PageRenderer pageRenderer) {
        this.pageRenderer = pageRenderer;
    }
    
    /**
     * Extracts hyperlinks from the PDF page and creates overlay elements
     */
    public void extractHyperlinks() {
        // Clear existing overlays
        clearOverlays();
        
        if (!MainWindow.mainScreen.hasDocument(false)) return;
        
        try {
            PDPage page = MainWindow.mainScreen.document.pdfPagesRender.getDocument()
                    .getPage(pageRenderer.getPage());
            
            List<PDAnnotation> annotations = page.getAnnotations();
            if (annotations == null) return;
            
            for (PDAnnotation annotation : annotations) {
                if(annotation instanceof PDAnnotationLink linkAnnotation){
                    processLinkAnnotation(linkAnnotation, page);
                }
            }
        } catch (IOException e) {
            Log.e(e);
        }
    }
    
    private void processLinkAnnotation(PDAnnotationLink linkAnnotation, PDPage page) throws IOException {
        PDRectangle rect = linkAnnotation.getRectangle();
        if (rect == null) return;
        
        Hyperlink hyperlink = createHyperlinkFromAnnotation(linkAnnotation, rect, page);
        if (hyperlink != null) {
            HyperlinkOverlay overlay = new HyperlinkOverlay(hyperlink, pageRenderer);
            overlays.add(overlay);
            
            // Add overlay to page
            if (!pageRenderer.getChildren().contains(overlay)) {
                pageRenderer.getChildren().add(overlay);
            }
            overlay.setVisible(visible);
        }
    }
    
    private Hyperlink createHyperlinkFromAnnotation(PDAnnotationLink linkAnnotation, PDRectangle rect, PDPage page) throws IOException {
        Hyperlink.Type type = Hyperlink.Type.UNKNOWN;
        String destination = null;
        Integer targetPage = null;
        Integer targetY = null;
        
        PDAction action = linkAnnotation.getAction();
        PDDestination dest = linkAnnotation.getDestination();
        
        // Check action first
        if(action instanceof PDActionURI uriAction){
            String uri = uriAction.getURI();
            if (uri != null) {
                if (uri.startsWith("mailto:")) {
                    type = Hyperlink.Type.MAILTO;
                } else {
                    type = Hyperlink.Type.URL;
                }
                destination = uri;
            }
        }else if(action instanceof PDActionGoTo gotoAction){
            type = Hyperlink.Type.GOTO;
            if(gotoAction.getDestination() instanceof PDPageDestination gotoActionDestination){
                targetPage = gotoActionDestination.retrievePageNumber();
                if(gotoActionDestination instanceof PDPageXYZDestination xyzDestination){
                    targetY = xyzDestination.getTop();
                }
            }
        }else if(dest instanceof PDPageDestination pageDestination){
            // Check destination if no action
            type = Hyperlink.Type.GOTO;
            targetPage = pageDestination.retrievePageNumber();
            if(pageDestination instanceof PDPageXYZDestination xyzDestination){
                targetY = xyzDestination.getTop();
            }
        }
        
        if (type == Hyperlink.Type.UNKNOWN) {
            return null; // Skip unknown link types
        }
        
        // Convert PDF coordinates to JavaFX coordinates
        PDRectangle pageCropBox = page.getCropBox();
        double pageHeight = pageCropBox.getHeight();
        
        // PDF coordinates start from bottom-left, JavaFX from top-left
        double x = rect.getLowerLeftX();
        double y = pageHeight - rect.getUpperRightY();
        double width = rect.getWidth();
        double height = rect.getHeight();
        
        return new Hyperlink(type, destination, targetPage, targetY, x, y, width, height);
    }
    
    /**
     * Shows all hyperlink overlays
     */
    public void show() {
        if(visible) return;
        visible = true;
        for (HyperlinkOverlay overlay : overlays) {
            overlay.setVisible(true);
            overlay.toFront();
        }
    }
    
    /**
     * Hides all hyperlink overlays
     */
    public void hide() {
        if(!visible) return;
        visible = false;
        for (HyperlinkOverlay overlay : overlays) {
            overlay.setVisible(false);
        }
    }
    
    /**
     * Clears all hyperlink overlays
     */
    public void clearOverlays() {
        for (HyperlinkOverlay overlay : overlays) {
            pageRenderer.getChildren().remove(overlay);
        }
        overlays.clear();
        visible = false;
    }
    
    /**
     * Visual overlay for a hyperlink
     */
    private static class HyperlinkOverlay extends Region {
        private final Hyperlink hyperlink;
        private final PageRenderer pageRenderer;
        
        public HyperlinkOverlay(Hyperlink hyperlink, PageRenderer pageRenderer) {
            this.hyperlink = hyperlink;
            this.pageRenderer = pageRenderer;
            
            setupVisuals();
            setupInteraction();
            updatePosition();
        }
        
        private static final String STYLE = "-fx-border-color: rgba(0, 120, 215, 0.5); -fx-border-radius: 2px; ";
        private void setupVisuals() {
            // Semi-transparent blue overlay
            setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 120, 215, 0.1),
                    new CornerRadii(2),
                    Insets.EMPTY
            )));
            
            // Border
            setStyle(STYLE + "-fx-border-width: 0px;");
            
            setCursor(Cursor.HAND);
            setMouseTransparent(false);
        }
        
        private void setupInteraction() {
            setOnMouseClicked(event -> {
                event.consume();
                handleClick();
            });
            
            setOnMouseEntered(event -> {
                setBackground(new Background(new BackgroundFill(
                        Color.rgb(0, 120, 215, 0.2),
                        new CornerRadii(2),
                        Insets.EMPTY
                )));
                setStyle(STYLE + "-fx-border-width: 1px;");
            });
            
            setOnMouseExited(event -> {
                setBackground(new Background(new BackgroundFill(
                        Color.rgb(0, 120, 215, 0.1),
                        new CornerRadii(2),
                        Insets.EMPTY
                )));
                setStyle(STYLE + "-fx-border-width: 0px;");
            });
        }
        
        private void handleClick() {
            switch(hyperlink.type()){
                case URL, MAILTO:
                    if(hyperlink.destination() != null){
                        try {
                            if(Main.hostServices != null){
                                Main.hostServices.showDocument(hyperlink.destination());
                                MainWindow.mainScreen.updateHyperlinksVisibility(false);
                            }
                        } catch (Exception e) {
                            Log.eNotified(e, "Error opening link: " + hyperlink.destination());
                        }
                    }
                    break;
                case GOTO:
                    if(hyperlink.targetPage() != null){
                        if(!MainWindow.mainScreen.hasDocument(false)) return;
                        PageRenderer page = MainWindow.mainScreen.document.getPage(hyperlink.targetPage());
                        if(page == null) return;
                        MainWindow.mainScreen.zoomOperator.scrollToPageAndY(page, hyperlink.targetY());
                    }
                    break;
                default:
                    Log.notifyOnly("Invalid link type.");
                    break;
            }
        }
        
        private void updatePosition() {
            // Get the actual PDF page dimensions
            PDRectangle pageCropBox;
            try {
                PDPage page = MainWindow.mainScreen.document.pdfPagesRender.getDocument()
                        .getPage(pageRenderer.getPage());
                pageCropBox = page.getCropBox();
            } catch (Exception e) {
                // Fallback to A4 dimensions if we can't get the page
                pageCropBox = new PDRectangle(595, 842);
            }
            
            // Convert PDF coordinates to page coordinates
            // PDF coordinates are already converted in createHyperlinkFromAnnotation,
            // so we just need to scale them to the current page size
            double pageWidth = pageCropBox.getWidth();
            double pageHeight = pageCropBox.getHeight();
            
            // Check for valid dimensions to avoid division by zero
            if (pageWidth > 0 && pageHeight > 0) {
                double scaleX = pageRenderer.getWidth() / pageWidth;
                double scaleY = pageRenderer.getHeight() / pageHeight;
                
                double x = hyperlink.x() * scaleX;
                double y = hyperlink.y() * scaleY;
                double width = hyperlink.width() * scaleX;
                double height = hyperlink.height() * scaleY;
                
                setLayoutX(x);
                setLayoutY(y);
                setPrefWidth(width);
                setPrefHeight(height);
            }
        }
    }
}
