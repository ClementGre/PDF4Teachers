/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections;

import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.render.display.PageRenderer;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.MainScreen.MainScreen;

public class TextTreeOnFile extends TextTreeSection {
    
    public TextTreeOnFile(){
        super(TR.tr("textTab.onDocumentList.name"), ONFILE_TYPE);
        setupGraphics();
    }
    
    @Override
    public void setupSortManager(){
        sortManager.setup(sortCell.pane, TR.tr("sorting.sortType.location"),
                TR.tr("sorting.sortType.location"), TR.tr("sorting.sortType.name"),
                "\n",
                TR.tr("sorting.sortType.fontFamily"), TR.tr("sorting.sortType.fontSize"), TR.tr("string.color"));
    }
    
    public void updateElementsList(){
        clearElements(true);
        
        // GET ALL ELEMENTS In THE DOCUMENT
        if(MainWindow.mainScreen.getStatus() == MainScreen.Status.OPEN){
            for(PageRenderer page : MainWindow.mainScreen.document.getPages()){
                for(int i = 0; i < page.getElements().size(); i++){
                    if(page.getElements().get(i) instanceof TextElement element){
                        getChildren().add(element.toNoDisplayTextElement(ONFILE_TYPE, true));
                    }
                }
            }
        }
        sortManager.simulateCall();
    }
    
    public void addElement(TextElement element){
        getChildren().add(element.toNoDisplayTextElement(ONFILE_TYPE, true));
        sortManager.simulateCall();
    }
    
    @Override
    public void removeElement(TextElement element){
        super.removeElement(element);
        MainWindow.textTab.treeView.lastsSection.removeElement(element);
        MainWindow.textTab.treeView.favoritesSection.removeElement(element);
    }
    
}
