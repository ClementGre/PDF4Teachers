package fr.themsou.panel.LeftBar;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class LBFilesListView {

    public static File lastDirChoosed = new File(System.getProperty("user.home"));

    public LBFilesListView(ListView listView){

        listView.setOnMouseClicked((MouseEvent event) -> {
            listView.refresh();
        });

        listView.setCellFactory(new Callback<ListView<File>, ListCell<File>>(){
            @Override public ListCell<File> call(ListView<File> listView){
            return new ListCell<File>(){
            @Override protected void updateItem(File file, boolean bln) {
                super.updateItem(file, bln);

                if(file != null) {
                    if(!file.exists()){
                        Main.lbFilesTab.removeFile(file, false);
                        return;
                    }
                    VBox pane = new VBox();
                    Text name = new Text(file.getName().replace(".pdf", ""));
                    Text path = new Text(file.getAbsolutePath().replace(System.getProperty("user.home"),"~").replace(file.getName(), ""));
                    setStyle("-fx-padding: 2 15;");

                    if(Edition.getEditFile(file).exists()){
                        name.setFont(Font.font(null, FontWeight.BOLD, 12));
                        try{
                            Integer[] elementsCount = Edition.countElements(Edition.getEditFile(file));

                            path.setText(path.getText() + " | " + elementsCount[0] + " " + TR.tr("Éléments"));
                            setTooltip(new Tooltip(elementsCount[0] + " " + TR.tr("Éléments") + "\n" + elementsCount[1] + " " + TR.tr("Commentaires") + "\n" + elementsCount[2] + " " + TR.tr("Notes") + "\n" + elementsCount[3] + "\n" + TR.tr("Figures")));

                        }catch(Exception e){ e.printStackTrace();}
                    }else{
                        name.setFont(Font.font(null, FontWeight.NORMAL, 12));
                        path.setText(path.getText() + " | " + "Non édité");
                        setTooltip(new Tooltip(TR.tr("Non édité")));
                    }


                    path.setFont(new Font(10));
                    pane.getChildren().addAll(name, path);
                    setGraphic(pane);

                    ContextMenu menu = getNewMenu();
                    menu.setId(file.getAbsolutePath());
                    setContextMenu(menu);

                    pane.setOnMouseClicked(mouseEvent -> {
                        if(mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2){
                            Main.mainScreen.openFile(file);
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
        NodeMenuItem item1 = new NodeMenuItem(new HBox(), TR.tr("Ouvrir"), -1, false);
        item1.setToolTip(TR.tr("Ouvre le fichier avec l'éditeur de PDF4Teachers. Il est aussi possible de l'ouvrir avec un double clic."));
        NodeMenuItem item2 = new NodeMenuItem(new HBox(), TR.tr("Retirer"), -1, false);
        item2.setToolTip(TR.tr("Retire le fichier de la liste. Le fichier ne sera en aucun cas supprimé."));
        NodeMenuItem item3 = new NodeMenuItem(new HBox(), TR.tr("Supprimer l'édition"), -1, false);
        item3.setToolTip(TR.tr("Réinitialise l'édition du document, retire tous les éléments ajoutés auparavant."));
        NodeMenuItem item4 = new NodeMenuItem(new HBox(), TR.tr("Supprimer le fichier"), -1, false);
        item4.setToolTip(TR.tr("Supprime le fichier PDF sur l'ordinateur."));
        NodeMenuItem item5 = new NodeMenuItem(new HBox(), TR.tr("Exporter"), -1, false);
        item5.setToolTip(TR.tr("Crée un nouveau fichier PDF à partir de celui-ci, avec tous les éléments ajoutés."));
        NodeMenuItem item6 = new NodeMenuItem(new HBox(), TR.tr("Vider la liste"), -1, false);
        item6.setToolTip(TR.tr("Retire tous les fichiers de la liste. Les fichiers ne seront en aucun cas supprimé."));

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
                alert.setTitle(TR.tr("Confirmation"));
                alert.setHeaderText(TR.tr("Êtes vous sûr de vouloir supprimer le document et son édition ?"));
                alert.setContentText(TR.tr("Cette action est irréversible."));

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