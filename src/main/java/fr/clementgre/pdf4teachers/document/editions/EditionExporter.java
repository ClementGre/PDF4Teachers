package fr.clementgre.pdf4teachers.document.editions;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.dialog.AlreadyExistDialog;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class EditionExporter {

    public static void showImportDialog(boolean onlyGrades){
        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Charger une autre édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer l'édition courante par une autre ?"));
        else dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer le barème courant par celui d'une autre édition ?"));

        ButtonType yes = new ButtonType(TR.tr("Oui, choisir un fichier"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Oui, choisir un dossier et répéter\ncette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes, yesAll);

        Optional<ButtonType> option = dialog.showAndWait();
        File directory = null;
        File singleFile = null;
        if(option.get() == yes){
            if(MainWindow.mainScreen.hasDocument(true)){
                File file = DialogBuilder.showFileDialog(true, TR.tr("Fichier d'édition YAML"), "*.yml");
                if(file != null){
                    directory = file.getParentFile();
                    singleFile = file;
                }
            }
        }else if(option.get() == yesAll){
            if(MainWindow.mainScreen.hasDocument(true)){
                directory = DialogBuilder.showDirectoryDialog(true);
            }
        }
        if(directory == null) return;

        File finalSingleFile = singleFile;
        File finalDirectory = directory;
        new TwoStepListAction<>(false, new TwoStepListInterface<Map.Entry<File, File>, Map.Entry<File, File>>() {
            @Override
            public List<Map.Entry<File, File>> prepare() {
                if(finalSingleFile != null){
                    return Collections.singletonList(Map.entry(MainWindow.mainScreen.document.getFile(), finalSingleFile));
                }else{
                    return MainWindow.filesTab.getOpenedFiles().stream().map(pdfFile -> {
                        return Map.entry(pdfFile, new File(finalDirectory.getAbsolutePath() + File.separator + pdfFile.getName() + ".yml"));
                    }).collect(Collectors.toList());
                }
            }

            @Override
            public Map.Entry<Map.Entry<File, File>, Integer> sortData(Map.Entry<File, File> data) {
                if(!FilesUtils.isInSameDir(data.getKey(), MainWindow.mainScreen.document.getFile())) return Map.entry(Map.entry(new File(""), new File("")), 1); // Check same dir

                if(data.getValue().exists()){
                    if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(data.getKey().getAbsolutePath())){
                        MainWindow.mainScreen.document.edition.clearEdit(false);
                    }

                    return Map.entry(Map.entry(data.getValue(), data.getKey()), TwoStepListAction.CODE_OK);
                }else{
                    boolean result = DialogBuilder.showWrongAlert(TR.tr("Aucun fichier d'édition trouvé pour") + " " + data.getKey().getName(),
                            TR.tr("Le fichier PDF et le fichier d'édition doivent avoir le même nom (excepté le .yml) pour pouvoir faire l'association entre les deux fichiers."), true);
                    if(result || finalSingleFile != null) return Map.entry(Map.entry(new File(""), new File("")),TwoStepListAction.CODE_STOP); // No match > Stop all
                    else return Map.entry(Map.entry(new File(""), new File("")), 2); // No match
                }
            }

            @Override
            public String getSortedDataName(Map.Entry<File, File> data) {
                return "undefined";
            }

            @Override
            public TwoStepListAction.ProcessResult completeData(Map.Entry<File, File> data){
                try{
                    Files.copy(data.getKey().toPath(), Edition.getEditFile(data.getValue()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }catch(IOException e){
                    e.printStackTrace();
                    boolean result = DialogBuilder.showErrorAlert(TR.tr("Impossible de copier le fichier") + " \"" + data.getKey().getName() + "\" " + TR.tr("vers") + " \"" + data.getValue().toPath() + "\"", e.getMessage(), finalSingleFile == null);
                    if(finalSingleFile != null) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                    if(result) return TwoStepListAction.ProcessResult.STOP;
                    else return TwoStepListAction.ProcessResult.SKIPPED;
                }
                if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(data.getValue().getAbsolutePath())){
                    MainWindow.mainScreen.document.loadEdition();
                }
                return TwoStepListAction.ProcessResult.OK;
            }

            @Override
            public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons) {
                Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Importation terminée"));
                endAlert.setHeaderText(TR.tr("Les éditions ont bien été importées"));
                String badFolderText = !excludedReasons.containsKey(1) ? "" : "\n(" + excludedReasons.get(1) + " " + TR.tr("documents ignorés car ils n'étaient pas dans le même dossier") + ")";
                String noMatchesText = !excludedReasons.containsKey(2) ? "" : "\n(" + excludedReasons.get(2) + " " + TR.tr("documents ignorés car ils n'avaient pas d'édition correspondante") + ")";
                endAlert.setContentText(completedSize + "/" + originSize + " " + TR.tr("éditions importés") + badFolderText + noMatchesText);

                endAlert.show();
            }
        });

        MainWindow.filesTab.refresh();
    }
    public static void showExportDialog(final boolean onlyGrades){
        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Exporter l'édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Vous allez exporter l'édition complète du document (annotations, notes, images...) sous forme de fichier."));
        else dialog.setHeaderText(TR.tr("Vous allez exporter le barème du document sous forme de fichier."));

        ButtonType yes = new ButtonType(TR.tr("Choisir un dossier"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Choisir un dossier et répéter\ncette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes, yesAll);

        Optional<ButtonType> option = dialog.showAndWait();
        File directory = null;
        boolean recursive = true;

        if(option.get() == yes){
            if(MainWindow.mainScreen.hasDocument(true)){
                if(MainWindow.mainScreen.document.save()){
                    directory = DialogBuilder.showDirectoryDialog(true);
                    recursive = false;
                }
            }
        }else if(option.get() == yesAll){
            if(MainWindow.mainScreen.hasDocument(true)){
                if(MainWindow.mainScreen.document.save()){
                    directory = DialogBuilder.showDirectoryDialog(true);
                }
            }
        }
        if(directory == null) return;

        final boolean finalRecursive = recursive;
        final File finalDirectory = directory;
        AlreadyExistDialog alreadyExistDialog = new AlreadyExistDialog(recursive);
        new TwoStepListAction<>(false, new TwoStepListInterface<File, Map.Entry<File, File>>() {
            @Override
            public List<File> prepare() {
                if(finalRecursive){
                    return MainWindow.filesTab.getOpenedFiles();
                }else{
                    return Collections.singletonList(MainWindow.mainScreen.document.getFile());
                }
            }

            @Override
            public Map.Entry<Map.Entry<File, File>, Integer> sortData(File pdfFile) {
                if(!FilesUtils.isInSameDir(pdfFile, MainWindow.mainScreen.document.getFile())) return Map.entry(Map.entry(new File(""), new File("")), 1); // Check same dir

                File editFile = Edition.getEditFile(pdfFile);
                if(!editFile.exists()) return Map.entry(Map.entry(new File(""), new File("")), 2); // Check HasEdit

                File toFile = new File(finalDirectory.getAbsolutePath() + File.separator + pdfFile.getName() + ".yml");
                if(toFile.exists()){ // Check Already Exist
                    AlreadyExistDialog.ResultType result = alreadyExistDialog.showAndWait(toFile);
                    if(result == AlreadyExistDialog.ResultType.SKIP) return Map.entry(Map.entry(new File(""), new File("")), 3);
                    else if(result == AlreadyExistDialog.ResultType.STOP) return Map.entry(Map.entry(new File(""), new File("")), TwoStepListAction.CODE_STOP);
                    else if(result == AlreadyExistDialog.ResultType.RENAME) toFile = AlreadyExistDialog.rename(toFile);
                }
                return Map.entry(Map.entry(editFile, toFile), TwoStepListAction.CODE_OK);
            }

            @Override
            public String getSortedDataName(Map.Entry<File, File> data) {
                return "undefined";
            }

            @Override
            public TwoStepListAction.ProcessResult completeData(Map.Entry<File, File> data){
                try{
                    Files.copy(data.getKey().toPath(), data.getValue().toPath(), StandardCopyOption.REPLACE_EXISTING);
                }catch(IOException e){
                    e.printStackTrace();
                    boolean result = DialogBuilder.showErrorAlert(TR.tr("Impossible de copier le fichier") + " \"" + data.getKey().getName() + "\" " + TR.tr("vers") + " \"" + data.getValue().toPath() + "\"", e.getMessage(), alreadyExistDialog.isRecursive());
                    if(!finalRecursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                    if(result) return TwoStepListAction.ProcessResult.STOP;
                    else return TwoStepListAction.ProcessResult.SKIPPED;
                }
                return TwoStepListAction.ProcessResult.OK;
            }

            @Override
            public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons) {
                Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation terminée"));
                ButtonType open = new ButtonType(TR.tr("Ouvrir le dossier"), ButtonBar.ButtonData.YES);
                endAlert.getButtonTypes().add(open);
                endAlert.setHeaderText(TR.tr("Les éditions ont bien été exportées"));

                String badFolderText = !excludedReasons.containsKey(1) ? "" : "\n(" + excludedReasons.get(1) + " " + TR.tr("documents ignorés car ils n'étaient pas dans le même dossier") + ")";
                String noEditText = !excludedReasons.containsKey(2) ? "" : "\n(" + excludedReasons.get(2) + " " + TR.tr("documents ignorés car ils n'avaient pas d'édition") + ")";
                String alreadyExistText = !excludedReasons.containsKey(3) ? "" : "\n(" + excludedReasons.get(3) + " " + TR.tr("documents ignorés car ils existaient déjà") + ")";
                endAlert.setContentText(completedSize + "/" + originSize + " " + TR.tr("éditions exportées") + badFolderText + noEditText + alreadyExistText);

                Optional<ButtonType> optionSelected = endAlert.showAndWait();
                if(optionSelected.get() == open){
                    Main.hostServices.showDocument(finalDirectory.getAbsolutePath());
                }
            }
        });
    }

}