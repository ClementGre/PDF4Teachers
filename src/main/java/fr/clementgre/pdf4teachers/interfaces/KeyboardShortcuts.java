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
import fr.clementgre.pdf4teachers.panel.sidebar.SideBar;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.VectorGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.stream.Stream;

public class KeyboardShortcuts {

    public KeyboardShortcuts(Scene main){

        main.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            
            if(checkPaintElementsShortcuts(e)){
                e.consume();
                return;
            }

            if(e.isShortcutDown()){
                String text = e.getText();
                if(!e.isAltDown()){
                    if(e.getCode() == KeyCode.T){
                        if(!MainWindow.mainScreen.hasDocument(false)) return;

                        SideBar.selectTab(MainWindow.textTab);
                        TextElement element = MainWindow.textTab.newTextElement(!e.isShiftDown());
                        element.setRealX(element.getPage().getNewElementXOnGrid(false));
                        return;

                    }else if(e.getCode() == KeyCode.D){
                        SideBar.selectTab(MainWindow.paintTab);
                        MainWindow.paintTab.newVectorDrawing(e.isShiftDown());
                        return;

                    }else if(e.getCode() == KeyCode.N){
                        if(!MainWindow.mainScreen.hasDocument(false)) return;

                        SideBar.selectTab(MainWindow.gradeTab);
                        MainWindow.gradeTab.treeView.getSelectionModel().select(GradeTreeView.getNextLogicGradeNonNull());
                        return;

                    }else if(e.getCode() == KeyCode.G){
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
                        return;
                    }else if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.KP_UP || e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD || "+".equals(text) || e.getCode() == KeyCode.EQUALS){
                        e.consume();
                        MainWindow.mainScreen.zoomMore();
                        return;
                    }else if(e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.KP_DOWN){
                        e.consume();
                        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.zoomOperator.fitWidth(false, false);
                        return;
                    }else if(e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT || "-".equals(text)){
                        e.consume();
                        MainWindow.mainScreen.zoomLess();
                        return;
                    }

                    try{
                        int i = Integer.parseInt(e.getCode().getChar());
                        numberPressed(i, true, false);
                        return;
                    }catch(NumberFormatException ignored){}
                    try{
                        int i = Integer.parseInt(text);
                        numberPressed(i, true, false);
                        return;
                    }catch(NumberFormatException ignored){}

                }else{ // SHORTCUT + ALT
                    if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.KP_UP){
                        e.consume();
                        MainWindow.mainScreen.pageUp();
                        return;
                    }else if(e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.KP_DOWN){
                        e.consume();
                        MainWindow.mainScreen.pageDown();
                        return;
                    }else if(e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.KP_LEFT){
                        e.consume();
                        MainWindow.filesTab.loadPreviousFile();
                        return;
                    }else if(e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.KP_RIGHT){
                        e.consume();
                        MainWindow.filesTab.loadNextFile();
                        return;
                    }else if(e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD || "+".equals(text) || e.getCode() == KeyCode.EQUALS){
                        e.consume();
                        if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                            MainWindow.textTab.sizeSpinner.increment();
                        }
                        return;
                    }else if(e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT || "-".equals(text)){
                        e.consume();
                        if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                            MainWindow.textTab.sizeSpinner.decrement();
                        }
                        return;
                    }

                    try{
                        int i = Integer.parseInt(e.getCode().getChar());
                        numberPressed(i, true, true);
                        return;
                    }catch(NumberFormatException ignored){}
                    try{
                        int i = Integer.parseInt(text);
                        numberPressed(i, true, true);
                        return;
                    }catch(NumberFormatException ignored){}

                }


            }else{ // NO SHORTCUT PRESSED

                if(e.getCode() == KeyCode.BEGIN || e.getCode() == KeyCode.HOME){ // NAVIGATE IN DOCUMENT SHORTCUTS
                    if(!canBeginEndOnNode(Main.window.getScene().getFocusOwner())){ // Do not execute custom actions if a text field of a spinner is focused.
                        e.consume();
                        MainWindow.mainScreen.navigateBegin();
                    }
                    return;
                }else if(e.getCode() == KeyCode.END){
                    if(!canBeginEndOnNode(Main.window.getScene().getFocusOwner())){ // Do not execute custom actions if a text field of a spinner is focused.
                        e.consume();
                        MainWindow.mainScreen.navigateEnd();
                    }
                    return;
                }else if(e.getCode() == KeyCode.PAGE_UP){
                    e.consume();
                    MainWindow.mainScreen.pageUp();
                    return;
                }else if(e.getCode() == KeyCode.PAGE_DOWN){
                    e.consume();
                    MainWindow.mainScreen.pageDown();
                    return;
                }
            }

            if(Main.window.getScene().getFocusOwner() instanceof SideBar
                    || Main.window.getScene().getFocusOwner() instanceof Slider
                    || Main.window.getScene().getFocusOwner() instanceof Button
                    || (
                    !(Main.window.getScene().getFocusOwner() instanceof Control)
                            && !(Main.window.getScene().getFocusOwner() instanceof Element)
                            && !(Main.window.getScene().getFocusOwner() instanceof KeyableHBox)
            )){
                MainWindow.keyboardShortcuts.reportKeyPressedForMultipleUsesKeys(e);
            }

        });

        main.setOnKeyPressed(this::reportKeyPressedForMultipleUsesKeys);

    }
    
    private boolean checkPaintElementsShortcuts(KeyEvent e){
        return Stream.concat(
                MainWindow.paintTab.favouriteVectors.getList().getAllItems().stream(),
                MainWindow.paintTab.favouriteImages.getList().getAllItems().stream())
            .anyMatch(item -> {
                if(item instanceof VectorGridElement vector){
                    if(vector.getVectorData().getKeyCodeCombination() != null && vector.getVectorData().getKeyCodeCombination().match(e)){
                        vector.addToDocument(false, true);
                        return true;
                    }
                }else if(item instanceof ImageGridElement image){
                    if(image.getImageData().getKeyCodeCombination() != null && image.getImageData().getKeyCodeCombination().match(e)){
                        image.addToDocument(true);
                        return true;
                    }
                }
                return false;
            });
    }

    public void reportKeyPressedForMultipleUsesKeys(KeyEvent e){
        if(!e.isShortcutDown()){ // NO SHORTCUT PRESSED

            if(e.getCode() == KeyCode.TAB){

                if(!MainWindow.textTab.isSelected()){
                    MainWindow.textTab.select();
                    e.consume();
                }else if(!MainWindow.paintTab.isSelected()){
                    MainWindow.paintTab.select();
                    e.consume();
                }

            }else if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.KP_UP){
                e.consume();
                MainWindow.mainScreen.navigateUp();
            }else if(e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.KP_DOWN){
                e.consume();
                MainWindow.mainScreen.navigateDown();
            }else if(e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.KP_LEFT){
                e.consume();
                MainWindow.mainScreen.navigateLeft();
            }else if(e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.KP_RIGHT){
                e.consume();
                MainWindow.mainScreen.navigateRight();
            }else if(MainWindow.mainScreen.isEditPagesMode() && e.getCode() == KeyCode.DELETE){
                e.consume();
                MainWindow.mainScreen.document.pdfPagesRender.editor.deleteSelectedPages();
            }
        }else{ // SHORTCUT PRESSED
            if(MainWindow.mainScreen.isEditPagesMode() && e.getCode() == KeyCode.A){
                e.consume();
                MainWindow.mainScreen.document.selectAll();
            }
        }
    }

    private void numberPressed(int i, boolean shortcut, boolean alt){

        if(shortcut){
            if(alt){ // Shortcut with Alt -> Change element color
                if(!MainWindow.mainScreen.hasDocument(false)) return;

                if(MainWindow.mainScreen.getSelected() instanceof TextElement){
                    MainWindow.textTab.colorPicker.selectCustomColor(i-1);
                }else if(MainWindow.mainScreen.getSelected() instanceof VectorElement){
                    if(i == 0){
                        MainWindow.paintTab.doFillButton.selectedProperty().setValue(!MainWindow.paintTab.doFillButton.isSelected());
                    }else{
                        MainWindow.paintTab.vectorFillColor.selectCustomColor(i-1);
                    }
                }

            }else{ // Shortcut without Alt -> Add favorite text element
                if(!MainWindow.mainScreen.hasDocument(false)) return;

                if(MainWindow.textTab.treeView.favoritesSection.sortToggleBtn.isSelected()) i++;
                if(i <= MainWindow.textTab.treeView.favoritesSection.getChildren().size() && i != 0){
                    ((TextTreeItem) MainWindow.textTab.treeView.favoritesSection.getChildren().get(i - 1)).addToDocument(false, false);
                    MainWindow.textTab.selectItem();
                }
            }
        }

    }

    private boolean canBeginEndOnNode(Node node){
         if(node instanceof TextInputControl) return true;
         else if(node instanceof Spinner<?> spinner){
             return spinner.isEditable();
         }
         return false;
    }

}
