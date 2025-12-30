/*
 * Copyright (c) 2024-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.toc;

import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
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
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

public class TocTab extends SideTab {
    
    private final VBox root = new VBox();
    private final TreeView<TocEntry> treeView = new TreeView<>();
    private final Label emptyLabel = new Label();
    
    public TocTab(){
        super("toc", SVGPathIcons.LIST, 24, 1.05);
        
        setContent(root);
        
        emptyLabel.setText(TR.tr("sidebar.toc.empty"));
        emptyLabel.setWrapText(true);
        emptyLabel.setFont(Font.font(emptyLabel.getFont().getFamily(), FontWeight.NORMAL, 13));
        emptyLabel.setPadding(new Insets(10));
        
        root.getChildren().add(emptyLabel);
        
        treeView.setShowRoot(false);
        treeView.setPadding(new Insets(5));
        treeView.prefHeightProperty().bind(root.heightProperty());
        
        // Handle click on TOC entry
        treeView.setOnMouseClicked(event -> {
            TreeItem<TocEntry> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if(selectedItem != null && selectedItem.getValue() != null){
                TocEntry entry = selectedItem.getValue();
                if(entry.pageNumber() >= 0){
                    navigateToPageAndY(entry.pageNumber(), entry.pageY());
                }
            }
        });
    }
    
    /**
     * Loads the table of contents from the PDF document
     */
    public void loadTableOfContents(){
        if(!MainWindow.mainScreen.hasDocument(false)){
            showEmptyState();
            return;
        }
        
        PDDocument document = MainWindow.mainScreen.document.pdfPagesRender.getDocument();
        if(document == null){
            showEmptyState();
            return;
        }
        
        PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
        if(outline == null){
            showEmptyState();
            return;
        }
        
        TreeItem<TocEntry> rootItem = new TreeItem<>(new TocEntry("Root", -1, 0));
        buildOutlineTree(outline, document, rootItem);
        
        if(rootItem.getChildren().isEmpty()){
            showEmptyState();
        }else{
            treeView.setRoot(rootItem);
            root.getChildren().clear();
            root.getChildren().add(treeView);
        }
    }
    
    /**
     * Recursively builds the outline tree
     */
    private void buildOutlineTree(PDOutlineNode parentOutline, PDDocument document, TreeItem<TocEntry> parentTreeItem){
        PDOutlineItem current = parentOutline.getFirstChild();
        while(current != null){
            try{
                String title = current.getTitle();
                PDPage page = current.findDestinationPage(document);
                int pageNumber = document.getPages().indexOf(page);
                int pageY = 0;
                if(current.getDestination() instanceof PDPageXYZDestination destination){
                    Log.t("Found y value " + destination.getTop() + " for outline item " + title);
                    pageY = destination.getTop();
                }
                
                TocEntry entry = new TocEntry(title != null ? title : "Untitled", pageNumber, pageY);
                TreeItem<TocEntry> treeItem = new TreeItem<>(entry);
                parentTreeItem.getChildren().add(treeItem);
                
                // Recursively add children
                buildOutlineTree(current, document, treeItem);
                
                current = current.getNextSibling();
            }catch(Exception e){
                // Skip this outline item if there's an error
                try{
                    current = current.getNextSibling();
                }catch(Exception e2){
                    break;
                }
            }
        }
    }
    
    /**
     * Shows empty state when no TOC is available
     */
    private void showEmptyState(){
        root.getChildren().clear();
        root.getChildren().add(emptyLabel);
    }
    
    /**
     * Navigates to the specified page
     */
    private void navigateToPageAndY(int pageNumber, int pageY){
        if(!MainWindow.mainScreen.hasDocument(false)) return;
        
        PageRenderer page = MainWindow.mainScreen.document.getPage(pageNumber);
        MainWindow.mainScreen.zoomOperator.scrollToPageAndY(page, pageY);
    }
    
    /**
     * Clears the table of contents
     */
    public void clear(){
        showEmptyState();
    }
}
