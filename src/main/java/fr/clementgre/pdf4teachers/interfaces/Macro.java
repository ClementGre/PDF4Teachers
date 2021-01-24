package fr.clementgre.pdf4teachers.interfaces;


import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeTreeView;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextListItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TextTreeItem;
import fr.clementgre.pdf4teachers.panel.sidebar.texts.TreeViewSections.TextTreeSection;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;

public class Macro {

    public Macro(Scene main){

        main.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.TAB){
                if(MainWindow.leftBar.getSelectionModel().getSelectedItem() == MainWindow.textTab) MainWindow.leftBar.getSelectionModel().select(MainWindow.gradeTab);
                else MainWindow.leftBar.getSelectionModel().select(MainWindow.textTab);
                return;
            }

            if(e.isShortcutDown()){
                String keyName = e.getText();
                if(!e.isAltDown()){
                    try{
                        int i = Integer.parseInt(e.getCode().getChar());
                        if(i == 0) return;

                        if(MainWindow.textTab.treeView.favoritesSection.sortToggleBtn.isSelected()) i++;
                        if(i <= MainWindow.textTab.treeView.favoritesSection.getChildren().size()){
                            ((TextTreeItem) MainWindow.textTab.treeView.favoritesSection.getChildren().get(i-1)).addToDocument(false);
                            return;
                        }

                    }catch(NumberFormatException ignored){}
                    try{
                        int i = Integer.parseInt(keyName);
                        if(i == 0) return;

                        if(MainWindow.textTab.treeView.favoritesSection.sortToggleBtn.isSelected()) i++;
                        if(i <= MainWindow.textTab.treeView.favoritesSection.getChildren().size()){
                            ((TextTreeItem) MainWindow.textTab.treeView.favoritesSection.getChildren().get(i-1)).addToDocument(false);
                            return;
                        }

                    }catch(NumberFormatException ignored){}

                    if(e.getCode() == KeyCode.T){
                        MainWindow.leftBar.getSelectionModel().select(1);
                        MainWindow.textTab.newBtn.fire();
                        Element selected = MainWindow.mainScreen.getSelected();
                        if(selected != null){
                            if(selected instanceof TextElement){
                                selected.setRealX((int) (selected.getPage().getMouseX() * Element.GRID_WIDTH / selected.getPage().getWidth()));
                            }
                        }
                    }else if(e.getCode() == KeyCode.N){

                        int page = MainWindow.mainScreen.document.getCurrentPage() == -1 ? 0 : MainWindow.mainScreen.document.getCurrentPage();
                        int y = (int) MainWindow.mainScreen.document.pages.get(page).getMouseY();

                        MainWindow.leftBar.getSelectionModel().select(2);
                        MainWindow.gradeTab.treeView.getSelectionModel().select(GradeTreeView.getNextLogicGradeNonNull());

                    }else if(e.getCode() == KeyCode.G){
                        if(MainWindow.leftBar.getSelectionModel().getSelectedItem() != MainWindow.gradeTab) return;

                        GradeTreeItem item = (GradeTreeItem) MainWindow.gradeTab.treeView.getSelectionModel().getSelectedItem();
                        if(item == null) return;
                        if(item.isRoot()){
                            GradeElement element = MainWindow.gradeTab.newGradeElementAuto(item);
                            element.select();
                        }else{
                            GradeElement element = MainWindow.gradeTab.newGradeElementAuto(((GradeTreeItem) item.getParent()));
                            element.select();
                        }
                        // Update total (Fix the bug when a total is predefined (with no children))
                        item.makeSum(false);
                    }
                }else{ // SHORTCUT + ALT

                    try{
                        int i = Integer.parseInt(e.getCode().getChar())-1;
                        if(i == -1){
                            MainWindow.textTab.treeView.favoritesSection.listsManager.saveListBtn.fire();
                            return;
                        }
                        if(i < TextTreeSection.lists.size()){
                            int k = 0;
                            for(ArrayList<TextListItem> list : TextTreeSection.lists.values()){
                                if(k == i){
                                    MainWindow.leftBar.getSelectionModel().select(1);
                                    MainWindow.textTab.treeView.favoritesSection.listsManager.loadList(list, true);
                                    return;
                                }
                                k++;
                            }
                            return;
                        }

                    }catch(NumberFormatException ignored){}
                    try{
                        int i = Integer.parseInt(keyName)-1;
                        if(i == -1){
                            MainWindow.textTab.treeView.favoritesSection.listsManager.saveListBtn.fire();
                            return;
                        }
                        if(i < TextTreeSection.lists.size()){
                            int k = 0;
                            for(ArrayList<TextListItem> list : TextTreeSection.lists.values()){
                                if(k == i){
                                    MainWindow.leftBar.getSelectionModel().select(1);
                                    MainWindow.textTab.treeView.favoritesSection.listsManager.loadList(list, true);
                                    return;
                                }
                                k++;
                            }
                            return;
                        }

                    }catch(NumberFormatException ignored){}

                }



            }

        });

    }

}
