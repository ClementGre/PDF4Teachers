package fr.clementgre.pdf4teachers.panel.leftBar.grades;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class GradeCopyGradeScaleDialog {

    public ArrayList<GradeRating> ratings = new ArrayList<>();

    boolean ignoreAlreadyExist = false;
    boolean ignoreErase = false;

    public void show(){

        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Copier le barème sur d'autres éditions"));
        dialog.setHeaderText(TR.tr("Cette action va copier le barème entré dans cette édition sur d'autres éditions."));

        ButtonType cancel = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType yes = new ButtonType(TR.tr("Copier sur les documents\nouverts du même dossier."), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Copier sur tous les\ndocuments ouverts"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(yesAll, yes, cancel);

        Optional<ButtonType> option = dialog.showAndWait();
        int copiedEditions = 0;
        if(option.get() == yes){
            prepareCopyEditions();
            boolean recursive = MainWindow.filesTab.getOpenedFiles().size() != 1;
            for(File file : MainWindow.filesTab.getOpenedFiles()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                if(MainWindow.mainScreen.document.getFile().getParent().equals(file.getParent())){
                    int result = copyToFile(file, recursive);
                    if(result == 0) copiedEditions++;
                    else if(result == 2) break;
                }
            }
        }else if(option.get() == yesAll){
            prepareCopyEditions();
            boolean recursive = MainWindow.filesTab.getOpenedFiles().size() != 1;
            for(File file : MainWindow.filesTab.getOpenedFiles()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                int result = copyToFile(file, recursive);
                if(result == 0) copiedEditions++;
                else if(result == 2) break;
            }
        }else return;

        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Barème copiés"));
        alert.setHeaderText(TR.tr("Votre barème a bien été copié."));
        alert.setContentText("(" + copiedEditions + " " + TR.tr("éditions affectées") + ".)");
        alert.show();

        MainWindow.filesTab.refresh();

    }

    public void prepareCopyEditions(){
        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
        File editFile = Edition.getEditFile(MainWindow.mainScreen.document.getFile());

        try {
            Element[] elements = Edition.simpleLoad(editFile);
            for(Element element : elements){
                if(element instanceof GradeElement){
                    ratings.add(((GradeElement) element).toGradeRating());
                }
            }
        }catch(Exception e){ e.printStackTrace(); }
    }

    // 0 : Copied | 1 : Canceled | 2 : Cancel All
    public int copyToFile(File file, boolean recursive){
        try{
            File editFile = Edition.getEditFile(file);

            Element[] elementsArray = Edition.simpleLoad(editFile);
            List<GradeElement> gradeElements = new ArrayList<>();
            List<Element> otherElements = new ArrayList<>();
            for(Element element : elementsArray){
                if(element instanceof GradeElement) gradeElements.add((GradeElement) element);
                else otherElements.add(element);
            }

            if(gradeElements.size() >= 1 && !ignoreAlreadyExist){
                Alert dialog = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("Barème déjà présent"));
                dialog.setHeaderText(TR.tr("L'édition du fichier") + " " + file.getName() + " " + TR.tr("contient déjà un barème"));
                dialog.setContentText(TR.tr("PDF4Teachers va essayer de récupérer les notes de l'ancien barème pour les inclure au nouveau barème.") + "\n" + TR.tr("Vous serez avertis si une note va être écrasée."));

                ButtonType ignore = new ButtonType(TR.tr("Continuer"), ButtonBar.ButtonData.OK_DONE);
                ButtonType ignoreAll = new ButtonType(TR.tr("Toujours continuer"), ButtonBar.ButtonData.OK_DONE);
                ButtonType stop = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType stopAll = new ButtonType(TR.tr("Tout annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);

                if(recursive) dialog.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);
                else dialog.getButtonTypes().setAll(ignore, stopAll);

                    Optional<ButtonType> option = dialog.showAndWait();
                if(option.get() == stop){
                    return 1;
                }else if(option.get() == stopAll){
                    return 2;
                }else if(option.get() == ignoreAll){
                    ignoreAlreadyExist= true;
                }
            }

            for(GradeRating rating : ratings){
                GradeElement element = rating.getSamePathIn((ArrayList<GradeElement>) gradeElements);
                if(element != null){
                    otherElements.add(rating.toGradeElement(element.getValue(), element.getRealX(), element.getRealY(), element.getPageNumber()));
                    gradeElements.remove(element);
                }else{
                    otherElements.add(rating.toGradeElement());
                }
            }

            if(gradeElements.size() >= 1 && !ignoreErase){
                String grades = "";
                for(GradeElement grade : gradeElements){
                    grades += "\n" + grade.getParentPath().replaceAll(Pattern.quote("\\"), "/") + "/" + grade.getName() + "  (" + MainWindow.format.format(grade.getValue()).replaceAll("-1", "?") + "/" + MainWindow.format.format(grade.getTotal()) + ")";
                }

                Alert dialog = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("Écraser les notes non correspondantes"));
                dialog.setHeaderText(TR.tr("Aucune note du nouveau barème ne correspond à :") + grades + "\n" + TR.tr("Dans le document") + " : " + file.getName());

                ButtonType ignore = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.OK_DONE);
                ButtonType ignoreAll = new ButtonType(TR.tr("Toujours écraser"), ButtonBar.ButtonData.OK_DONE);
                ButtonType stop = new ButtonType(TR.tr("Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType stopAll = new ButtonType(TR.tr("Tout arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);

                if(recursive) dialog.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);
                else dialog.getButtonTypes().setAll(ignore, stop);

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
                if(o1 instanceof GradeElement && o2 instanceof GradeElement){
                    return GradeTreeView.getElementTier(((GradeElement) o1).getParentPath()) - GradeTreeView.getElementTier(((GradeElement) o2).getParentPath());
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
