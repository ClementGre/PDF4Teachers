package fr.themsou.utils;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.util.Collections;
import java.util.Optional;

public class CustomListView {

    public CustomListView(ListView listView){

        listView.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override public ListCell<File> call(ListView<File> arg0){
            return new ListCell<File>(){
            @Override protected void updateItem(File file, boolean bln) {
                super.updateItem(file, bln);

                if(file != null) {
                    VBox pane = new VBox();
                    Text name = new Text(file.getName().replace(".pdf", ""));
                    Text path = new Text(file.getAbsolutePath().replace(System.getProperty("user.home"),"~").replace(file.getName(), ""));
                    setStyle("-fx-padding: 5 15;");
                    path.setFont(new Font(10));
                    pane.getChildren().addAll(name, path);
                    setGraphic(pane);

                    ContextMenu menu = getNewMenu();
                    menu.setId(file.getAbsolutePath());
                    setContextMenu(menu);

                    pane.setOnMouseClicked(new EventHandler<MouseEvent>(){
                        public void handle(MouseEvent mouseEvent){
                            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                                if(mouseEvent.getClickCount() == 2){
                                    Main.mainScreen.openFile(file);
                                }
                            }
                        }
                    });

                }else{
                    getChildren().clear();
                }
            }};}
        });

    }

    public ContextMenu getNewMenu(){

        ContextMenu menu = new ContextMenu();
        MenuItem item1 = new MenuItem("Ouvrir");
        MenuItem item2 = new MenuItem("Retirer");
        MenuItem item3 = new MenuItem("Supprimer l'édition");
        MenuItem item4 = new MenuItem("Supprimer le fichier");
        MenuItem item5 = new MenuItem("Exporter");
        MenuItem item6 = new MenuItem("Vider la liste");
        menu.getItems().addAll(item1, item2, item3, item4, item5, new SeparatorMenuItem(), item6);
        Builders.setMenuSize(menu);

        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        Main.mainScreen.openFile(new File(((MenuItem)e.getSource()).getParentPopup().getId()));
                    }
                });


            }
        });
        item2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Main.lbFilesTab.removeFile(new File(((MenuItem)e.getSource()).getParentPopup().getId()), true);

            }
        });
        item3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Edition.clearEdit(new File(((MenuItem)e.getSource()).getParentPopup().getId()), true);
            }
        });
        item4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(alert);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Êtes vous sûr de vouloir supprimer le document et son édition ?");
                alert.setContentText("Cette action est irréversible.");

                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == ButtonType.OK){
                    Main.lbFilesTab.removeFile(new File(((MenuItem)e.getSource()).getParentPopup().getId()), false);
                    Edition.clearEdit(new File(((MenuItem)e.getSource()).getParentPopup().getId()), false);
                    new File(((MenuItem)e.getSource()).getParentPopup().getId()).delete();
                }


            }
        });
        item5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if(new File(((MenuItem)e.getSource()).getParentPopup().getId()).exists()){

                    if(Main.mainScreen.hasDocument(false)){
                        if(Main.mainScreen.document.getFile().getAbsolutePath().equals(((MenuItem)e.getSource()).getParentPopup().getId())){
                            Main.mainScreen.document.save();
                        }
                    }

                    new ExportWindow(Collections.singletonList(new File(((MenuItem)e.getSource()).getParentPopup().getId())));
                }

            }
        });
        item6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Main.lbFilesTab.clearFiles(true);
            }
        });
        return menu;

    }

}


