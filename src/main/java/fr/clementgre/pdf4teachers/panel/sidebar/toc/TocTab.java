/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.toc;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideTab;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

import java.io.IOException;

public class TocTab extends SideTab {
    
    private final VBox root = new VBox();
    private final TreeView<TocEntry> treeView = new TreeView<>();
    private final Label emptyLabel = new Label();
    
    public TocTab() {
        super("toc", SVGPathIcons.LIST, 20, 1.05);
        
        setContent(root);
        
        emptyLabel.setText(TR.tr("sidebar.toc.empty"));
        emptyLabel.setFont(Font.font(emptyLabel.getFont().getFamily(), FontWeight.ITALIC, 13));
        emptyLabel.setPadding(new Insets(10));
        
        root.getChildren().add(emptyLabel);
        
        treeView.setShowRoot(false);
        treeView.setPadding(new Insets(5));
        
        // Handle click on TOC entry
        treeView.setOnMouseClicked(event -> {
            TreeItem<TocEntry> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() != null) {
                TocEntry entry = selectedItem.getValue();
                if (entry.getPageNumber() >= 0) {
                    navigateToPage(entry.getPageNumber());
                }
            }
        });
    }
    
    /**
     * Loads the table of contents from the PDF document
     */
    public void loadTableOfContents() {
        if (!MainWindow.mainScreen.hasDocument(false)) {
            showEmptyState();
            return;
        }
        
        PDDocument document = MainWindow.mainScreen.document.pdfPagesRender.getDocument();
        if (document == null) {
            showEmptyState();
            return;
        }
        
        PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
        if (outline == null) {
            showEmptyState();
            return;
        }
        
        TreeItem<TocEntry> rootItem = new TreeItem<>(new TocEntry("Root", -1));
        buildOutlineTree(outline, rootItem);
        
        if (rootItem.getChildren().isEmpty()) {
            showEmptyState();
        } else {
            treeView.setRoot(rootItem);
            root.getChildren().clear();
            root.getChildren().add(treeView);
        }
    }
    
    /**
     * Recursively builds the outline tree
     */
    private void buildOutlineTree(PDOutlineNode parentOutline, TreeItem<TocEntry> parentTreeItem) {
        PDOutlineItem current = parentOutline.getFirstChild();
        while (current != null) {
            try {
                String title = current.getTitle();
                int pageNumber = -1;
                
                // Get the page number from the destination
                if (current.getDestination() != null) {
                    pageNumber = current.getDestination().retrievePageNumber();
                } else if (current.getAction() != null) {
                    // Some PDFs use actions instead of destinations
                    try {
                        if (current.getAction() instanceof org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo) {
                            org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo goToAction = 
                                (org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo) current.getAction();
                            if (goToAction.getDestination() != null) {
                                pageNumber = goToAction.getDestination().retrievePageNumber();
                            }
                        }
                    } catch (IOException e) {
                        // Ignore - page number will remain -1
                    }
                }
                
                TocEntry entry = new TocEntry(title != null ? title : "Untitled", pageNumber);
                TreeItem<TocEntry> treeItem = new TreeItem<>(entry);
                parentTreeItem.getChildren().add(treeItem);
                
                // Recursively add children
                buildOutlineTree(current, treeItem);
                
                current = current.getNextSibling();
            } catch (Exception e) {
                // Skip this outline item if there's an error
                try {
                    current = current.getNextSibling();
                } catch (Exception e2) {
                    break;
                }
            }
        }
    }
    
    /**
     * Shows empty state when no TOC is available
     */
    private void showEmptyState() {
        root.getChildren().clear();
        root.getChildren().add(emptyLabel);
    }
    
    /**
     * Navigates to the specified page
     */
    private void navigateToPage(int pageNumber) {
        if (!MainWindow.mainScreen.hasDocument(false)) return;
        
        if (pageNumber >= 0 && pageNumber < MainWindow.mainScreen.document.getPagesNumber()) {
            // Scroll to the page
            MainWindow.mainScreen.document.getPage(pageNumber).toFront();
            MainWindow.mainScreen.document.getPage(pageNumber).requestFocus();
            
            // Calculate the target scroll position
            double targetY = MainWindow.mainScreen.document.getPage(pageNumber).getTranslateY();
            double paneHeight = MainWindow.mainScreen.pane.getHeight();
            if (paneHeight > 0) {
                MainWindow.mainScreen.zoomOperator.vScrollBar.setValue(
                    targetY / paneHeight
                );
            }
        }
    }
    
    /**
     * Clears the table of contents
     */
    public void clear() {
        showEmptyState();
    }
}
