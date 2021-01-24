package fr.clementgre.pdf4teachers.document.editions;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeCopyGradeScaleDialog;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.AlreadyExistDialog;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EditionExporter {

    public static void showImportDialog(boolean onlyGrades){
        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Charger une autre édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer l'édition courante par une autre ?"));
        else dialog.setHeaderText(TR.tr("Êtes vous sûr de vouloir remplacer le barème courant par celui d'une autre édition ?"));

        CheckBox copyLocations = new CheckBox(TR.tr("Copier la position des notes"));
        if(onlyGrades) dialog.getDialogPane().setContent(copyLocations);

        ButtonType yes = new ButtonType(TR.tr("Oui, choisir un fichier"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Oui, choisir un fichier\nqui contient une édition par\ndocument de la liste"), ButtonBar.ButtonData.OTHER);

        if(!onlyGrades)  dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes, yesAll);
        else dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes);

        Optional<ButtonType> option = dialog.showAndWait();
        File file = null;
        boolean recursive = false;
        if(MainWindow.mainScreen.hasDocument(true)){
            if(MainWindow.mainScreen.document.save()){
                if(option.get() == yes){
                    file = DialogBuilder.showFileDialog(true, TR.tr("Fichier d'édition YAML"), "*.yml");
                }else if(option.get() == yesAll){
                    file = DialogBuilder.showFileDialog(true, TR.tr("Fichier d'édition YAML"), "*.yml");
                    recursive = true;
                }
            }
        }

        if(file == null) return;

        GradeCopyGradeScaleDialog gradeCopyGradeScale;
        if(onlyGrades){
            gradeCopyGradeScale = new GradeCopyGradeScaleDialog();
            try{
                Element[] elements = Edition.simpleLoad(file);
                for(Element element : elements){
                    if(element instanceof GradeElement){
                        gradeCopyGradeScale.ratings.add(((GradeElement) element).toGradeRating());
                    }
                }
                int result = gradeCopyGradeScale.copyToFile(MainWindow.mainScreen.document.getFile(), false, copyLocations.isSelected());
                if(result == 0){
                    Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Importation terminée"));
                    endAlert.setHeaderText(TR.tr("Le barème a bien été importé"));
                    endAlert.show();
                }
                MainWindow.mainScreen.document.updateEdition();
                MainWindow.filesTab.refresh();
            }catch(Exception e){
                e.printStackTrace();
                DialogBuilder.showErrorAlert(TR.tr("Une erreur est survenue"), e.getMessage(), false);
            }
        }else{
            try{
                Config loadedConfig = new Config(file);
                loadedConfig.load();

                new TwoStepListAction<>(false, recursive, new TwoStepListInterface<String, Config>() {
                    @Override
                    public List<String> prepare(boolean recursive) {
                        if(!recursive){
                            return Collections.singletonList(MainWindow.mainScreen.document.getFile().getName());
                        }else{
                            return loadedConfig.base.keySet().stream().collect(Collectors.toList());
                        }
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public Map.Entry<Config, Integer> sortData(String fileName, boolean recursive) throws Exception{

                        if(!recursive){
                            File editFile = Edition.getEditFile(MainWindow.mainScreen.document.getFile());

                            if(loadedConfig.base.containsKey(fileName)){
                                loadedConfig.base = (HashMap<String, Object>) loadedConfig.base.get(fileName);
                            }

                            if(onlyGrades){
                                Config config = new Config(editFile);
                                config.load();
                                config.set("grades", loadedConfig.getList("grades"));
                                loadedConfig.base = config.base;
                            }

                            loadedConfig.setFile(editFile);
                            loadedConfig.setName(fileName);

                            return Map.entry(loadedConfig, TwoStepListAction.CODE_OK);
                        }else{
                            File pdfFile = new File(MainWindow.mainScreen.document.getFile().getParent() + File.separator + fileName);

                            if(pdfFile.exists()){
                                if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(pdfFile.getAbsolutePath())){ // clear edit if doc is opened
                                    MainWindow.mainScreen.document.edition.clearEdit(false);
                                }

                                File editFile = Edition.getEditFile(pdfFile);
                                Config config = new Config(editFile);
                                config.setName(fileName);

                                HashMap<String, Object> data = (HashMap<String, Object>) loadedConfig.base.get(pdfFile.getName());
                                if(onlyGrades){
                                    config.load();
                                    config.set("grades", Config.getList(data, "grades"));
                                }else{
                                    config.base = data;
                                }

                                return Map.entry(config, TwoStepListAction.CODE_OK);
                            }else{
                                boolean result = DialogBuilder.showWrongAlert(TR.tr("Aucun document ne correspond à l'édition :") + " " + fileName,
                                        TR.tr("Les fichiers PDF doivent avoir les mêmes noms que lors de l'exportation de l'édition."), true);
                                if(result) return Map.entry(new Config(), TwoStepListAction.CODE_STOP); // No match > Stop all
                                else return Map.entry(new Config(), 2); // No match
                            }
                        }
                    }

                    @Override
                    public String getSortedDataName(Config config, boolean recursive) {
                        return config.getName();
                    }

                    @Override
                    public TwoStepListAction.ProcessResult completeData(Config config, boolean recursive){
                        try{
                            config.save();
                        }catch(IOException e){
                            e.printStackTrace();
                            boolean result = DialogBuilder.showErrorAlert(TR.tr("Impossible d'enregistrer le fichier") + " \"" + config.getFile().toPath() + "\" (" + TR.tr("Correspond au document :") + " \"" + config.getName() + "\")", e.getMessage(), recursive);
                            if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                            if(result) return TwoStepListAction.ProcessResult.STOP;
                            else return TwoStepListAction.ProcessResult.SKIPPED;
                        }
                        if(Edition.getEditFile(MainWindow.mainScreen.document.getFile()).getAbsolutePath().equals(config.getFile().getAbsolutePath())){
                            MainWindow.mainScreen.document.loadEdition();
                        }
                        return TwoStepListAction.ProcessResult.OK;
                    }

                    @Override
                    public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive) {
                        Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Importation terminée"));
                        endAlert.setHeaderText(TR.tr("Les éditions ont bien été importées"));
                        String noMatchesText = !excludedReasons.containsKey(2) ? "" : "\n(" + excludedReasons.get(2) + " " + TR.tr("éditions ignorés car elles n'avaient pas de document correspondant") + ")";
                        endAlert.setContentText(completedSize + "/" + originSize + " " + TR.tr("éditions importés") + noMatchesText);

                        endAlert.show();
                    }
                });

                MainWindow.filesTab.refresh();

            }catch(Exception e){
                e.printStackTrace();
                DialogBuilder.showErrorAlert(TR.tr("Une erreur est survenue"), e.getMessage(), false);
            }
        }


    }
    public static void showExportDialog(final boolean onlyGrades){
        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Exporter l'édition"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("Vous allez exporter l'édition complète du document (annotations, notes, images...) sous forme de fichier."));
        else dialog.setHeaderText(TR.tr("Vous allez exporter le barème du document sous forme de fichier."));

        ButtonType yes = new ButtonType(TR.tr("Exporter pour ce document"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("Exporter pour tous les documents\nde la liste et du même dossier"), ButtonBar.ButtonData.OTHER);
        ButtonType yesAllOneFile = new ButtonType(TR.tr("Exporter pour tous les documents\nde la liste et du même dossier\nen un seul fichier"), ButtonBar.ButtonData.OTHER);
        if(!onlyGrades){
            dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes, yesAll, yesAllOneFile);
            dialog.getDialogPane().lookupButton(yesAll).setDisable(MainWindow.filesTab.getOpenedFiles().size() <= 1);
            dialog.getDialogPane().lookupButton(yesAllOneFile).setDisable(MainWindow.filesTab.getOpenedFiles().size() <= 1);
        }
        else dialog.getButtonTypes().setAll(ButtonType.CANCEL, yes);

        Optional<ButtonType> option = dialog.showAndWait();
        File directory = null;
        boolean recursive = option.get() != yes;
        boolean oneFile = option.get() == yesAllOneFile;
        final Config oneFileConfig = new Config();

        if(MainWindow.mainScreen.hasDocument(true)){
            if(MainWindow.mainScreen.document.save()){
                if(option.get() == yes){
                    directory = DialogBuilder.showDirectoryDialog(true);
                }else if(option.get() == yesAll){
                    directory = DialogBuilder.showDirectoryDialog(true);
                }else if(option.get() == yesAllOneFile){
                    File file = DialogBuilder.showSaveDialog(true, "edits.yml", "YAML", ".yml");
                    if(file != null){
                        oneFileConfig.setFile(file);
                        directory = new File(file.getParent());
                    }
                }
            }
        }
        if(directory == null) return; // check isPresent btw

        final File finalDirectory = directory;
        AlreadyExistDialog alreadyExistDialog = new AlreadyExistDialog(recursive);
        new TwoStepListAction<>(false, recursive, new TwoStepListInterface<File, Config>() {
            @Override
            public List<File> prepare(boolean recursive) {
                if(recursive){
                    return MainWindow.filesTab.getOpenedFiles();
                }else{
                    return Collections.singletonList(MainWindow.mainScreen.document.getFile());
                }
            }

            @Override
            public Map.Entry<Config, Integer> sortData(File pdfFile, boolean recursive) throws Exception {
                if(!FilesUtils.isInSameDir(pdfFile, MainWindow.mainScreen.document.getFile())) return Map.entry(new Config(), 1); // Check same dir

                File editFile = Edition.getEditFile(pdfFile);
                if(!editFile.exists()) return Map.entry(new Config(), 2); // Check HasEdit

                Config config = new Config(editFile);
                config.load(); config.setName(pdfFile.getName());
                if(onlyGrades){
                    HashMap<String, Object> newBase = new HashMap<>();
                    List<Object> grades = config.getList("grades");
                    for(Object grade : grades){
                        if(grade instanceof HashMap){
                            ((HashMap) grade).put("value", -1);
                            ((HashMap) grade).remove("alwaysVisible");
                        }
                    }
                    newBase.put("grades", grades);
                    config.base = newBase;
                }

                if(oneFile){
                    oneFileConfig.base.put(pdfFile.getName(), config.base);
                    return Map.entry(config, TwoStepListAction.CODE_OK);
                }

                config.setDestFile(new File(finalDirectory.getAbsolutePath() + File.separator + pdfFile.getName() + ".yml"));
                if(config.getDestFile().exists()){ // Check Already Exist
                    AlreadyExistDialog.ResultType result = alreadyExistDialog.showAndWait(config.getDestFile());
                    if(result == AlreadyExistDialog.ResultType.SKIP) return Map.entry(new Config(), 3);
                    else if(result == AlreadyExistDialog.ResultType.STOP) return Map.entry(new Config(), TwoStepListAction.CODE_STOP);
                    else if(result == AlreadyExistDialog.ResultType.RENAME) config.setDestFile(AlreadyExistDialog.rename(config.getDestFile()));
                }

                return Map.entry(config, TwoStepListAction.CODE_OK);
            }

            @Override
            public String getSortedDataName(Config config, boolean recursive) {
                return config.getName();
            }

            @Override
            public TwoStepListAction.ProcessResult completeData(Config config, boolean recursive){
                if(oneFile) return TwoStepListAction.ProcessResult.OK;

                try{
                    config.saveToDestFile();
                }catch(IOException e){
                    e.printStackTrace();
                    boolean result = DialogBuilder.showErrorAlert(TR.tr("Impossible d'enregistrer le fichier") + " \"" + config.getDestFile().toPath() + "\" (" + TR.tr("Correspond à l'édition") + " \"" + config.getName() + "\"", e.getMessage(), recursive);
                    if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                    if(result) return TwoStepListAction.ProcessResult.STOP;
                    else return TwoStepListAction.ProcessResult.SKIPPED;
                }
                return TwoStepListAction.ProcessResult.OK;
            }

            @Override
            public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive) {
                if(oneFile){
                    try{
                        oneFileConfig.save();
                    }catch(IOException e){
                        e.printStackTrace();
                        DialogBuilder.showErrorAlert(TR.tr("Impossible d'enregistrer le fichier") + " \"" + oneFileConfig.getDestFile().toPath() + "\"", e.getMessage(), false);
                        return;
                    }
                }

                Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Exportation terminée"));
                ButtonType open = new ButtonType(TR.tr("Ouvrir le dossier"), ButtonBar.ButtonData.YES);
                endAlert.getButtonTypes().add(open);
                if(onlyGrades) endAlert.setHeaderText(TR.tr("Le barème a bien été exporté"));
                else endAlert.setHeaderText(TR.tr("Les éditions ont bien été exportées"));

                String badFolderText = !excludedReasons.containsKey(1) ? "" : "\n(" + excludedReasons.get(1) + " " + TR.tr("documents ignorés car ils n'étaient pas dans le même dossier") + ")";
                String noEditText = !excludedReasons.containsKey(2) ? "" : "\n(" + excludedReasons.get(2) + " " + TR.tr("documents ignorés car ils n'avaient pas d'édition") + ")";
                String alreadyExistText = !excludedReasons.containsKey(3) ? "" : "\n(" + excludedReasons.get(3) + " " + TR.tr("documents ignorés car leur fichier YAML existait déjà") + ")";
                endAlert.setContentText(completedSize + "/" + originSize + " " + TR.tr("éditions exportées") + badFolderText + noEditText + alreadyExistText);

                Optional<ButtonType> optionSelected = endAlert.showAndWait();
                if(optionSelected.get() == open){
                    PlatformUtils.openDirectory(finalDirectory.getAbsolutePath());
                }


            }
        });
    }

}