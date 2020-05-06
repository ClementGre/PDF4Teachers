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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class NoteCopyRatingScaleDialog {

    ArrayList<NoteRating> ratings = new ArrayList<>();

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
                    if(copyToFile(file)) copiedEditions++;
                    else break;
                }
            }
        }else if(option.get() == yesAll){
            prepareCopyEditions();
            for(File file : MainWindow.lbFilesTab.files.getItems()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                if(copyToFile(file)) copiedEditions++;
                else break;
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

    public void prepareCopyEditions(){
        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
        File editFile = Edition.getEditFile(MainWindow.mainScreen.document.getFile());

        try {
            Element[] elements = Edition.simpleLoad(editFile);
            for(Element element : elements){
                if(element instanceof NoteElement){
                    ratings.add(((NoteElement) element).toNoteRating());
                }
            }
        }catch(Exception e){ e.printStackTrace(); }
    }

    public boolean copyToFile(File file){
        try{
            File editFile = Edition.getEditFile(file);

            Element[] elementsArray = Edition.simpleLoad(editFile);
            ArrayList<NoteElement> noteElements = new ArrayList<>();
            List<Element> otherElements = new ArrayList<>();
            for(Element element : elementsArray){
                if(element instanceof NoteElement) noteElements.add((NoteElement) element);
                else otherElements.add(element);
            }

            for(NoteRating rating : ratings){
                NoteElement element = rating.getSamePathIn(noteElements);
                if(element != null){
                    otherElements.add(rating.toNoteElement(element.getValue(), element.getRealX(), element.getRealY(), element.getPageNumber()));
                    noteElements.remove(element);
                }else{
                    otherElements.add(rating.toNoteElement());
                }
            }

            if(noteElements.size() >= 1){
                String notes = "";
                for(NoteElement note : noteElements){
                    notes += "\n" + note.getParentPath().replaceAll(Pattern.quote("\\"), "/") + "/" + note.getName();
                }

                Alert dialog = new Alert(Alert.AlertType.WARNING);
                new JMetro(dialog.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(dialog);
                dialog.setTitle(TR.tr("Écraser les notes non correspondantes"));
                dialog.setHeaderText(TR.tr("Aucune note du nouveau barème ne correspond à :") + notes + "\n" + TR.tr("Dans le document") + " : " + file.getName());

                ButtonType ignore = new ButtonType(TR.tr("Ignorer"), ButtonBar.ButtonData.OK_DONE);
                ButtonType stop = new ButtonType(TR.tr("Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType stopAll = new ButtonType(TR.tr("Tout arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getButtonTypes().setAll(ignore, stop, stopAll);

                Optional<ButtonType> option = dialog.showAndWait();
                if(option.get() == stop){
                    return true;
                }else if(option.get() == stopAll){
                    return false;
                }
            }

            otherElements.sort((o1, o2) -> {
                if(o1 instanceof NoteElement && o2 instanceof NoteElement){
                    return NoteTreeView.getElementTier(((NoteElement) o1).getParentPath()) - NoteTreeView.getElementTier(((NoteElement) o2).getParentPath());
                }
                return 0;
            });

            Edition.simpleSave(editFile, otherElements.toArray(new Element[0]));
            return true;

        }catch(Exception e){
            e.printStackTrace();
            return true;
        }
    }

}
