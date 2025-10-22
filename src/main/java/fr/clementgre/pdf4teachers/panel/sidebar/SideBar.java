/*
 * Copyright (c) 2021-2025. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SideBar extends TabPane {
    
    public static final String TAB_DRAG_KEY = "SideBarTabDrag";
    public static final int DEFAULT_WIDTH = 270;
    public static final int MAX_WIDTH = 450;
    public static final int TAB_WIDTH = 50; // Estimated
    
    public static final List<String> DEFAULT_LEFT_TABS = List.of("files", "text", "grades", "skills", "paint");
    public static final List<String> DEFAULT_RIGHT_TABS = Collections.emptyList();
    
    private static final String STYLE = "-fx-tab-max-width: 22px;";
    public static Tab draggingTab;
    private final boolean left;
    private boolean loaded;
    
    public SideBar(boolean left){
        this.left = left;
        
        getStyleClass().add("app-side-bar");
        setStyle(STYLE);
        
        setMaxWidth(left ? Main.syncUserData.leftBarWidth : Main.syncUserData.rightBarWidth);
        setMinWidth(0);
        restoreWidth(false, false);
        
        SplitPane.setResizableWithParent(this, false);
        
        widthProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.doubleValue() != 0 && draggingTab == null){
                if(left){
                    Main.syncUserData.leftBarWidth = newValue.doubleValue();
                }else{
                    Main.syncUserData.rightBarWidth = newValue.doubleValue();
                }
            }
        });
        
        getTabs().addListener((ListChangeListener<Tab>) c -> {
            c.next();
            if(getTabs().isEmpty()){ // No tab in this pane
                if(draggingTab != null){
                    showDragSpace();
                }else{
                    removeToPane();
                }
            }else if(c.wasAdded() && getTabs().size() == 1){ // The tab is the first added
                hideDragSpace();
                
                if(loaded){ // prevent editing max width when not completely loaded yet
                    Platform.runLater(() -> {
                        setMaxWidth(MAX_WIDTH);
                        if(getWidth() <= 50){
                            restoreWidth(false, true);
                        }
                    });
                }
                
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
                        int targetIndex = MathUtils.clamp((int) ((e.getX() - 5) / TAB_WIDTH), 0, getTabs().size() - 1);
                        
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
                    int targetIndex = MathUtils.clamp((int) ((e.getX() - 5) / TAB_WIDTH), 0, getTabs().size() - 1);
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
                    draggingTab = null;
                }
            }
        });
        
        Platform.runLater(() -> {
            getTabs().addListener((ListChangeListener<Tab>) c -> {
                saveBarsOrganization();
            });
        });
        
    }
    public static void selectTab(String tab){
        selectTab(SideTab.getByName(tab));
    }
    public static SideBar getTabSideBar(SideTab tab){
        if(isIntoLeftBar(tab)) return MainWindow.leftBar;
        else return MainWindow.rightBar;
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
    public static void showDragSpaces(){
        MainWindow.leftBar.showDragSpace();
        MainWindow.rightBar.showDragSpace();
    }
    public static void hideDragSpaces(){
        MainWindow.leftBar.hideDragSpace();
        MainWindow.rightBar.hideDragSpace();
    }
    public static void saveBarsOrganization(){
        Main.syncUserData.leftBarOrganization = MainWindow.leftBar.getTabsList();
        Main.syncUserData.rightBarOrganization = MainWindow.rightBar.getTabsList();
    }
    public static void loadBarsOrganization(){
        MainWindow.leftBar.loadTabsList(Main.syncUserData.leftBarOrganization);
        MainWindow.rightBar.loadTabsList(Main.syncUserData.rightBarOrganization);
    }
    public static void loadDefaultBarsOrganization(){
        MainWindow.leftBar.loadTabsList(DEFAULT_LEFT_TABS);
        MainWindow.rightBar.loadTabsList(DEFAULT_RIGHT_TABS);
    }
    public static void loadBarsDefaultWidths(){
        MainWindow.leftBar.restoreDefaultWidth();
        MainWindow.rightBar.restoreDefaultWidth();
    }
    public static boolean areSideBarsMinimized(){
        return MainWindow.leftBar.isMinimizedAndOnPane() || MainWindow.rightBar.isMinimizedAndOnPane();
    }
    public static void toggleSideBarsMinimized(){
        if(areSideBarsMinimized()){
            MainWindow.leftBar.deMinimize();
            MainWindow.rightBar.deMinimize();
        }else{
            MainWindow.leftBar.minimize();
            MainWindow.rightBar.minimize();
        }
    }
    public static void moveAllTabsToLeft(){
        // Move all tabs from right bar to left bar
        ArrayList<Tab> tabsToMove = new ArrayList<>(MainWindow.rightBar.getTabs());
        for(Tab tab : tabsToMove){
            MainWindow.rightBar.getTabs().remove(tab);
            MainWindow.leftBar.getTabs().add(tab);
        }
    }
    public static void moveAllTabsToRight(){
        // Move all tabs from left bar to right bar
        ArrayList<Tab> tabsToMove = new ArrayList<>(MainWindow.leftBar.getTabs());
        for(Tab tab : tabsToMove){
            MainWindow.leftBar.getTabs().remove(tab);
            MainWindow.rightBar.getTabs().add(tab);
        }
    }
    public void restoreWidth(boolean onlyIfTooSmall, boolean byDivider){
        double width = left ? Main.syncUserData.leftBarWidth : Main.syncUserData.rightBarWidth;
        if(width <= 50){
            if(onlyIfTooSmall) return;
            width = DEFAULT_WIDTH;
        }
        if(byDivider){
            setWidthByEditingDivider(width);
        }else{
            setPrefWidth(width);
            setWidth(width);
        }
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
            restoreWidth(true, true);
            if(left){
                // Right width must be updated to stay the same
                double rightWidth = MainWindow.rightBar.getWidthByDivider();
                
                MainWindow.mainPane.getItems().addFirst(this);
                
                MainWindow.rightBar.setWidthByEditingDivider(rightWidth);
            }else MainWindow.mainPane.getItems().add(this);
        }
    }
    private void removeToPane(){
        if(MainWindow.mainPane.getItems().contains(this)){
            // Check if removing this would leave no sidebars (only mainScreen)
            if(MainWindow.mainPane.getItems().size() <= 2){
                // Instead of removing, just set width to minimal value
                // Using 1 instead of 0 to avoid GTK warnings on Linux
                setMaxWidth(1);
                setWidthByEditingDivider(1);
            }else{
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
        
    }
    public void showDragSpace(){
        if(getTabs().isEmpty()){
            addToPane();
            setStyle(STYLE + "-fx-background-color: #0078d7");
            setWidthByEditingDivider(30);
            setMaxWidth(30);
        }else{
            addToPane();
            restoreWidth(false, true);
        }
    }
    
    public void hideDragSpace(){
        setStyle(STYLE);
        if(getTabs().isEmpty()){
            removeToPane();
        }else{
            addToPane();
        }
    }
    public List<String> getTabsList(){
        return getTabs()
                .stream()
                .map(tab -> (SideTab) tab)
                .map(SideTab::getName)
                .collect(Collectors.toCollection(ArrayList::new));
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
        loaded = true;
        PlatformUtils.runLaterOnUIThread(3000, () -> {
            setMaxWidth(MAX_WIDTH); // restore normal max width
        });
    }
    public void restoreDefaultWidth(){
        if(left){
            Main.syncUserData.leftBarWidth = DEFAULT_WIDTH;
        }else{
            Main.syncUserData.rightBarWidth = DEFAULT_WIDTH;
        }
        if(MainWindow.mainPane.getItems().contains(this)){
            setMaxWidth(MAX_WIDTH);
            setWidthByEditingDivider(DEFAULT_WIDTH);
        }
    }
    public boolean isMinimizedAndOnPane(){
        return MainWindow.mainPane.getItems().contains(this) && getWidth() <= 50;
    }
    public boolean isNotMinimizedAndOnPane(){
        return MainWindow.mainPane.getItems().contains(this) && getWidth() > 50;
    }
    public void minimize(){
        if(isNotMinimizedAndOnPane()){
            setWidthByEditingDivider(0);
        }
    }
    public void deMinimize(){
        if(isMinimizedAndOnPane()){
            restoreWidth(false, true);
        }
    }
}
