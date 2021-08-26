/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SideBar extends TabPane {
    
    public static final String TAB_DRAG_KEY = "SideBarTabDrag";
    
    public static Tab draggingTab = null;
    
    public static final int DEFAULT_WIDTH = 270;
    public static final int MAX_WIDTH = 450;
    
    private static final String STYLE = "-fx-tab-max-width: 22px;";
    
    private final boolean left;
    
    public SideBar(boolean left){
        this.left = left;
        
        setStyle(STYLE);
        
        setMaxWidth(DEFAULT_WIDTH);
        setMinWidth(0);
        setPrefWidth(DEFAULT_WIDTH);
        setWidth(DEFAULT_WIDTH);
        
        SplitPane.setResizableWithParent(this, false);
        
        getTabs().addListener((ListChangeListener<Tab>) c -> {
            c.next();
            if(getTabs().size() == 0){ // No tab in this pane
                if(draggingTab != null){
                    showDragSpace();
                }else{
                    removeToPane();
                }
            }else if(c.wasAdded() && getTabs().size() == 1){ // The tab is the first added
                hideDragSpace();
                Platform.runLater(() -> {
                    setMaxWidth(MAX_WIDTH);
                    if(getWidth() <= 50){
                        setWidthByEditingDivider(DEFAULT_WIDTH);
                    }
                });
            }
            
            // prevent layout bugs
            PlatformUtils.runLaterOnUIThread(200, this::requestLayout);
            PlatformUtils.runLaterOnUIThread(500, this::requestLayout);
        });
        
        AtomicReference<TabPane> previewLastTabPane = new AtomicReference<>(null);
        AtomicReference<Tab> previewTab = new AtomicReference<>(null);
        
        setOnDragOver(e -> {
            final Dragboard dragboard = e.getDragboard();
            if(TAB_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){
                
                if(draggingTab != null){
                    
                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                    
                    if(draggingTab.getTabPane() == this){ // Skip if tab is already in preview / already in this tabPane
                        int actualIndex = getTabs().indexOf(draggingTab);
                        int targetIndex = StringUtils.clamp((int) ((e.getX() - 5) / 55), 0, getTabs().size() - 1);
                        
                        if(actualIndex != targetIndex){
                            getTabs().remove(draggingTab);
                            getTabs().add(targetIndex, draggingTab);
                            getSelectionModel().select(draggingTab);
                        }
                        
                        return;
                    }
                    
                    previewLastTabPane.set(draggingTab.getTabPane());
                    previewTab.set(draggingTab);
                    
                    draggingTab.getTabPane().getTabs().remove(draggingTab);
                    int targetIndex = StringUtils.clamp((int) ((e.getX() - 5) / 55), 0, getTabs().size() - 1);
                    getTabs().add(targetIndex, draggingTab);
                    getSelectionModel().select(draggingTab);
                    
                    SideBar.showDragSpaces();
                }
            }
            
        });
        setOnDragExited(e -> { // Remove the tab of this TabPane and re-add it into its original TabPane
            final Dragboard dragboard = e.getDragboard();
            if(TAB_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){
                if(draggingTab != null && previewTab.get() != null){ // Check there is a tab who is temporary in this TabPane
                    
                    if(draggingTab.getTabPane() != previewLastTabPane.get()){ // Check the Tab is not already into the target TabPane
                        getTabs().remove(draggingTab);
                        previewLastTabPane.get().getTabs().add(draggingTab);
                        previewLastTabPane.get().getSelectionModel().select(draggingTab);
                    }
                    previewTab.set(null);
                    previewLastTabPane.set(null);
                    SideBar.showDragSpaces();
                }
            }
        });
        setOnDragDropped(event -> { // Complete drop : Make the preview final
            final Dragboard dragboard = event.getDragboard();
            if(TAB_DRAG_KEY.equals(dragboard.getContent(Main.INTERNAL_FORMAT))){
                if(draggingTab != null){
                    previewTab.set(null);
                    previewLastTabPane.set(null);
                    
                    event.setDropCompleted(true);
                    event.consume();
                    SideBar.hideDragSpaces();
                }
            }
        });
        
        Platform.runLater(() -> {
            getTabs().addListener((ListChangeListener<Tab>) c -> {
                saveBarsOrganization();
            });
        });
        
    }
    
    public static SideBar getTabSideBar(SideTab tab){
        if(isIntoLeftBar(tab)) return MainWindow.leftBar;
        else return MainWindow.rightBar;
    }
    
    public void setWidthByEditingDivider(double width){
        if(left){
            MainWindow.mainPane.setDividerPosition(0, width / MainWindow.mainPane.getWidth());
        }else{
            if(MainWindow.mainPane.getItems().contains(MainWindow.leftBar)){
                MainWindow.mainPane.setDividerPosition(1, (MainWindow.mainPane.getWidth() - width) / MainWindow.mainPane.getWidth());
            }else{ // Only one tab in mainPane -> divider n°0
                MainWindow.mainPane.setDividerPosition(0, (MainWindow.mainPane.getWidth() - width) / MainWindow.mainPane.getWidth());
            }
            
        }
    }
    public double getWidthByDivider(){
        if(left){
            return MainWindow.mainPane.getDividerPositions()[0] * MainWindow.mainPane.getWidth();
        }else{
            if(MainWindow.mainPane.getItems().contains(MainWindow.leftBar)){
                return MainWindow.mainPane.getWidth() - (MainWindow.mainPane.getDividerPositions()[1] * MainWindow.mainPane.getWidth());
            }else{ // Only one tab in mainPane -> divider n°0
                return MainWindow.mainPane.getWidth() - (MainWindow.mainPane.getDividerPositions()[0] * MainWindow.mainPane.getWidth());
            }
            
        }
    }
    
    private void addToPane(){
        if(!MainWindow.mainPane.getItems().contains(this)){
            if(left){
                // Right width must be updated to stay the same
                double rightWidth = MainWindow.rightBar.getWidthByDivider();
                
                MainWindow.mainPane.getItems().add(0, this);
                
                MainWindow.rightBar.setWidthByEditingDivider(rightWidth);
            }else MainWindow.mainPane.getItems().add(this);
        }
    }
    private void removeToPane(){
        if(MainWindow.mainPane.getItems().contains(this)){
            if(left){
                // Right width must be updated to stay the same
                double rightWidth = MainWindow.rightBar.getWidthByDivider();
                
                MainWindow.mainPane.getItems().remove(this);
                
                MainWindow.rightBar.setWidthByEditingDivider(rightWidth);
            }else{
                MainWindow.mainPane.getItems().remove(this);
            }
        }
        
    }
    
    public static void selectTab(String tab){
        selectTab(SideTab.getByName(tab));
    }
    
    public static void selectTab(Tab tab){
        if(isIntoLeftBar(tab)){
            MainWindow.leftBar.getSelectionModel().select(tab);
        }else if(isIntoRightBar(tab)){
            MainWindow.rightBar.getSelectionModel().select(tab);
        }
    }
    
    public static boolean isIntoLeftBar(Tab tab){
        return MainWindow.leftBar.getTabs().contains(tab);
    }
    
    public static boolean isIntoRightBar(Tab tab){
        return MainWindow.rightBar.getTabs().contains(tab);
    }
    
    public static void setupDividers(SplitPane mainPane){
    
    }
    
    public static void showDragSpaces(){
        MainWindow.leftBar.showDragSpace();
        MainWindow.rightBar.showDragSpace();
    }
    
    public void showDragSpace(){
        if(getTabs().size() == 0){
            addToPane();
            setStyle(STYLE + "-fx-background-color: #0078d7");
            setWidthByEditingDivider(30);
            setMaxWidth(30);
        }
        
    }
    
    public static void hideDragSpaces(){
        MainWindow.leftBar.hideDragSpace();
        MainWindow.rightBar.hideDragSpace();
    }
    
    public void hideDragSpace(){
        setStyle(STYLE);
        if(getTabs().size() == 0){
            removeToPane();
        }
    }
    
    public List<String> getTabsList(){
        ArrayList<String> tabs = new ArrayList<>();
        
        for(Tab tab : getTabs()){
            SideTab sideTab = (SideTab) tab;
            tabs.add(sideTab.getName());
        }
        
        return tabs;
    }
    
    public void loadTabsList(List<String> tabsName){
        for(String tabName : tabsName){
            SideTab tab = SideTab.getByName(tabName);
            if(tab != null){
                if(tab.getTabPane() != null) tab.getTabPane().getTabs().remove(tab);
                getTabs().add(tab);
            }
        }
        hideDragSpace();
    }
    
    public static void saveBarsOrganization(){
        Main.syncUserData.leftBarOrganization = MainWindow.leftBar.getTabsList();
        Main.syncUserData.rightBarOrganization = MainWindow.rightBar.getTabsList();
    }
    
    public static void loadBarsOrganization(){
        MainWindow.leftBar.loadTabsList(Main.syncUserData.leftBarOrganization);
        MainWindow.rightBar.loadTabsList(Main.syncUserData.rightBarOrganization);
    }
}
