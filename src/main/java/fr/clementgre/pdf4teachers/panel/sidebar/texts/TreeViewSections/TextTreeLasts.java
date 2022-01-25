/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.ListsManager;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.utils.sort.Sorter;

import java.util.ArrayList;
import java.util.List;

public class TextTreeLasts extends TextTreeSection {
    
    public ListsManager listsManager;
    
    public TextTreeLasts(){
        super(TR.tr("textTab.previousList.name"), LAST_TYPE);
        
        listsManager = new ListsManager(this);
        setupGraphics();
    }
    
    @Override
    public void setupSortManager(){
        sortManager.setup(sortCell.pane, TR.tr("sorting.sortType.addDate.short"),
                TR.tr("sorting.sortType.addDate.short"), TR.tr("sorting.sortType.name"), TR.tr("sorting.sortType.use"),
                "\n",
                TR.tr("sorting.sortType.fontFamily"), TR.tr("sorting.sortType.fontSize"), TR.tr("sorting.sortType.color"));
    }
    
    @Override
    public void setupGraphics(){
        super.setupGraphics();
        pane.getChildren().add(pane.getChildren().size() - 1, listsManager.saveListBtn);
        pane.getChildren().add(pane.getChildren().size() - 1, listsManager.loadListBtn);
        
    }
    
    @Override
    public void updateGraphics(){
        super.updateGraphics();
        listsManager.updateGraphics();
    }
    
    @Override
    public void addElement(TextTreeItem element){
        if(!getChildren().contains(element)){
            getChildren().add(element);
            checkMaxElements();
            sortManager.simulateCall();
        }
    }
    private void checkMaxElements(){
        int toRemoveItems = getChildren().size() - Main.settings.maxPreviousElements.getValue();
        if(toRemoveItems > 0){
            
            
            // SORT BY DATE
            List<TextTreeItem> toSort = new ArrayList<>();
            for(int i = 0; i < getChildren().size(); i++){
                if(getChildren().get(i) instanceof TextTreeItem){
                    toSort.add((TextTreeItem) getChildren().get(i));
                }
            }
            List<TextTreeItem> sorted = Sorter.sortElementsByDate(toSort, false);
        
            // The removed items will be the less used along the older half of the maximum number of previous elements.
            toSort = new ArrayList<>();
            for(int i = 0; i < Math.min(Main.settings.maxPreviousElements.getValue() / 2, sorted.size()-1); i++){
                toSort.add(sorted.get(i));
            }
            sorted = Sorter.sortElementsByUtils(toSort, false);
        
            for(int i = 0; i < toRemoveItems; i++){
                if(sorted.size() <= i){ // More than the half of the items have been removed, let's re-call the function.
                    checkMaxElements();
                    return;
                }
                removeElement(sorted.get(i));
            }
        }
    }
}
