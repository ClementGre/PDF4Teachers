package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class NoteCopyRatingScaleDialog {

    public NoteCopyRatingScaleDialog(){

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        new JMetro(dialog.getDialogPane(), Style.LIGHT);
        Builders.secureAlert(dialog);
        dialog.setTitle(TR.tr("Copier le barème sur d'autres éditions"));
        dialog.setHeaderText(TR.tr("Cette action va copier le barème entré dans cette édition sur d'autres éditions."));

        ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType yes = new ButtonType(TR.tr("Copier sur les documents\nouverts du même dossier."), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Copier sur tous les\ndocuments ouverts"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(yesAll, yes, cancel);

        Optional<ButtonType> option = dialog.showAndWait();
        int copiedEditions = 0;
        if(option.get() == yes){
            prepareCopyEditions();
            for(File file : MainWindow.lbFilesTab.files.getItems()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                if(MainWindow.mainScreen.document.getFile().getParent().equals(file.getParent())){
                    copyToFile(file);
                    copiedEditions++;
                }
            }
        }else if(option.get() == yesAll){
            prepareCopyEditions();
            for(File file : MainWindow.lbFilesTab.files.getItems()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                copyToFile(file); copiedEditions++;
            }
        }else return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        new JMetro(alert.getDialogPane(), Style.LIGHT);
        Builders.secureAlert(alert);
        alert.setTitle(TR.tr("Barème copiés"));
        alert.setHeaderText(TR.tr("Votre barème a bien été copié."));
        alert.setContentText("(" + copiedEditions + " " + TR.tr("éditions affectés") + ".)");
        alert.show();

        MainWindow.lbFilesTab.files.refresh();

    }

    ArrayList<NoteElement> notes = new ArrayList<>();
    Element[] array = new Element[]{};
    public void prepareCopyEditions(){

        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
        File editFile = Edition.getEditFile(MainWindow.mainScreen.document.getFile());

        try {
            Element[] elements = Edition.simpleLoad(editFile);
            for(Element element : elements){
                if(element instanceof NoteElement){
                    NoteElement note = ((NoteElement) element);
                    note.setValue(-1);
                    notes.add(note);
                }
            }
            array = new Element[notes.size()];
            array = notes.toArray(array);

        }catch(Exception e){ e.printStackTrace(); }
    }

    public void copyToFile(File file){
        try{

            File editFile = Edition.getEditFile(file);
            Edition.simpleAppend(editFile, array);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
