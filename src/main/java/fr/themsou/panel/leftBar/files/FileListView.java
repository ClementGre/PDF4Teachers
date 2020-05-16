package fr.themsou.panel.leftBar.files;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.export.ExportWindow;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.NodeMenuItem;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

public class FileListView extends ListView<File>{


    public FileListView(){

        setStyle("-fx-border-width: 0px;");
        setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);

        setOnMouseClicked((MouseEvent event) -> {
            refresh();
        });

        setCellFactory(param -> new FileListItem());
    }

    public static ContextMenu getNewMenu(){

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

        item1.setOnAction(e -> Platform.runLater(() -> MainWindow.mainScreen.openFile(new File(((MenuItem)e.getSource()).getParentPopup().getId()))));

        item2.setOnAction(e -> MainWindow.lbFilesTab.removeFile(new File(((MenuItem)e.getSource()).getParentPopup().getId())));

        item3.setOnAction(e -> Edition.clearEdit(new File(((MenuItem)e.getSource()).getParentPopup().getId()), true));

        item4.setOnAction(e -> {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            Builders.secureAlert(alert);
            alert.setTitle(TR.tr("Confirmation"));
            alert.setHeaderText(TR.tr("Êtes vous sûr de vouloir supprimer le document et son édition ?"));
            alert.setContentText(TR.tr("Cette action est irréversible."));

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                MainWindow.lbFilesTab.removeFile(new File(((MenuItem)e.getSource()).getParentPopup().getId()));
                Edition.clearEdit(new File(((MenuItem)e.getSource()).getParentPopup().getId()), false);
                new File(((MenuItem)e.getSource()).getParentPopup().getId()).delete();
            }

        });
        item5.setOnAction(e -> {
            if(new File(((MenuItem)e.getSource()).getParentPopup().getId()).exists()){

                if(MainWindow.mainScreen.hasDocument(false)){
                    if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(((MenuItem)e.getSource()).getParentPopup().getId())){
                        MainWindow.mainScreen.document.save();
                    }
                }

                new ExportWindow(Collections.singletonList(new File(((MenuItem)e.getSource()).getParentPopup().getId())));
            }

        });
        item6.setOnAction(e -> MainWindow.lbFilesTab.clearFiles());
        return menu;

    }

}