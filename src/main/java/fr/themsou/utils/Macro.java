package fr.themsou.utils;


import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.panel.leftBar.texts.TextListItem;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
import fr.themsou.windows.MainWindow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import java.util.ArrayList;

public class Macro {

    public Macro(Pane main){

        main.setOnKeyPressed(e -> {
            if(e.isShortcutDown()){
                String keyName = e.getText();
                if(!e.isAltDown()){
                    try{
                        int i = Integer.parseInt(e.getCode().getChar());
                        if(i == 0) return;

                        if(MainWindow.lbTextTab.favoritesTextToggleOption.isSelected()) i++;
                        if(i <= MainWindow.lbTextTab.favoritesText.getChildren().size()){
                            ((TextTreeItem) MainWindow.lbTextTab.favoritesText.getChildren().get(i-1)).addToDocument(false);
                            return;
                        }

                    }catch(NumberFormatException ignored){}
                    try{
                        int i = Integer.parseInt(keyName);
                        if(i == 0) return;

                        if(MainWindow.lbTextTab.favoritesTextToggleOption.isSelected()) i++;
                        if(i <= MainWindow.lbTextTab.favoritesText.getChildren().size()){
                            ((TextTreeItem) MainWindow.lbTextTab.favoritesText.getChildren().get(i-1)).addToDocument(false);
                            return;
                        }

                    }catch(NumberFormatException ignored){}

                    if(e.getCode() == KeyCode.T){
                        MainWindow.leftBar.getSelectionModel().select(1);
                        MainWindow.lbTextTab.newBtn.fire();
                        Element selected = MainWindow.mainScreen.getSelected();
                        if(selected != null){
                            if(selected instanceof TextElement){
                                ((TextElement) selected).setRealX((int) (selected.getPage().getMouseX() * Element.GRID_WIDTH / selected.getPage().getWidth()));
                            }
                        }
                    }else if(e.getCode() == KeyCode.N){

                        int page = MainWindow.mainScreen.document.getCurrentPage() == -1 ? 0 : MainWindow.mainScreen.document.getCurrentPage();
                        int y = (int) MainWindow.mainScreen.document.pages.get(page).getMouseY();

                        MainWindow.leftBar.getSelectionModel().select(2);
                        MainWindow.lbNoteTab.treeView.getSelectionModel().select(NoteTreeView.getNextNote(page, y));
                    }
                }else{
                    try{
                        int i = Integer.parseInt(e.getCode().getChar())-1;
                        if(i == -1){
                            MainWindow.lbTextTab.listsManager.saveListBtn.fire();
                            return;
                        }
                        if(i < MainWindow.lbTextTab.favoriteLists.size()){
                            int k = 0;
                            for(ArrayList<TextListItem> list : MainWindow.lbTextTab.favoriteLists.values()){
                                if(k == i){
                                    MainWindow.leftBar.getSelectionModel().select(1);
                                    MainWindow.lbTextTab.listsManager.loadList(list);
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
                            MainWindow.lbTextTab.listsManager.saveListBtn.fire();
                            return;
                        }
                        if(i < MainWindow.lbTextTab.favoriteLists.size()){
                            int k = 0;
                            for(ArrayList<TextListItem> list : MainWindow.lbTextTab.favoriteLists.values()){
                                if(k == i){
                                    MainWindow.leftBar.getSelectionModel().select(1);
                                    MainWindow.lbTextTab.listsManager.loadList(list);
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
