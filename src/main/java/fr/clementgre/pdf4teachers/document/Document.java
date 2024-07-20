/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoEngine;
import fr.clementgre.pdf4teachers.document.render.display.PDFPagesRender;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.document.render.display.PageStatus;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.CustomAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.WarningAlert;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Document {
    
    private final File file;
    public Edition edition;
    private final ArrayList<PageRenderer> pages = new ArrayList<>();
    
    private int lastSelectedPage;
    private final HashSet<Integer> selectedPages = new HashSet<>();
    
    private int currentPage = -1;
    public int numberOfPages;
    
    public PDFPagesRender pdfPagesRender;
    private UndoEngine undoEngine;
    
    private boolean documentSaverNeedToStop;
    public void stopDocumentSaver(){
        documentSaverNeedToStop = true;
    }
    private final Thread documentSaver = new Thread(() -> {
        documentSaverNeedToStop = false;
        while(!documentSaverNeedToStop){
            if(Main.settings.regularSave.getValue() != -1){
                
                PlatformUtils.sleepThreadMinutes(Main.settings.regularSave.getValue());
                if(!documentSaverNeedToStop && !Edition.isSave()){
                    Platform.runLater(() -> edition.save(true));
                }
                
            }else{
                // Should not be too big because it keeps the document as a CG Root Object.
                PlatformUtils.sleepThreadSeconds(30);
            }
        }
    }, "Document AutoSaver");
    
    public Document(File file) throws IOException{
        this.file = file;
        
        pdfPagesRender = new PDFPagesRender(file);
        if(pdfPagesRender.getNumberOfPages() == 0) throw new IOException("Unable to load a document with 0 pages!");
        numberOfPages = pdfPagesRender.getNumberOfPages();
    }
    
    public void showPages(){
        
        for(int i = 0; i < numberOfPages; i++){
            PageRenderer page = new PageRenderer(i);
            MainWindow.mainScreen.addPage(page);
            pages.add(page);
        }
        getPage(0).updatePosition(PageRenderer.getPageMargin(), false);
        updateShowsStatus();
    }
    
    public void updatePagesPosition(){
        getPage(0).updatePosition(PageRenderer.getPageMargin(), true);
        updateShowsStatus();
    }
    
    public void updateShowsStatus(){
        for(PageRenderer page : pages){
            page.updateShowStatus();
        }
    }
    
    public void updateZoom(){
        for(PageRenderer page : pages){
            page.updateZoom();
        }
    }
    
    public void updateBackgrounds(){
        for(PageRenderer page : pages){
            page.setStatus(PageStatus.HIDE);
        }
        updateShowsStatus();
    }
    
    public boolean loadEdition(boolean updateScrollValue){
        edition = new Edition(file, this);
        if(edition.load(updateScrollValue)){
            if(!documentSaver.isAlive()) documentSaver.start();
            undoEngine = new UndoEngine();
            return true;
        }
        return false;
    }
    // Excludes the grid display mode
    public double getLastScrollValue(){
        return MainWindow.mainScreen.zoomOperator.getLastVScrollValue();
    }
    public void setCurrentScrollValue(double value){
        MainWindow.mainScreen.zoomOperator.vScrollBar.setValue(value);
    }
    
    public ArrayList<Element> getElements(){
        return pages.stream()
                .flatMap(page -> page.getElements().stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }
    // [1] : Elements
    // [2] : Texts
    // [3] : Grades
    // [4] : Graphics
    public int[] countElements(){
        ArrayList<Element> elements = getElements();
        int texts = (int) elements.stream().filter((e) -> e instanceof TextElement).count();
        int grades = (int) elements.stream().filter((e) -> e instanceof GradeElement).count();
        int graphics = (int) elements.stream().filter((e) -> e instanceof GraphicElement).count();
        return new int[]{elements.size(), texts, grades, graphics};
    }
    
    public void updateEdition(){
        MainWindow.mainScreen.setSelected(null);
        for(PageRenderer page : pages){
            page.clearElements();
        }
        MainWindow.textTab.treeView.onFileSection.updateElementsList();
        MainWindow.skillsTab.clearEditRelatedData();
        MainWindow.gradeTab.treeView.clearElements(false, false);
        this.edition.load(false);
        this.undoEngine = new UndoEngine();
    }
    
    public void close(){
        pdfPagesRender.close();
        for(int i = 0; i < numberOfPages; i++){
            if(pages.size() > i) pages.get(i).remove();
        }
        pages.clear();
        undoEngine = null;
    }
    /**
     * Save the edition of this document
     *
     * @param doShowIgnoreWarning if user click on "Ignore", the WarningAlert for editing/exporting an edition that is not saved will be displayed.
     * @return false if action has been cancelled, true otherwise, whatever saved or ignored.
     */
    public boolean save(boolean doShowIgnoreWarning){
        
        if(Edition.isSave()){
            edition.saveLastScrollValue();
            return true;
        }
        
        if(Main.settings.autoSave.getValue()){
            edition.save(true);
            return true;
            
        }
        
        CustomAlert alert = new CustomAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.unsavedEdit.title"), TR.tr("dialog.unsavedEdit.header"), TR.tr("dialog.unsavedEdit.details"));
        alert.addCancelButton(ButtonPosition.CLOSE);
        alert.addButton(TR.tr("actions.save"), ButtonPosition.DEFAULT);
        ButtonType ignore = alert.addIgnoreButton(ButtonPosition.OTHER_RIGHT);
        
        ButtonType option = alert.getShowAndWait();
        if(option == null) return false; // Window close button (null)
        if(option.getButtonData().isDefaultButton()){ // Save button (Default)
            edition.save(true);
            return true;
            
        }
        if(option.getButtonData().isCancelButton()){ // cancel button
            return false;
        }
        
        // Ignore button or OS close
        if(option == ignore && doShowIgnoreWarning){
            boolean exportAnyway = new WarningAlert(TR.tr("dialog.unsavedEdit.title"),
                    TR.tr("dialog.unsavedEdit.ignoreWarningDialog.header"),
                    TR.tr("dialog.unsavedEdit.ignoreWarningDialog.details"))
                    .execute();
            if(!exportAnyway) return false;
        }
        edition.saveLastScrollValue();
        return true;
        
    }
    
    public UndoEngine getUndoEngine(){
        return undoEngine;
    }
    public boolean hasUndoEngine(){
        return undoEngine != null;
    }
    
    public String getFileName(){
        return file.getName();
    }
    
    public File getFile(){
        return file;
    }
    
    public int getLastCursorOverPage(){
        return currentPage;
    }
    
    public HashSet<Integer> getSelectedPages(){
        return selectedPages;
    }
    public void invertSelectedPage(int index){
        if(selectedPages.contains(index)) selectedPages.remove(index);
        else selectedPages.add(index);
        
        lastSelectedPage = index;
        updateSelectedPages();
    }
    public void deselectPage(int index){
        selectedPages.remove(index);
        lastSelectedPage = index;
        updateSelectedPages();
    }
    public void selectPage(int index){
        selectedPages.clear();
        selectedPages.add(index);
        lastSelectedPage = index;
        updateSelectedPages();
    }
    public void addSelectedPage(int index){
        selectedPages.add(index);
        lastSelectedPage = index;
        updateSelectedPages();
    }
    public void clearSelectedPages(){
        selectedPages.clear();
        lastSelectedPage = 0;
        updateSelectedPages();
    }
    public void setLastSelectedPage(int index){
        lastSelectedPage = index;
    }
    public void selectToPage(int index, boolean keepOldSelection){
        if(!keepOldSelection) selectedPages.clear();
        lastSelectedPage = MathUtils.clamp(lastSelectedPage, 0, pages.size()-1);
        boolean forward = index >= lastSelectedPage;
        for(int i = lastSelectedPage; i != index; i += forward ? 1 : -1) selectedPages.add(i);
        selectedPages.add(index);
        updateSelectedPages();
    }
    public void selectAll(){
        selectedPages.clear();
        for(int i = 0; i < pages.size(); i++) selectedPages.add(i);
        updateSelectedPages();
    }
    public void updateSelectedPages(){
        
        for(PageRenderer page : pages){
            if(MainWindow.mainScreen.isEditPagesMode() && selectedPages.contains(page.getPage())) page.setEffect(MainWindow.mainScreen.selectedShadow);
            else page.setEffect(MainWindow.mainScreen.notSelectedShadow);
        }
    }
    public boolean isPageSelected(PageRenderer page){
        return selectedPages.contains(page.getPage());
    }
    public boolean isPageSelected(int page){
        return selectedPages.contains(page);
    }
    
    
    public void setCurrentPage(int currentPage){
        this.currentPage = currentPage;
        MainWindow.footerBar.updateCurrentPage();
    }
    public int getPagesNumber(){
        return pages.size();
    }
    
    // PageRenderer getter
    
    public ArrayList<PageRenderer> getPages(){
        return pages;
    }
    public PageRenderer getPage(int page){
        return page < pages.size() ? pages.get(page) : null;
    }
    public PageRenderer getPageNonNull(int page){
        return page < pages.size() ? pages.get(page) : pages.getFirst();
    }
    public WeakReference<PageRenderer> getPageWeakReference(int page){
        return new WeakReference<>(getPageNonNull(page));
    }
    
    public PageRenderer getLastCursorOverPageObject(){
        return (getLastCursorOverPage() != -1) ? pages.get(getLastCursorOverPage()) : pages.getFirst();
    }
    public WeakReference<PageRenderer> getLastCursorOverPageWeakReference(){
        return (getLastCursorOverPage() != -1) ? getPageWeakReference(getLastCursorOverPage()) : getPageWeakReference(0);
    }
    
    public PageRenderer getPreciseMouseCurrentPage(){
        PageRenderer match = null;
        for(PageRenderer page : pages){
            if(MainWindow.mainScreen.mouseY < page.getBottomY()){
                match = page; break;
            }
            match = page;
        }
        
        if(MainWindow.mainScreen.isGridView() && match != null){
            // Detected page is the first one of the row.
            // Check pages horizontally.
            for(int i = match.getPage(); i < Math.min(match.getPage()+MainWindow.mainScreen.getGridModePagesPerRow(), MainWindow.mainScreen.document.numberOfPages); i++){
                PageRenderer page = MainWindow.mainScreen.document.getPage(i);
                if(MainWindow.mainScreen.mouseX < page.getRightX()){
                    match = page; break;
                }
                match = page;
            }
        }
        
        return match;
    }
    
    // Return null if there is no top page below the top of MainScreen
    public PageRenderer getFirstTopVisiblePage(){
        
        Bounds mainScreenBoundsInScene = MainWindow.mainScreen.localToScene(MainWindow.mainScreen.getLayoutBounds());
        
        int i = 0;
        int iMax = pages.size();
        for(PageRenderer page : pages){
            Bounds boundsInScene = MainWindow.mainScreen.pane.localToScene(page.getBoundsInParent());
            if(boundsInScene.getMinY() > mainScreenBoundsInScene.getMinY()){
                return page;
            }
            i++;
        }
        return null;
    }
    // Return null if there is no bottom page above the bottom of MainScreen
    public PageRenderer getFirstBottomVisiblePage(){
        
        Bounds mainScreenBoundsInScene = MainWindow.mainScreen.localToScene(MainWindow.mainScreen.getLayoutBounds());
        
        int iMax = pages.size();
        for(int i = 1; i <= iMax; i++){
            PageRenderer page = pages.get(iMax - i);
            
            Bounds boundsInScene = MainWindow.mainScreen.pane.localToScene(page.getBoundsInParent());
            if(boundsInScene.getMaxY() < mainScreenBoundsInScene.getMaxY()){
                return page;
            }
        }
        return null;
    }
}
