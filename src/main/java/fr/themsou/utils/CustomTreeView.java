package fr.themsou.utils;

import fr.themsou.document.editions.elements.NoDisplayTextElement;
import fr.themsou.main.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class CustomTreeView {

    public CustomTreeView(TreeView treeView){

        treeView.setCellFactory(new Callback<TreeView, TreeCell>() {
            @Override public TreeCell call(TreeView param) {
            return new TreeCell<String>() {
            @Override protected void updateItem(String text, boolean bln) {
                super.updateItem(text, bln);

                if(text != null) {

                    Text name = new Text(text);
                    setStyle("-fx-padding: 5 15;");
                    name.setFont(new Font(14));
                    setGraphic(name);

                }else{

                    if(getTreeItem() instanceof NoDisplayTextElement){
                        NoDisplayTextElement element = (NoDisplayTextElement) getTreeItem();

                        Text name = new Text(element.getText());
                        setStyle("-fx-padding: 5 15;");
                        name.setFill(element.getColor());
                        name.setFont(new Font(element.getFont().getFamily(), 14));
                        setGraphic(name);

                        ContextMenu menu = getNewMenu(element);
                        setContextMenu(menu);

                        setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent mouseEvent){
                                if(mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2){
                                    element.addToDocument();
                                }
                            }
                        });
                    }else{
                        Pane pane = new Pane();
                        setGraphic(pane);
                    }
                }
            }};}
        });

    }

    public ContextMenu getNewMenu(NoDisplayTextElement element){

        ContextMenu menu = new ContextMenu();
        MenuItem item1 = new MenuItem("Ajouter");
        MenuItem item2 = new MenuItem("Retirer");
        MenuItem item3 = new MenuItem("Ajouter aux favoris");
        MenuItem item4 = new MenuItem("Vider la liste");

        menu.getItems().addAll(item1, item2);
        if(!element.isFavorite()) menu.getItems().add(item3);
        menu.getItems().addAll(new SeparatorMenuItem(), item4);
        Builders.setMenuSize(menu);

        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                element.addToDocument();
            }
        });
        item2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Main.lbTextTab.removeSavedElement(element);
            }
        });
        item3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e){
                Main.lbTextTab.addSavedElement(new NoDisplayTextElement(element.getFont(), element.getText(), element.getColor(), true));
            }
        });
        item4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if(element.isFavorite()){
                    Main.lbTextTab.clearSavedFavoritesElements();
                }else{
                    Main.lbTextTab.clearSavedLastsElements();
                }

            }
        });
        return menu;

    }

}


