package fr.themsou.document.editions;

import fr.themsou.main.Main;
import fr.themsou.utils.dialog.AlreadyExistDialog;
import fr.themsou.utils.dialog.DialogBuilder;
import fr.themsou.interfaces.windows.language.TR;
import fr.themsou.interfaces.windows.MainWindow;
import fr.themsou.utils.FilesUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class EditionExporter {

    private enum ImportExportResult{
        DONE,
        ERROR_CONTINUE,
        ERROR_BREAK,
        NO_EDIT
    }

    public static ImportExportResult importEdition(File pdfFile, File toImportFile, boolean onlyGrades, boolean toCurrentDocument){

        if(toImportFile.exists()){
            if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(pdfFile.getAbsolutePath()) || toCurrentDocument){
                MainWindow.mainScreen.document.edition.clearEdit(false);
            }
            try{
                Files.copy(toImportFile.toPath(), Edition.getEditFile(pdfFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }catch(IOException e){
                e.printStackTrace();
                boolean result = DialogBuilder.showErrorAlert(TR.tr("Impossible de copier le fichier") + " \"" + toImportFile.toPath() + "\" " + TR.tr("vers") + " \"" + Edition.getEditFile(pdfFile).toPath() + "\"", e.getMessage(), !toCurrentDocument);
                if(result){
                    return ImportExportResult.ERROR_BREAK;
                }else{
                    return ImportExportResult.ERROR_CONTINUE;
                }
            }
            if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(pdfFile.getAbsolutePath()) || toCurrentDocument){
                MainWindow.mainScreen.document.loadEdition();
            }
            return ImportExportResult.DONE;
        }else{
            boolean result = DialogBuilder.showWrongAlert(TR.tr("Aucun fichier d'édition trouvé pour") + " " + pdfFile.getName(),
                    TR.tr("Le fichier PDF et le fichier d'édition doivent avoir le même nom (excepté le .yml) pour pouvoir faire l'association entre les deux fichiers."), true);
            if(result) return ImportExportResult.ERROR_BREAK;
            else return ImportExportResult.ERROR_CONTINUE;
        }
    }

    public static ImportExportResult exportEdition(File pdfFile, File toDir, boolean onlyGrades, AlreadyExistDialog alreadyExistDialog){
        File editFile = Edition.getEditFile(pdfFile);
        if(!editFile.exists()) return ImportExportResult.NO_EDIT;

        File toFile = new File(toDir.getAbsolutePath() + File.separator + pdfFile.getName() + ".yml");
        if(toFile.exists()){
            AlreadyExistDialog.ResultType result = alreadyExistDialog.showAndWait(toFile);
            if(result == AlreadyExistDialog.ResultType.SKIP){
                return ImportExportResult.ERROR_CONTINUE;
            }else if(result == AlreadyExistDialog.ResultType.STOP){
                return ImportExportResult.ERROR_BREAK;
            }else if(result == AlreadyExistDialog.ResultType.RENAME){
                toFile = AlreadyExistDialog.rename(toFile);
            }
        }
        try{
            Files.copy(editFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){
            e.printStackTrace();
            boolean result = DialogBuilder.showErrorAlert(TR.tr("Impossible de copier le fichier") + " \"" + editFile.toPath() + "\" " + TR.tr("vers") + " \"" + toFile.toPath() + "\"", e.getMessage(), alreadyExistDialog.isRecursive());
            if(result){
                return ImportExportResult.ERROR_BREAK;
            }else{
                return ImportExportResult.ERROR_CONTINUE;
            }
        }

        return ImportExportResult.DONE;
    }

    public static void showImportDialog(boolean onlyGrades){
        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Charger une autre édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer l'édition courante par une autre ?"));
        else dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer le barème courant par celui d'une autre édition ?"));

        ButtonType yes = new ButtonType(TR.tr("Oui, choisir un fichier"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Oui, choisir un dossier et répéter\ncette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes, yesAll);

        Optional<ButtonType> option = dialog.showAndWait();
        if(option.get() == yes){
            if(MainWindow.mainScreen.hasDocument(true)){
                File file = DialogBuilder.showFileDialog(true, TR.tr("Fichier d'édition YAML"), "*.yml");
                if(file != null){
                    ImportExportResult result = importEdition(MainWindow.mainScreen.document.getFile(), file, onlyGrades, true);
                    if(result == ImportExportResult.DONE){
                        Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Importation terminée"));
                        endAlert.setHeaderText(TR.tr("L'édition a bien été chargée"));
                        endAlert.show();
                    }
                }
            }
        }else if(option.get() == yesAll){
            if(MainWindow.mainScreen.hasDocument(true)){
                File directory = DialogBuilder.showDirectoryDialog(true);
                if(directory != null){
                    int counter = 0;
                    int badFolderCounter = 0;
                    for(File pdfFile : MainWindow.filesTab.getOpenedFiles()){
                        if(FilesUtils.isInSameDir(pdfFile, MainWindow.mainScreen.document.getFile())){
                            ImportExportResult result = importEdition(pdfFile, new File(directory.getAbsolutePath() + File.separator + pdfFile.getName() + ".yml"), onlyGrades, false);
                            if(result == ImportExportResult.DONE) counter++;
                            else if(result == ImportExportResult.ERROR_BREAK) break;
                        }else badFolderCounter++;
                    }
                    Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Importation terminée"));
                    endAlert.setHeaderText(TR.tr("Les éditions ont bien été importées"));
                    String badFolderText = badFolderCounter == 0 ? "" : "\n(" + (badFolderCounter) + " " + TR.tr("documents ignorés car ils n'étaient pas dans le même dossier") + ")";
                    endAlert.setContentText(counter + "/" + (MainWindow.filesTab.getOpenedFiles().size()-badFolderCounter) + " " + TR.tr("éditions importés") + badFolderText);

                    endAlert.show();
                }
            }
        }
        MainWindow.filesTab.refresh();
    }
    public static void showExportDialog(boolean onlyGrades){
        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Exporter l'édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Vous allez exporter l'édition complète du document (annotations, notes, images...) sous forme de fichier."));
        else dialog.setHeaderText(TR.tr("Vous allez exporter le barème du document sous forme de fichier."));

        ButtonType yes = new ButtonType(TR.tr("Choisir un dossier"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Choisir un dossier et répéter\ncette action pour tous les fichiers\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes, yesAll);

        Optional<ButtonType> option = dialog.showAndWait();
        if(option.get() == yes){
            if(MainWindow.mainScreen.hasDocument(true)){
                File directory = DialogBuilder.showDirectoryDialog(true);
                if(directory != null){
                    ImportExportResult result = exportEdition(MainWindow.mainScreen.document.getFile(), directory, onlyGrades, new AlreadyExistDialog(false));
                    if(result == ImportExportResult.DONE){
                        Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation terminée"));
                        endAlert.setHeaderText(TR.tr("L'édition a bien été exporté"));
                        endAlert.setContentText(TR.tr("Dans le dossier :") + " " + FilesUtils.getPathReplacingUserHome(directory.getAbsolutePath()));

                        ButtonType open = new ButtonType(TR.tr("Ouvrir le dossier"), ButtonBar.ButtonData.YES);
                        endAlert.getButtonTypes().add(open);

                        Optional<ButtonType> optionSelected = endAlert.showAndWait();
                        if(optionSelected.get() == open){
                            Main.hostServices.showDocument(directory.getAbsolutePath());
                        }
                    }
                }
            }
        }else if(option.get() == yesAll){
            if(MainWindow.mainScreen.hasDocument(true)){
                File directory = DialogBuilder.showDirectoryDialog(true);
                if(directory != null){
                    int counter = 0;
                    int badFolderCounter = 0;
                    int noEditCounter = 0;
                    AlreadyExistDialog alreadyExistDialog = new AlreadyExistDialog(true);
                    for(File pdfFile : MainWindow.filesTab.getOpenedFiles()){
                        if(FilesUtils.isInSameDir(pdfFile, MainWindow.mainScreen.document.getFile())){
                            ImportExportResult result = exportEdition(pdfFile, directory, onlyGrades, alreadyExistDialog);
                            if(result == ImportExportResult.DONE) counter++;
                            else if(result == ImportExportResult.ERROR_BREAK) break;
                            else if(result == ImportExportResult.NO_EDIT) noEditCounter++;
                        }else badFolderCounter++;
                    }
                    Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation terminée"));
                    ButtonType open = new ButtonType(TR.tr("Ouvrir le dossier"), ButtonBar.ButtonData.YES);
                    endAlert.getButtonTypes().add(open);
                    endAlert.setHeaderText(TR.tr("Les éditions ont bien été exportées"));
                    String badFolderText = badFolderCounter == 0 ? "" : "\n(" + (badFolderCounter) + " " + TR.tr("documents ignorés car ils n'étaient pas dans le même dossier") + ")";
                    String noEditText = noEditCounter == 0 ? "" : "\n(" + (noEditCounter) + " " + TR.tr("documents ignorés car ils n'avaient pas d'édition") + ")";
                    endAlert.setContentText(counter + "/" + (MainWindow.filesTab.getOpenedFiles().size()) + " " + TR.tr("éditions exportées") + badFolderText + noEditText);

                    Optional<ButtonType> optionSelected = endAlert.showAndWait();
                    if(optionSelected.get() == open){
                        Main.hostServices.showDocument(directory.getAbsolutePath());
                    }
                }
            }
        }
    }

}
