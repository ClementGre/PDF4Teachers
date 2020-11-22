package fr.themsou.document.editions;

import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.util.Optional;

public class EditionUtils {

    public static int importEdition(File file, boolean onlyGrades){
        return 0;
    }


    public static int exportEdition(File file, boolean onlyGrades){
        return 0;
    }

    public static void showImportDialog(boolean onlyGrades){
        Alert dialog = Builders.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Charger une autre édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer l'édition courante par une autre ?"));
        else dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer le barème courant par celui d'une autre édition ?"));

        ButtonType cancel = new ButtonType(TR.tr("Non"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType yes = new ButtonType(TR.tr("Oui, choisir un fichier"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Oui, choisir un dossier et répéter\ncette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(cancel, yes, yesAll);

        Optional<ButtonType> option = dialog.showAndWait();
        if(option.get() == yes){
            if(MainWindow.mainScreen.hasDocument(true)){

                MainWindow.mainScreen.document.edition.clearEdit(false);
                //importEdition();
                MainWindow.mainScreen.document.loadEdition();
            }
        }else if(option.get() == yesAll){

        }
    }
    public static void showExportDialog(boolean onlyGrades){
        Alert dialog = Builders.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Exporter l'édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Vous allez exporter l'édition complète du document (annotations, notes, images...) sous forme de fichier."));
        else dialog.setHeaderText(TR.tr("Vous allez exporter le barème du document sous forme de fichier."));

        ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType yes = new ButtonType(TR.tr("Choisir un dossier"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Choisir un dossier et répéter\ncette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(cancel, yes, yesAll);

        Optional<ButtonType> option = dialog.showAndWait();
        if(option.get() == yes){
            if(MainWindow.mainScreen.hasDocument(true)){

                MainWindow.mainScreen.document.edition.clearEdit(false);
                //Edition.mergeEditFileWithEditFile(files.getKey(), Edition.getEditFile(MainWindow.mainScreen.document.getFile()));
                MainWindow.mainScreen.document.loadEdition();
            }
        }else if(option.get() == yesAll){

        }
    }

}
