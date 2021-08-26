/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.ListsManager;

public class TextTreeFavorites extends TextTreeSection {
    
    public ListsManager listsManager;
    
    public TextTreeFavorites(){
        super(TR.tr("textTab.favoriteList.name"), FAVORITE_TYPE);
        
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
}
