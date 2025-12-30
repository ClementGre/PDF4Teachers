/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.display.hyperlinks;

import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import javafx.application.HostServices;
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
            }
        }else if(dest instanceof PDPageDestination pageDestination){
            // Check destination if no action
            type = Hyperlink.Type.GOTO;
            targetPage = pageDestination.retrievePageNumber();
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
        
        return new Hyperlink(type, destination, targetPage, x, y, width, height);
    }
    
    /**
     * Shows all hyperlink overlays
     */
    public void show() {
        visible = true;
        for (HyperlinkOverlay overlay : overlays) {
            overlay.setVisible(true);
        }
    }
    
    /**
     * Hides all hyperlink overlays
     */
    public void hide() {
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
        
        private void setupVisuals() {
            // Semi-transparent blue overlay
            setBackground(new Background(new BackgroundFill(
                    Color.rgb(0, 120, 215, 0.2),
                    new CornerRadii(2),
                    Insets.EMPTY
            )));
            
            // Border
            setStyle("-fx-border-color: rgba(0, 120, 215, 0.5); -fx-border-width: 1px; -fx-border-radius: 2px;");
            
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
                        Color.rgb(0, 120, 215, 0.3),
                        new CornerRadii(2),
                        Insets.EMPTY
                )));
            });
            
            setOnMouseExited(event -> {
                setBackground(new Background(new BackgroundFill(
                        Color.rgb(0, 120, 215, 0.2),
                        new CornerRadii(2),
                        Insets.EMPTY
                )));
            });
        }
        
        private void handleClick() {
            switch (hyperlink.getType()) {
                case URL, MAILTO:
                    if (hyperlink.getDestination() != null) {
                        try {
                            HostServices hostServices = fr.clementgre.pdf4teachers.Main.hostServices;
                            if (hostServices != null) {
                                hostServices.showDocument(hyperlink.getDestination());
                            }
                        } catch (Exception e) {
                            Log.eNotified(e, "Error opening link: " + hyperlink.getDestination());
                        }
                    }
                    break;
                case GOTO:
                    if (hyperlink.getTargetPage() != null) {
                        navigateToPage(hyperlink.getTargetPage());
                    }
                    break;
            }
        }
        
        private void navigateToPage(int pageNumber) {
            if (!MainWindow.mainScreen.hasDocument(false)) return;
            
            if (pageNumber >= 0 && pageNumber < MainWindow.mainScreen.document.getPagesNumber()) {
                // Scroll to the target page
                double targetY = MainWindow.mainScreen.document.getPage(pageNumber).getTranslateY();
                double paneHeight = MainWindow.mainScreen.pane.getHeight();
                if (paneHeight > 0) {
                    MainWindow.mainScreen.zoomOperator.vScrollBar.setValue(
                            targetY / paneHeight
                    );
                }
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
                
                double x = hyperlink.getX() * scaleX;
                double y = hyperlink.getY() * scaleY;
                double width = hyperlink.getWidth() * scaleX;
                double height = hyperlink.getHeight() * scaleY;
                
                setLayoutX(x);
                setLayoutY(y);
                setPrefWidth(width);
                setPrefHeight(height);
            }
        }
    }
}
