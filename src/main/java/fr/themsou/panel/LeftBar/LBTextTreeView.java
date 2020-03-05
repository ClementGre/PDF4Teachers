package fr.themsou.panel.LeftBar;

import fr.themsou.document.editions.elements.NoDisplayTextElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.w3c.dom.ls.LSOutput;

import javax.swing.*;

public class LBTextTreeView {


    public LBTextTreeView(TreeView treeView){

        treeView.setCellFactory(new Callback<TreeView, TreeCell>() {
            @Override public TreeCell call(TreeView param) {
                return new TreeCell<String>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if(empty){
                            setGraphic(null);
                            setStyle(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                            return;
                        }if(item != null){
                            if(item.equals("favoritesOptions")){
                                setStyle("-fx-padding: 0 0 0 -40; -fx-margin: 0; -fx-background-color: #cccccc;");
                                setGraphic(Main.lbTextTab.favoritesTextOptions);
                                return;
                            }if(item.equals("lastsOptions")){
                                setStyle("-fx-padding: 0 0 0 -40; -fx-margin: 0; -fx-background-color: #cccccc;");
                                setGraphic(Main.lbTextTab.lastsTextOptions);
                                return;
                            }if(item.equals("onFileOptions")){
                                setStyle("-fx-padding: 0 0 0 -40; -fx-margin: 0; -fx-background-color: #cccccc;");
                                setGraphic(Main.lbTextTab.onFileTextOptions);
                                return;
                            }


                            HBox box = new HBox();
                            box.setAlignment(Pos.CENTER);
                            setMaxHeight(30);
                            box.setPrefHeight(18);
                            setStyle("-fx-padding: 6 6 6 2; -fx-background-color: #cccccc;");
                            box.setStyle("-fx-padding: -6 -6 -6 0;");

                            Text name = new Text();
                            name.setFont(new Font(14));
                            box.getChildren().add(name);

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);
                            box.getChildren().add(spacer);

                            Pane toggle = new Pane();
                            box.getChildren().add(toggle);

                            if(item.equals("favoritesText")){
                                name.setText("Éléments Favoris");
                                toggle.getChildren().add(Main.lbTextTab.favoritesTextToggleOption);
                            }if(item.equals("lastsText")){
                                name.setText("Éléments Précédents");
                                box.getChildren().add(Main.lbTextTab.lastsTextToggleOption);
                            }if(item.equals("onFileText")){
                                name.setText("Éléments sur ce document");
                                box.getChildren().add(Main.lbTextTab.onFileTextToggleOption);
                            }
                            setGraphic(box);


                            return;
                        }

                        if(getTreeItem() instanceof NoDisplayTextElement){
                            setStyle(null);
                            NoDisplayTextElement element = (NoDisplayTextElement) getTreeItem();

                            VBox nameParts = new VBox();
                            String fullName = element.getText();

                            setGraphic(nameParts);
                            setStyle("-fx-padding: 3 -10;");

                            int index = 0;
                            while(index < fullName.split(" ").length) {
                                String namePart = fullName.split(" ")[index];
                                Text name = new Text(namePart);
                                index++;

                                name.setFill(element.getColor());
                                name.setFont(TextElement.getFont(element.getFont().getFamily(), false, false, 14));

                                while(index < fullName.split(" ").length){
                                    String lastNamePart = namePart;
                                    namePart += " " + fullName.split(" ")[index];
                                    name.setText(namePart);

                                    if(name.getBoundsInParent().getWidth() > 235){
                                        name.setText(lastNamePart);
                                        break;
                                    }
                                    index++;
                                }
                                nameParts.getChildren().add(name);
                            }

                            ContextMenu menu = getNewMenu(element);
                            setContextMenu(menu);

                            setOnMouseClicked(new EventHandler<MouseEvent>(){
                                public void handle(MouseEvent mouseEvent){
                                    if(mouseEvent.getButton().equals(MouseButton.PRIMARY) && getGraphic() instanceof VBox){
                                        element.addToDocument();
                                        if(element.getType() == NoDisplayTextElement.FAVORITE_TYPE){
                                            Main.lbTextTab.favoritesTextSortManager.simulateCall();
                                        }else if(element.getType() == NoDisplayTextElement.LAST_TYPE){
                                            Main.lbTextTab.lastsTextSortManager.simulateCall();
                                        }
                                    }
                                }
                            });
                        }else{
                            setStyle(null);
                            setGraphic(null);
                            setContextMenu(null);
                            setOnMouseClicked(null);
                        }
                    }
                };
            }
        });

    }

    public ContextMenu getNewMenu(NoDisplayTextElement element){

        ContextMenu menu = new ContextMenu();
        MenuItem item1 = new MenuItem("Ajouter");
        MenuItem item2 = new MenuItem("Retirer");
        MenuItem item3 = new MenuItem("Ajouter aux favoris");
        MenuItem item6 = new MenuItem("Vider la liste");
        MenuItem item7 = new MenuItem("Ajouter aux éléments précédents");


        // Ajouter les items en fonction du type
        menu.getItems().addAll(item1, item2);
        if(element.getType() != NoDisplayTextElement.FAVORITE_TYPE) menu.getItems().add(item3);
        if(element.getType() == NoDisplayTextElement.ONFILE_TYPE) menu.getItems().add(item7);
        else menu.getItems().addAll(new SeparatorMenuItem(), item6);
        Builders.setMenuSize(menu);

        // Définis les actions des boutons
        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                element.addToDocument();
                if(element.getType() == NoDisplayTextElement.FAVORITE_TYPE){
                    Main.lbTextTab.favoritesTextSortManager.simulateCall();
                }else if(element.getType() == NoDisplayTextElement.LAST_TYPE){
                    Main.lbTextTab.lastsTextSortManager.simulateCall();
                }

            }
        });
        item2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if(element.getType() == NoDisplayTextElement.ONFILE_TYPE){
                    element.getCore().delete();
                }else{
                    Main.lbTextTab.removeSavedElement(element);
                }
            }
        });
        item3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Main.lbTextTab.addSavedElement(new NoDisplayTextElement(element.getFont(), element.getText(), element.getColor(), NoDisplayTextElement.FAVORITE_TYPE, 0, System.currentTimeMillis()/1000));
                if(element.getType() == NoDisplayTextElement.LAST_TYPE){
                    if(Main.settings.isRemoveElementInPreviousListWhenAddingToFavorites()){
                        Main.lbTextTab.removeSavedElement(element);
                    }
                }
            }
        });
        item6.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(element.getType() == NoDisplayTextElement.FAVORITE_TYPE){
                    Main.lbTextTab.clearSavedFavoritesElements();
                }else if(element.getType() == NoDisplayTextElement.LAST_TYPE){
                    Main.lbTextTab.clearSavedLastsElements();
                }

            }
        });
        item7.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Main.lbTextTab.addSavedElement(new NoDisplayTextElement(element.getFont(), element.getText(), element.getColor(), NoDisplayTextElement.LAST_TYPE, 0, System.currentTimeMillis()/1000));
            }
        });
        return menu;

    }

}


