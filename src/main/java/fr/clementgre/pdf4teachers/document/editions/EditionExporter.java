/*
 * Copyright (c) 2019-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.editions;

import fr.clementgre.pdf4teachers.datasaving.Config;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeCopyGradeScaleDialog;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.*;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EditionExporter {
    
    public static void showImportDialog(boolean onlyGrades){
        CustomAlert dialog = new CustomAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.importEdit.confirm.title"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("dialog.importEdit.confirm.header"));
        else dialog.setHeaderText(TR.tr("dialog.importEdit.confirm.onlyGrades.header"));
        
        CheckBox copyLocations = new CheckBox(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.copyLocations"));
        if(onlyGrades) dialog.getDialogPane().setContent(copyLocations);
        
        dialog.addCancelButton(ButtonPosition.CLOSE);
        dialog.addButton(TR.tr("dialog.importEdit.confirm.YesOneFile"), ButtonPosition.DEFAULT);
        if(!onlyGrades)
            dialog.addButton(TR.tr("dialog.importEdit.confirm.YesMultipleFiles"), ButtonPosition.OTHER_RIGHT);
        
        ButtonPosition option = dialog.getShowAndWaitGetButtonPosition(ButtonPosition.CLOSE);
        if(option == ButtonPosition.CLOSE) return;
        
        File file = null;
        boolean recursive = false;
        if(MainWindow.mainScreen.hasDocument(true)){
            MainWindow.mainScreen.document.edition.clearEdit(false);
            
            if(option == ButtonPosition.DEFAULT){
                file = FilesChooserManager.showFileDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR, TR.tr("dialog.file.extensionType.YAMLEditFile"), "*.yml");
            }else if(option == ButtonPosition.OTHER_RIGHT){
                file = FilesChooserManager.showFileDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR, TR.tr("dialog.file.extensionType.YAMLEditFile"), "*.yml");
                recursive = true;
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
                    new OKAlert(TR.tr("actions.import.completedMessage"), TR.tr("dialog.importEdit.completed.onlyGrades.header")).show();
                }
                MainWindow.mainScreen.document.updateEdition();
                MainWindow.filesTab.refresh();
            }catch(Exception e){
                Log.eAlerted(e);
            }
        }else{
            try{
                Config loadedConfig = new Config(file);
                loadedConfig.load();
                
                new TwoStepListAction<>(false, recursive, new TwoStepListInterface<String, Config>() {
                    @Override
                    public List<String> prepare(boolean recursive){
                        if(!recursive){
                            return Collections.singletonList(MainWindow.mainScreen.document.getFile().getName());
                        }
                        return new ArrayList<>(loadedConfig.base.keySet());
                    }
                    
                    @Override
                    @SuppressWarnings("unchecked")
                    public Map.Entry<Config, Integer> filterData(String fileName, boolean recursive) throws Exception{
                        
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
                        }
                        File pdfFile = new File(MainWindow.mainScreen.document.getFile().getParent() + File.separator + fileName);
                        
                        if(pdfFile.exists()){
                            if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(pdfFile.getAbsolutePath())){ // clear edit if doc is opened
                                MainWindow.mainScreen.document.edition.clearEdit(false);
                            }
                            
                            Config config = new Config(Edition.getEditFile(pdfFile));
                            config.setName(fileName);
                            
                            HashMap<String, Object> data = (HashMap<String, Object>) loadedConfig.base.get(pdfFile.getName());
                            if(onlyGrades){
                                config.load();
                                config.set("grades", Config.getList(data, "grades"));
                            }else{
                                config.base = data;
                            }
                            
                            return Map.entry(config, TwoStepListAction.CODE_OK);
                        }
                        boolean result = new WrongAlert(TR.tr("dialog.importEdit.errorNoMatch.header", fileName), TR.tr("dialog.importEdit.errorNoMatch.details"), true).execute();
                        if(result)
                            return Map.entry(new Config(), TwoStepListAction.CODE_STOP); // No match > Stop all
                        
                        return Map.entry(new Config(), 2); // No match
                    }
                    
                    @Override
                    public String getSortedDataName(Config config, boolean recursive){
                        return config.getName();
                    }
                    
                    @Override
                    public TwoStepListAction.ProcessResult completeData(Config config, boolean recursive){
                        try{
                            config.save();
                        }catch(IOException e){
                            Log.e(e);
                            boolean result = new ErrorAlert(TR.tr("dialog.importEdit.ioError.header", FilesUtils.getPathReplacingUserHome(config.getFile().toPath()), config.getName()), e.getMessage(), recursive).execute();
                            if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                            if(result) return TwoStepListAction.ProcessResult.STOP;
                            return TwoStepListAction.ProcessResult.SKIPPED;
                        }
                        if(Edition.getEditFile(MainWindow.mainScreen.document.getFile()).getAbsolutePath().equals(config.getFile().getAbsolutePath())){
                            MainWindow.mainScreen.document.loadEdition(false);
                        }
                        return TwoStepListAction.ProcessResult.OK;
                    }
                    
                    @Override
                    public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive){
                        OKAlert endAlert = new OKAlert(TR.tr("actions.import.completedMessage"), TR.tr("dialog.importEdit.completed.header"));
                        
                        String noMatchesText = !excludedReasons.containsKey(2) ? "" : "\n(" + TR.tr("dialog.importEdit.completed.recap.ignored", excludedReasons.get(2)) + ")";
                        endAlert.setContentText(TR.tr("dialog.importEdit.completed.recap.imported", completedSize, originSize) + noMatchesText);
                        
                        endAlert.show();
                    }
                });
                
                MainWindow.filesTab.refresh();
                
            }catch(Exception e){
                Log.eAlerted(e);
            }
        }
        
        
    }
    
    public static void showExportDialog(final boolean onlyGrades){
        CustomAlert dialog = new CustomAlert(Alert.AlertType.CONFIRMATION, TR.tr("dialog.exportEdit.confirm.title"));
        if(!onlyGrades) dialog.setHeaderText(TR.tr("dialog.exportEdit.confirm.header"));
        else dialog.setHeaderText(TR.tr("dialog.exportEdit.confirm.onlyGrades.header"));
        
        dialog.addCancelButton(ButtonPosition.CLOSE);
        dialog.addButton(TR.tr("dialog.exportEdit.confirm.YesThisFile"), ButtonPosition.DEFAULT);
        
        ButtonType yesAll = null;
        ButtonType yesAllOneFile = null;
        if(!onlyGrades){
            yesAll = dialog.addButton(TR.tr("dialog.exportEdit.confirm.YesMultipleFile"), ButtonPosition.OTHER_RIGHT);
            yesAllOneFile = dialog.addButton(TR.tr("dialog.exportEdit.confirm.YesMultipleFileInOne"), ButtonPosition.OTHER_RIGHT);
            
            dialog.getDialogPane().lookupButton(yesAll).setDisable(MainWindow.filesTab.getOpenedFiles().size() <= 1);
            dialog.getDialogPane().lookupButton(yesAllOneFile).setDisable(MainWindow.filesTab.getOpenedFiles().size() <= 1);
        }
        
        ButtonType option = dialog.getShowAndWait();
        if(option == null || option.getButtonData().isCancelButton()) return;
        
        File directory = null;
        boolean recursive = !option.getButtonData().isDefaultButton();
        boolean oneFile = option == yesAllOneFile;
        final Config oneFileConfig = new Config();
        
        if(MainWindow.mainScreen.hasDocument(true)){
            if(MainWindow.mainScreen.document.save(true)){
                if(option.getButtonData().isDefaultButton() || option == yesAll){
                    directory = FilesChooserManager.showDirectoryDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR);
                }else if(option == yesAllOneFile){
                    File file = FilesChooserManager.showSaveDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR, "edits.yml", "YAML", ".yml");
                    if(file != null){
                        oneFileConfig.setFile(file);
                        directory = new File(file.getParent());
                    }
                }
            }
        }
        if(directory == null) return; // check isPresent btw
        
        final File finalDirectory = directory;
        AlreadyExistDialogManager alreadyExistDialogManager = new AlreadyExistDialogManager(recursive);
        new TwoStepListAction<>(false, recursive, new TwoStepListInterface<File, Config>() {
            @Override
            public List<File> prepare(boolean recursive){
                if(recursive){
                    return MainWindow.filesTab.getOpenedFiles();
                }
                return Collections.singletonList(MainWindow.mainScreen.document.getFile());
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public Map.Entry<Config, Integer> filterData(File pdfFile, boolean recursive) throws Exception{
                if(!FilesUtils.isInSameDir(pdfFile.toPath(), MainWindow.mainScreen.document.getFile().toPath()))
                    return Map.entry(new Config(), 1); // Check same dir
                
                File editFile = Edition.getEditFile(pdfFile);
                if(!editFile.exists()) return Map.entry(new Config(), 2); // Check HasEdit
                
                Config config = new Config(editFile);
                config.load();
                config.setName(pdfFile.getName());
                
                if(onlyGrades){
                    HashMap<String, Object> newBase = new HashMap<>();
                    List<Object> grades = config.getList("grades");
                    for(Object grade : grades){
                        if(grade instanceof HashMap){
                            ((HashMap<String, Object>) grade).put("value", -1);
                            ((HashMap<?, ?>) grade).remove("alwaysVisible");
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
                    AlreadyExistDialogManager.ResultType result = alreadyExistDialogManager.showAndWait(config.getDestFile());
                    if(result == AlreadyExistDialogManager.ResultType.SKIP) return Map.entry(new Config(), 3);
                    if(result == AlreadyExistDialogManager.ResultType.STOP)
                        return Map.entry(new Config(), TwoStepListAction.CODE_STOP);
                    if(result == AlreadyExistDialogManager.ResultType.RENAME)
                        config.setDestFile(AlreadyExistDialogManager.rename(config.getDestFile()));
                }
                
                return Map.entry(config, TwoStepListAction.CODE_OK);
            }
            
            @Override
            public String getSortedDataName(Config config, boolean recursive){
                return config.getName();
            }
            
            @Override
            public TwoStepListAction.ProcessResult completeData(Config config, boolean recursive){
                if(oneFile) return TwoStepListAction.ProcessResult.OK;
                
                try{
                    config.saveToDestFile();
                }catch(IOException e){
                    Log.e(e);
                    boolean result = new ErrorAlert(TR.tr("dialog.exportEdit.ioError.header", FilesUtils.getPathReplacingUserHome(config.getDestFile().toPath()), config.getName()), e.getMessage(), recursive).execute();
                    if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                    if(result) return TwoStepListAction.ProcessResult.STOP;
                    return TwoStepListAction.ProcessResult.SKIPPED;
                }
                return TwoStepListAction.ProcessResult.OK;
            }
            
            @Override
            public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive){
                if(oneFile){
                    try{
                        oneFileConfig.save();
                    }catch(IOException e){
                        Log.e(e);
                        new ErrorAlert(TR.tr("dialog.file.saveError.header", FilesUtils.getPathReplacingUserHome(oneFileConfig.getDestFile().toPath())), e.getMessage(), false).showAndWait();
                        return;
                    }
                }
                
                String header = onlyGrades ? TR.tr("dialog.exportEdit.completed.header.onlyGrades") : TR.tr("dialog.exportEdit.completed.header");
                
                String badFolderText = !excludedReasons.containsKey(1) ? "" : "\n(" + TR.tr("dialog.exportEdit.completed.recap.ignored.notSameFolder", excludedReasons.get(1)) + ")";
                String noEditText = !excludedReasons.containsKey(2) ? "" : "\n(" + TR.tr("exportWindow.dialogs.completed.ignored.noEdit", excludedReasons.get(2)) + ")";
                String alreadyExistText = !excludedReasons.containsKey(3) ? "" : "\n(" + TR.tr("dialog.exportEdit.completed.recap.ignored.fileAlreadyExisting", excludedReasons.get(3)) + ")";
                String details = TR.tr("dialog.exportEdit.completed.recap.exported", completedSize, originSize) + badFolderText + noEditText + alreadyExistText;
                
                DialogBuilder.showAlertWithOpenDirButton(TR.tr("actions.export.completedMessage"), header, details, finalDirectory.getAbsolutePath());
                
            }
        });
    }
    
}
