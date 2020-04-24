package fr.themsou.panel.leftBar.files;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.Main;
import fr.themsou.panel.leftBar.notes.NoteTreeItem;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.TR;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
import java.util.Collections;
import java.util.Optional;

public class LBFilesListView {


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
                    HBox nameBox = new HBox();
                    Text name = new Text(file.getName().replace(".pdf", ""));
                    name.setFont(Font.font(null, FontWeight.NORMAL, 12));

                    Text path = new Text(file.getAbsolutePath().replace(System.getProperty("user.home"),"~").replace(file.getName(), ""));
                    setStyle("-fx-padding: 2 15;");

                    try{
                        double[] elementsCount = Edition.countElements(Edition.getEditFile(file));

                        if(elementsCount.length > 0){ // has edit file
                            String note = (elementsCount[4] == -1 ? "?" : NoteTreeItem.format.format(elementsCount[4])) + "/" + NoteTreeItem.format.format(elementsCount[5]);
                            if(elementsCount[0] > 0){ // Has Elements

                                name.setFont(Font.font(null, FontWeight.BOLD, 12));

                                path.setText(path.getText() + " | " + NoteTreeItem.format.format(elementsCount[0]) + " " + TR.tr("Éléments") + " | " + note);
                                setTooltip(new Tooltip(NoteTreeItem.format.format(elementsCount[0]) + " " + TR.tr("Éléments") + " | " + note + "\n" + NoteTreeItem.format.format(elementsCount[1]) + " " + TR.tr("Commentaires") + "\n" + NoteTreeItem.format.format(elementsCount[2]) + "/" + NoteTreeItem.format.format(elementsCount[6]) + " " + TR.tr("Notes") + "\n" + NoteTreeItem.format.format(elementsCount[3]) + " " + TR.tr("Figures")));

                                if(elementsCount[2] == elementsCount[6]){
                                    ImageView check = Builders.buildImage(getClass().getResource("/img/FilesTab/check.png")+"", 0, 0);
                                    HBox.setMargin(check, new Insets(0, 4, 0, 0));
                                    nameBox.getChildren().add(check);
                                }

                            }else{ // Don't have elements
                                path.setText(path.getText() + " | " + TR.tr("Non édité") + " | " + note);
                                setTooltip(new Tooltip(TR.tr("Non édité") + " | " + note + "\n" + NoteTreeItem.format.format(elementsCount[6]) + " " + TR.tr("Barèmes")));
                            }
                        }else{ // don't have edit file
                            path.setText(path.getText() + " | " + TR.tr("Non édité"));
                            setTooltip(new Tooltip(TR.tr("Non édité")));
                        }
                    }catch(Exception e){
                        path.setStyle("-fx-collor: red;");
                        path.setText(path.getText() + " | " + TR.tr("Impossible de récupérer les informations"));
                        setTooltip(new Tooltip(e.getMessage()));
                    }

                    path.setFont(new Font(10));
                    nameBox.getChildren().add(name);
                    pane.getChildren().addAll(nameBox, path);
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

        item1.setOnAction(e -> Platform.runLater(() -> Main.mainScreen.openFile(new File(((MenuItem)e.getSource()).getParentPopup().getId()))));

        item2.setOnAction(e -> Main.lbFilesTab.removeFile(new File(((MenuItem)e.getSource()).getParentPopup().getId()), true));

        item3.setOnAction(e -> Edition.clearEdit(new File(((MenuItem)e.getSource()).getParentPopup().getId()), true));

        item4.setOnAction(e -> {

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


        });
        item5.setOnAction(e -> {
            if(new File(((MenuItem)e.getSource()).getParentPopup().getId()).exists()){

                if(Main.mainScreen.hasDocument(false)){
                    if(Main.mainScreen.document.getFile().getAbsolutePath().equals(((MenuItem)e.getSource()).getParentPopup().getId())){
                        Main.mainScreen.document.save();
                    }
                }

                new ExportWindow(Collections.singletonList(new File(((MenuItem)e.getSource()).getParentPopup().getId())));
            }

        });
        item6.setOnAction(e -> Main.lbFilesTab.clearFiles(true));
        return menu;

    }

}