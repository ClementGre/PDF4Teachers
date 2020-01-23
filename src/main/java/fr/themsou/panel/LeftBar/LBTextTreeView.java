package fr.themsou.panel.LeftBar;

import fr.themsou.document.editions.elements.NoDisplayTextElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.w3c.dom.ls.LSOutput;

public class LBTextTreeView {

    public LBTextTreeView(TreeView treeView){

        treeView.setCellFactory(new Callback<TreeView, TreeCell>() {
            @Override public TreeCell call(TreeView param) {
            return new TreeCell<String>() {
            @Override protected void updateItem(String text, boolean bln) {
                super.updateItem(text, bln);

                if(text != null) {

                    Text name = new Text(text);
                    name.setFont(new Font(14));
                    setStyle("-fx-padding: 5 0;");
                    setGraphic(name);

                }else{

                    if(getTreeItem() instanceof NoDisplayTextElement){
                        NoDisplayTextElement element = (NoDisplayTextElement) getTreeItem();



                        VBox nameParts = new VBox();
                        String fullName = element.getText();
                        int index = 0;
                        while (index < fullName.length()) {
                            String namePart = fullName.substring(index, Math.min(index + 35,fullName.length()));

                            Text name = new Text(namePart);
                            name.setFill(element.getColor());
                            name.setFont(TextElement.getFont(element.getFont().getFamily(), false, false, 14));

                            nameParts.getChildren().add(name);
                            index += 35;
                        }

                        setGraphic(nameParts);
                        setStyle("-fx-padding: 3 -15;");

                        ContextMenu menu = getNewMenu(element);
                        setContextMenu(menu);

                        setOnMouseClicked(new EventHandler<MouseEvent>(){
                            public void handle(MouseEvent mouseEvent){
                                if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
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
        MenuItem item4 = new MenuItem("Monter");
        MenuItem item5 = new MenuItem("Descendre");
        MenuItem item6 = new MenuItem("Vider la liste");

        if(element.isFavorite()){
            item4.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.userData.favoritesText.getChildren().indexOf(element) <= 0;}, Bindings.size(Main.userData.favoritesText.getChildren())));
            item5.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.userData.favoritesText.getChildren().indexOf(element) >= Main.userData.favoritesText.getChildren().size()-1;}, Bindings.size(Main.userData.favoritesText.getChildren())));
        }else{
            item4.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.userData.lastsText.getChildren().indexOf(element) <= 0;}, Bindings.size(Main.userData.lastsText.getChildren())));
            item5.disableProperty().bind(Bindings.createBooleanBinding(() -> {return Main.userData.lastsText.getChildren().indexOf(element) >= Main.userData.lastsText.getChildren().size()-1;}, Bindings.size(Main.userData.lastsText.getChildren())));
        }


        menu.getItems().addAll(item1, item2);
        if(!element.isFavorite()) menu.getItems().add(item3);
        menu.getItems().addAll(new SeparatorMenuItem(), item4, item5, new SeparatorMenuItem(), item6);
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
            @Override public void handle(ActionEvent e) {
                Main.lbTextTab.ascendElement(element);
            }
        });
        item5.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Main.lbTextTab.descendElement(element);
            }
        });
        item6.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
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


