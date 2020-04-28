package fr.themsou.utils;


import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.notes.NoteTreeView;
import fr.themsou.panel.leftBar.texts.TextListItem;
import fr.themsou.panel.leftBar.texts.TextTreeItem;
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
                        int i = Integer.parseInt(keyName);
                        System.out.println(i);
                        if(i == 0) return;

                        if(Main.lbTextTab.favoritesTextToggleOption.isSelected()) i++;
                        if(i <= Main.lbTextTab.favoritesText.getChildren().size()){
                            ((TextTreeItem) Main.lbTextTab.favoritesText.getChildren().get(i-1)).addToDocument(false, false);
                            return;
                        }

                    }catch(NumberFormatException ignored){}

                    if(e.getCode() == KeyCode.T){
                        Main.leftBar.getSelectionModel().select(1);
                        Main.lbTextTab.newBtn.fire();
                        Element selected = Main.mainScreen.getSelected();
                        if(selected != null){
                            if(selected instanceof TextElement){
                                ((TextElement) selected).setRealX((int) (selected.getPage().getMouseX() * Element.GRID_WIDTH / selected.getPage().getWidth()));
                            }
                        }
                    }else if(e.getCode() == KeyCode.N){

                        int page = Main.mainScreen.document.getCurrentPage() == -1 ? 0 : Main.mainScreen.document.getCurrentPage();
                        int y = (int) Main.mainScreen.document.pages.get(page).getMouseY();

                        Main.leftBar.getSelectionModel().select(2);
                        Main.lbNoteTab.treeView.getSelectionModel().select(NoteTreeView.getNextNote(page, y));
                    }
                }else{
                    try{
                        int i = Integer.parseInt(keyName)-1;
                        if(i == -1){
                            Main.lbTextTab.listsManager.saveListBtn.fire();
                            return;
                        }
                        if(i < Main.lbTextTab.favoriteLists.size()){
                            int k = 0;
                            for(ArrayList<TextListItem> list : Main.lbTextTab.favoriteLists.values()){
                                if(k == i){
                                    Main.leftBar.getSelectionModel().select(1);
                                    Main.lbTextTab.listsManager.loadList(list);
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
