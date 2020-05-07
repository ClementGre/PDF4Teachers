package fr.themsou.panel.leftBar.notes;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.main.Main;
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
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NoteCopyRatingScaleDialog {

    ArrayList<NoteRating> ratings = new ArrayList<>();

    boolean ignoreAlreadyExist = false;
    boolean ignoreErase = false;

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
                    int result = copyToFile(file);
                    if(result == 0) copiedEditions++;
                    else if(result == 2) break;
                }
            }
        }else if(option.get() == yesAll){
            prepareCopyEditions();
            for(File file : MainWindow.lbFilesTab.files.getItems()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                int result = copyToFile(file);
                if(result == 0) copiedEditions++;
                else if(result == 2) break;
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

    // 0 : Copied | 1 : Canceled | 2 : Cancel All
    public int copyToFile(File file){
        try{
            File editFile = Edition.getEditFile(file);

            Element[] elementsArray = Edition.simpleLoad(editFile);
            List<NoteElement> noteElements = new ArrayList<>();
            List<Element> otherElements = new ArrayList<>();
            for(Element element : elementsArray){
                if(element instanceof NoteElement) noteElements.add((NoteElement) element);
                else otherElements.add(element);
            }

            if(noteElements.size() >= 1 && !ignoreAlreadyExist){
                Alert dialog = new Alert(Alert.AlertType.WARNING);
                new JMetro(dialog.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(dialog);
                dialog.setTitle(TR.tr("Barème déjà présent"));
                dialog.setHeaderText(TR.tr("L'édition du fichier") + " " + file.getName() + " " + TR.tr("contient déjà un barème"));
                dialog.setContentText(TR.tr("PDF4Teachers va essayer de récupérer les notes de l'ancien barème pour les ajouter au nouveau barème.") + "\n" + TR.tr("Vous serez avertis si une note va être écrasée."));

                ButtonType ignore = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.OK_DONE);
                ButtonType ignoreAll = new ButtonType(TR.tr("Toujours continuer"), ButtonBar.ButtonData.OK_DONE);
                ButtonType stop = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType stopAll = new ButtonType(TR.tr("Tout annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);

                Optional<ButtonType> option = dialog.showAndWait();
                if(option.get() == stop){
                    return 1;
                }else if(option.get() == stopAll){
                    return 2;
                }else if(option.get() == ignoreAll){
                    ignoreAlreadyExist= true;
                }
            }

            for(NoteRating rating : ratings){
                NoteElement element = rating.getSamePathIn((ArrayList<NoteElement>) noteElements);
                if(element != null){
                    otherElements.add(rating.toNoteElement(element.getValue(), element.getRealX(), element.getRealY(), element.getPageNumber()));
                    noteElements.remove(element);
                }else{
                    otherElements.add(rating.toNoteElement());
                }
            }

            if(noteElements.size() >= 1 && !ignoreErase){
                String notes = "";
                for(NoteElement note : noteElements){
                    notes += "\n" + note.getParentPath().replaceAll(Pattern.quote("\\"), "/") + "/" + note.getName() + "  (" + Main.format.format(note.getValue()).replaceAll("-1", "?") + "/" + Main.format.format(note.getTotal()) + ")";
                }

                Alert dialog = new Alert(Alert.AlertType.WARNING);
                new JMetro(dialog.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(dialog);
                dialog.setTitle(TR.tr("Écraser les notes non correspondantes"));
                dialog.setHeaderText(TR.tr("Aucune note du nouveau barème ne correspond à :") + notes + "\n" + TR.tr("Dans le document") + " : " + file.getName());

                ButtonType ignore = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.OK_DONE);
                ButtonType ignoreAll = new ButtonType(TR.tr("Toujours écraser"), ButtonBar.ButtonData.OK_DONE);
                ButtonType stop = new ButtonType(TR.tr("Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType stopAll = new ButtonType(TR.tr("Tout arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                dialog.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);

                Optional<ButtonType> option = dialog.showAndWait();
                if(option.get() == stop){
                    return 1;
                }else if(option.get() == stopAll){
                    return 2;
                }else if(option.get() == ignoreAll){
                    ignoreErase = true;
                }
            }

            otherElements.sort((o1, o2) -> {
                if(o1 instanceof NoteElement && o2 instanceof NoteElement){
                    return NoteTreeView.getElementTier(((NoteElement) o1).getParentPath()) - NoteTreeView.getElementTier(((NoteElement) o2).getParentPath());
                }
                return 0;
            });

            Edition.simpleSave(editFile, otherElements.toArray(new Element[0]));
            return 0;

        }catch(Exception e){
            e.printStackTrace();
            return 1;
        }
    }

}
