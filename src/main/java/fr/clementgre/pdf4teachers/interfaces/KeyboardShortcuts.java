/*
 * Copyright (c) 2019-2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.interfaces;


import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.components.KeyableHBox;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.utils.MathUtils;
import fr.clementgre.pdf4teachers.utils.keyboard.CustomKeyCombination;
import fr.clementgre.pdf4teachers.utils.keyboard.KeyCodesCombination;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class KeyboardShortcuts {
    
    // Shortcuts are checked on the scene event filter. They have the higher priority in the whole app.
    private final ArrayList<ShortcutRecord> shortcuts = new ArrayList<>();
    // Lazy shortcuts are checked on the scene event handler, or in the scene event filter if no classic shortcuts have been fired
    // and the target element is a SideBar, Slider, Button OR is not a Control, Element, KeyableHBox
    // These shortcuts might be used and consumed by other elements, and might not contain any modifier key.
    private final ArrayList<ShortcutRecord> lazyShortcuts = new ArrayList<>();
    // List of menu bar shortcuts used to detect conflicts
    private final ArrayList<ShortcutRecord> menuBarShortcuts = new ArrayList<>();
    
    public KeyboardShortcuts(Scene main){
        
        /*******************************/
        /* Graphics elements shortcuts */
        /*******************************/
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.elements.customGraphicsElements"),
                new CustomKeyCombination(e -> {
                    return matchVectorShortcut(e).isPresent() || matchImageShortcut(e).isPresent();
                }, KeyCodeCombination.SHORTCUT_DOWN), this::firePaintElementsShortcut));
        
        /******************************/
        /***** Elements shortcuts *****/
        /******************************/
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.elements.newText"),
                new KeyCodeCombination(KeyCode.T, KeyCodeCombination.SHORTCUT_DOWN, KeyCodesCombination.SHIFT_ANY), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            SideBar.selectTab(MainWindow.textTab);
            TextElement element = MainWindow.textTab.newTextElement(!e.isShiftDown());
            element.setRealX(element.getPage().getNewElementXOnGrid(false));
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.elements.newVectorDrawing"),
                new KeyCodeCombination(KeyCode.D, KeyCodeCombination.SHORTCUT_DOWN, KeyCodesCombination.SHIFT_ANY), e -> {
            SideBar.selectTab(MainWindow.paintTab);
            MainWindow.paintTab.newVectorDrawing(e.isShiftDown());
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.elements.selectNextGrade"),
                new KeyCodeCombination(KeyCode.N, KeyCodeCombination.SHORTCUT_DOWN), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            SideBar.selectTab(MainWindow.gradeTab);
            MainWindow.gradeTab.treeView.getSelectionModel().select(GradeTreeView.getNextLogicGradeNonNull());
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.elements.createSameLevelGrade"),
                new KeyCodeCombination(KeyCode.G, KeyCodeCombination.SHORTCUT_DOWN), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            if(!MainWindow.gradeTab.isSelected()) MainWindow.gradeTab.select();
            
            GradeTreeItem item = (GradeTreeItem) MainWindow.gradeTab.treeView.getSelectionModel().getSelectedItem();
            if(item == null || item.isRoot()){
                item = MainWindow.gradeTab.treeView.getRootTreeItem(); // In case item == null
                
                GradeElement element = MainWindow.gradeTab.newGradeElementAuto(item);
                element.select();
                // Update total (Fix the bug when a total is predefined (with no children))
                item.makeSum(false);
            }else{
                GradeElement element = MainWindow.gradeTab.newGradeElementAuto(((GradeTreeItem) item.getParent()));
                element.select();
            }
            e.consume();
        }));
        
        /******************************/
        /**** Navigation shortcuts ****/
        /******************************/
        //  +/- or arrows with shortcut for zoom and reset zoom
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.zoomLess"),
                new CustomKeyCombination(e -> {
            return e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT || "-".equals(e.getText());
        }, KeyCodeCombination.SHORTCUT_DOWN), e -> {
            MainWindow.mainScreen.zoomLess();
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.zoomMore"),
                new CustomKeyCombination(e -> {
            return e.getCode() == KeyCode.UP || e.getCode() == KeyCode.KP_UP || e.getCode() == KeyCode.PLUS
                    || e.getCode() == KeyCode.ADD || "+".equals(e.getText()) || e.getCode() == KeyCode.EQUALS;
        }, KeyCodeCombination.SHORTCUT_DOWN), e -> {
            MainWindow.mainScreen.zoomMore();
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.fitWidth"),
                new KeyCodesCombination(KeyCode.DOWN, KeyCode.KP_DOWN, KeyCodeCombination.SHORTCUT_DOWN), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            MainWindow.mainScreen.zoomOperator.fitWidth(false, false);
            e.consume();
        }));
        // Arrows with ALT
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.pageUp"),
                new KeyCodesCombination(KeyCode.UP, KeyCode.KP_UP,
                KeyCodesCombination.SHORTCUT_DOWN, KeyCodesCombination.ALT_DOWN), e -> {
            MainWindow.mainScreen.pageUp();
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.pageDown"),
                new KeyCodesCombination(KeyCode.DOWN, KeyCode.KP_DOWN,
                KeyCodesCombination.SHORTCUT_DOWN, KeyCodesCombination.ALT_DOWN), e -> {
            MainWindow.mainScreen.pageDown();
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.previousFile"),
                new KeyCodesCombination(KeyCode.LEFT, KeyCode.KP_LEFT,
                KeyCodesCombination.SHORTCUT_DOWN, KeyCodesCombination.ALT_DOWN), e -> {
            MainWindow.filesTab.loadPreviousFile();
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.nextFile"),
                new KeyCodesCombination(KeyCode.RIGHT, KeyCode.KP_RIGHT, KeyCodesCombination.SHORTCUT_DOWN, KeyCodesCombination.ALT_DOWN), e -> {
            MainWindow.filesTab.loadNextFile();
            e.consume();
        }));
        // Begin/End and Page Up/Page Down
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.begin"),
                new KeyCodesCombination(KeyCode.BEGIN, KeyCode.HOME), e -> {
            if(!canBeginEndOnNode(Main.window.getScene().getFocusOwner())){ // Do not execute custom actions if a text field or a spinner is focused.
                e.consume();
                MainWindow.mainScreen.navigateBegin();
            }
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.end"),
                new KeyCodeCombination(KeyCode.END), e -> {
            if(!canBeginEndOnNode(Main.window.getScene().getFocusOwner())){ // Do not execute custom actions if a text field or a spinner is focused.
                e.consume();
                MainWindow.mainScreen.navigateEnd();
            }
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.pageUp"),
                new KeyCodeCombination(KeyCode.PAGE_UP), e -> {
            MainWindow.mainScreen.pageUp();
            e.consume();
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.navigation.pageDown"),
                new KeyCodeCombination(KeyCode.PAGE_DOWN), e -> {
            MainWindow.mainScreen.pageDown();
            e.consume();
        }));
        
        /******************************/
        /****** Number shortcuts ******/
        /******************************/
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.numbers"),
                new CustomKeyCombination(e -> {
            return MathUtils.parseIntFromKeyEventOrNull(e) != null;
        }, KeyCodeCombination.SHORTCUT_DOWN, KeyCodesCombination.ALT_ANY), e -> {
            if(!numberPressed(MathUtils.parseIntFromKeyEventOrNull(e), e.isAltDown())) return;
            e.consume();
        }));
        
        /******************************/
        /**** Elements color/size *****/
        /******************************/
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.elements.decrementSize"),
                new CustomKeyCombination(e -> {
            return e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT || "-".equals(e.getText()) || "—".equals(e.getText());
        }, KeyCodeCombination.SHORTCUT_DOWN, KeyCodesCombination.ALT_DOWN), e -> {
            if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                MainWindow.textTab.sizeSpinner.decrement();
                e.consume();
            }else if(MainWindow.mainScreen.getSelected() instanceof VectorElement){
                MainWindow.paintTab.vectorStrokeWidth.decrement();
                e.consume();
            }
        }));
        shortcuts.add(new ShortcutRecord(TR.tr("shortcuts.elements.incrementSize"),
                new CustomKeyCombination(e -> {
            return e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD || "+".equals(e.getText()) || e.getCode() == KeyCode.EQUALS;
        }, KeyCodeCombination.SHORTCUT_DOWN, KeyCodesCombination.ALT_DOWN), e -> {
            if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                MainWindow.textTab.sizeSpinner.increment();
                e.consume();
            }else if(MainWindow.mainScreen.getSelected() instanceof VectorElement){
                MainWindow.paintTab.vectorStrokeWidth.increment();
                e.consume();
            }
        }));
        
        
        /*******************************/
        /********** LAZY: TAB **********/
        /*******************************/
        lazyShortcuts.add(new ShortcutRecord("",
                new KeyCodeCombination(KeyCode.TAB), e -> {
            if(!MainWindow.textTab.isSelected()){
                MainWindow.textTab.select();
                e.consume();
            }else if(!MainWindow.paintTab.isSelected()){
                MainWindow.paintTab.select();
                e.consume();
            }
        }));
        
        /*******************************/
        /*** LAZY: Pages Navigation ****/
        /*******************************/
        // Pages navigation
        lazyShortcuts.add(new ShortcutRecord("",
                new KeyCodesCombination(KeyCode.UP, KeyCode.KP_UP), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            MainWindow.mainScreen.navigateUp();
            e.consume();
        }));
        lazyShortcuts.add(new ShortcutRecord("",
                new KeyCodesCombination(KeyCode.DOWN, KeyCode.KP_DOWN), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            MainWindow.mainScreen.navigateDown();
            e.consume();
        }));
        lazyShortcuts.add(new ShortcutRecord("",
                new KeyCodesCombination(KeyCode.LEFT, KeyCode.KP_LEFT), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            MainWindow.mainScreen.navigateLeft();
            e.consume();
        }));
        lazyShortcuts.add(new ShortcutRecord("",
                new KeyCodesCombination(KeyCode.RIGHT, KeyCode.KP_RIGHT), e -> {
            if(!MainWindow.mainScreen.hasDocument(false)) return;
            MainWindow.mainScreen.navigateRight();
            e.consume();
        }));
        
        /*******************************/
        /**** LAZY: Edit pages mode ****/
        /*******************************/
        lazyShortcuts.add(new ShortcutRecord("",
                new KeyCodeCombination(KeyCode.DELETE), e -> {
            if(MainWindow.mainScreen.hasDocument(false) && MainWindow.mainScreen.isEditPagesMode()){
                MainWindow.mainScreen.document.pdfPagesRender.editor.deleteSelectedPages();
                e.consume();
            }
        }));
        lazyShortcuts.add(new ShortcutRecord("",
                new KeyCodeCombination(KeyCode.A, KeyCodeCombination.SHORTCUT_DOWN), e -> {
            if(MainWindow.mainScreen.hasDocument(false) && MainWindow.mainScreen.isEditPagesMode()){
                MainWindow.mainScreen.document.selectAll();
                e.consume();
            }
        }));
        
        
        /******************************/
        /*********** EVENTS ***********/
        /******************************/
        
        main.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            Optional<ShortcutRecord> first = shortcuts.stream()
                    .filter(entry -> entry.getCombination().match(e))
                    .filter(entry -> {
                        entry.getAction().accept(e);
                        return e.isConsumed();
                    }).findFirst();
            
            if(first.isEmpty()){
                if(Main.window.getScene().getFocusOwner() instanceof SideBar
                        || Main.window.getScene().getFocusOwner() instanceof Slider
                        || Main.window.getScene().getFocusOwner() instanceof Button
                        || (!(Main.window.getScene().getFocusOwner() instanceof Control)
                        && !(Main.window.getScene().getFocusOwner() instanceof Element)
                        && !(Main.window.getScene().getFocusOwner() instanceof KeyableHBox)
                )){
                    processLazyShortcuts(e);
                }
            }
        });
        
        main.setOnKeyPressed(this::processLazyShortcuts);
        
    }
    public void processLazyShortcuts(KeyEvent e){
        Optional<ShortcutRecord> first = lazyShortcuts.stream()
                .filter(entry -> entry.getCombination().match(e))
                .filter(entry -> {
                    entry.getAction().accept(e);
                    return e.isConsumed();
                }).findFirst();
        
    }
    // Returns String if used, null otherwise
    public String getShortcutNameIfExists(KeyCodeCombination combination){
        if(combination == null) return null;
        // KeyCombination::equals could be used, but it would not work for CustomKeyCombination that require events
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED,
                combination.getCode().getChar(), combination.getCode().getChar(), combination.getCode(),
                combination.getShift() == KeyCombination.ModifierValue.DOWN,
                combination.getControl() == KeyCombination.ModifierValue.DOWN,
                combination.getAlt() == KeyCombination.ModifierValue.DOWN,
                combination.getMeta() == KeyCombination.ModifierValue.DOWN);
        
        Optional<ShortcutRecord> first = Stream.concat(shortcuts.stream(), menuBarShortcuts.stream())
                .filter(s -> s.combination.match(event) && !s.getName().isEmpty())
                .findFirst();
        return first.map(ShortcutRecord::getName).orElse(null);
    }
    
    // Paint elements shortcuts
    
    private Optional<VectorGridElement> matchVectorShortcut(KeyEvent e){
        return MainWindow.paintTab.favouriteVectors.getList().getAllItems().stream()
                .filter(vector ->
                        vector.getVectorData().getKeyCodeCombination() != null && vector.getVectorData().getKeyCodeCombination().match(e))
                .findFirst();
    }
    private Optional<ImageGridElement> matchImageShortcut(KeyEvent e){
        return MainWindow.paintTab.favouriteImages.getList().getAllItems().stream()
                .filter(image ->
                        image.getImageData().getKeyCodeCombination() != null && image.getImageData().getKeyCodeCombination().match(e))
                .findFirst();
    }
    private void firePaintElementsShortcut(KeyEvent e){
        Optional<VectorGridElement> vectorData = matchVectorShortcut(e);
        if(vectorData.isPresent()){
            vectorData.get().addToDocument(false, true);
            e.consume();
            return;
        }
        Optional<ImageGridElement> imageData = matchImageShortcut(e);
        if(imageData.isPresent()){
            imageData.get().addToDocument(true);
            e.consume();
        }
    }
    
    private boolean numberPressed(int i, boolean alt){
        
        if(alt){ // Shortcut with Alt -> Change element color
            if(!MainWindow.mainScreen.hasDocument(false)) return false;
            
            if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                MainWindow.textTab.colorPicker.selectCustomColor(i - 1);
            }else if(MainWindow.mainScreen.getSelected() instanceof VectorElement){
                if(i == 0){
                    MainWindow.paintTab.doFillButton.selectedProperty().setValue(!MainWindow.paintTab.doFillButton.isSelected());
                }else{
                    MainWindow.paintTab.vectorFillColor.selectCustomColor(i - 1);
                }
            }
            
        }else{ // Shortcut without Alt -> Add favorite text element
            if(!MainWindow.mainScreen.hasDocument(false)) return false;
            
            if(MainWindow.textTab.treeView.favoritesSection.sortToggleBtn.isSelected()) i++;
            if(i <= MainWindow.textTab.treeView.favoritesSection.getChildren().size() && i != 0){
                ((TextTreeItem) MainWindow.textTab.treeView.favoritesSection.getChildren().get(i - 1)).addToDocument(false, false);
                MainWindow.textTab.selectItem();
            }else{
                return false;
            }
        }
        return true;
    }
    
    private boolean canBeginEndOnNode(Node node){
        if(node instanceof TextInputControl) return true;
        if(node instanceof Spinner<?> spinner){
            return spinner.isEditable();
        }
        return false;
    }
    public void registerMenuBarShortcut(KeyCombination combination, String name){
        menuBarShortcuts.add(new ShortcutRecord(name, combination));
    }

    private static class ShortcutRecord {
        private final String name;
        
        private final KeyCombination combination;
        private Consumer<KeyEvent> action;
        public ShortcutRecord(String name, KeyCombination combination, Consumer<KeyEvent> action){
            this.name = name;
            this.combination = combination;
            this.action = action;
        }
        public ShortcutRecord(String name, KeyCombination combination){
            this.name = name;
            this.combination = combination;
        }
        public String getName(){
            return name;
        }
        public KeyCombination getCombination(){
            return combination;
        }
        public Consumer<KeyEvent> getAction(){
            return action;
        }
    }
}
