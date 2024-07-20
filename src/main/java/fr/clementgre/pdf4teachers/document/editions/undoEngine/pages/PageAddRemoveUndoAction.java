/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions.undoEngine.pages;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UType;
import fr.clementgre.pdf4teachers.document.editions.undoEngine.UndoAction;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeRating;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import org.apache.pdfbox.pdmodel.PDPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PageAddRemoveUndoAction extends UndoAction {
    
    private final int pageIndex;
    private final PDPage page;
    private boolean deleted;
    private final boolean originallyDeleted;
    private List<Element> elements = new ArrayList<>();
    private List<GradeRating> gradeRatings = new ArrayList<>();
    
    public PageAddRemoveUndoAction(UType undoType, int pageIndex, PageRenderer pageRenderer, PDPage page, boolean deleted){
        super(undoType);
        this.pageIndex = pageIndex;
        this.page = page;
        this.deleted = deleted;
        this.originallyDeleted = deleted;
        
        if(deleted && pageRenderer != null) loadElements(pageRenderer);
    }
    @Override
    public boolean undoAndInvert(){
        if(MainWindow.mainScreen.hasDocument(false)){
            if(deleted){
                MainWindow.mainScreen.document.pdfPagesRender.editor.addPage(page, pageIndex);
                restoreElements();
            }else{
                loadElements(MainWindow.mainScreen.document.getPage(pageIndex));
                MainWindow.mainScreen.document.pdfPagesRender.editor.deletePage(MainWindow.mainScreen.document.getPage(pageIndex));
            }
            
            // invert
            deleted = !deleted;
            return true;
        }
        return false;
    }
    
    private void loadElements(PageRenderer page){
        elements = page.getElements().stream().filter(e -> !(e instanceof GradeElement)).toList();
        gradeRatings = page.getElements().stream().filter(e -> e instanceof GradeElement).map(e -> ((GradeElement) e).toGradeRating()).toList();
    }
    
    private void restoreElements(){
    
        PageRenderer page = MainWindow.mainScreen.document.getPage(pageIndex);
        
        for(Element element : elements){
            
            
            // GradeElements are not deleted, just reset
            if(element instanceof GradeElement) return;
            
            // Move element if it has been moved on another page (e.g. SkillTableElement).
            if(element.getParent() != null){
                if(!page.getElements().contains(element)) element.switchPage(pageIndex);
                return;
            }
            
            element.setPage(page);
            page.addElement(element, true, UType.NO_UNDO);
            element.restoredToDocument();
        }
        
        restoreGrade(MainWindow.gradeTab.treeView.getRootTreeItem());
        
        Edition.setUnsave("RestorePageElements");
        MainWindow.mainScreen.document.edition.save(false);
        MainWindow.mainScreen.setSelected(null);
    }
    
    private void restoreGrade(GradeTreeItem gradeTreeItem){
        
        // Update children
        for(int i = 0; i < gradeTreeItem.getChildren().size(); i++){
            GradeTreeItem children = (GradeTreeItem) gradeTreeItem.getChildren().get(i);
            restoreGrade(children);
        }
        gradeTreeItem.makeSum(false);
    
        // Update values
        GradeElement gradeElement = gradeTreeItem.getCore();
        Optional<GradeRating> gradeRating = gradeRatings.stream()
                .filter((g) -> Objects.equals(g.name, gradeElement.getName()))
                .filter((g) -> Objects.equals(g.parentPath, gradeElement.getParentPath()))
                .filter((g) -> Objects.equals(g.index, gradeElement.getIndex()))
                .filter((g) -> Objects.equals(g.total, gradeElement.getTotal())).findFirst();
    
        if(gradeRating.isPresent()){
            if(!gradeTreeItem.hasSubGrade()) gradeElement.setValue(gradeRating.get().originalValue);
            gradeElement.switchPage(gradeRating.get().page);
            gradeElement.setRealX(gradeRating.get().x);
            gradeElement.setRealY(gradeRating.get().y);
        }
    }
    
    @Override
    public String toString(){
        if(originallyDeleted){
            return TR.tr("actions.deletePage", pageIndex+1);
        }
        return TR.tr("actions.addPage", pageIndex+1);
        
    }
}
