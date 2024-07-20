/*
 * Copyright (c) 2021-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.interfaces.StringToIntConverter;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportWindow extends AlternativeWindow<VBox> {
    
    private final List<File> files;
    
    public ExportWindow(List<File> files){
        super(new VBox(), StageWidth.LARGE, TR.tr("exportWindow.title.oneFile"));
        this.files = files;
    }
    
    HBox path = new HBox();
    TextField filePath = new TextField();
    HBox types = new HBox();
    CheckBox textElements = new CheckBox(TR.tr("elements.name.texts"));
    CheckBox gradesElements = new CheckBox(TR.tr("elements.name.grades"));
    CheckBox drawElements = new CheckBox(TR.tr("elements.name.paints"));
    CheckBox skillElements = new CheckBox(TR.tr("elements.name.skillsTable"));
    Button export;
    
    @Override
    public void setupSubClass(){
        
        // DIRECTORY INPUTS
        HBox filePathPane = new HBox();
        filePath.setText(MainWindow.filesTab.getCurrentDir() != null ? MainWindow.filesTab.getCurrentDir().getAbsolutePath() : System.getProperty("user.home"));
        filePath.setPromptText(TR.tr("file.destinationFolder"));
        filePath.setMinWidth(1);
        filePath.setMinHeight(30);
        HBox.setHgrow(filePath, Priority.ALWAYS);
        HBox.setHgrow(filePathPane, Priority.SOMETIMES);
        PaneUtils.setHBoxPosition(filePathPane, 0, 30, new Insets(5, 5, 0, 0));
        filePathPane.getChildren().add(filePath);
        Button changePath = new Button(TR.tr("file.browse"));
        PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(5, 0, 0, 0));
        changePath.setPadding(new Insets(0, 5, 0, 5));
        path.getChildren().addAll(filePathPane, changePath);
        
        changePath.setOnAction(event -> {
            
            final DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(TR.tr("dialog.file.selectFolder.title"));
            chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) : new File(files.getFirst().getParentFile().getPath())));
            
            File file = chooser.showDialog(Main.window);
            if(file != null){
                filePath.setText(file.getAbsolutePath() + File.separator);
            }
        });
        
        // TYPES OPTIONS
        textElements.setSelected(true);
        gradesElements.setSelected(true);
        drawElements.setSelected(true);
        skillElements.setSelected(true);
        types.getChildren().addAll(textElements, gradesElements, drawElements, skillElements);
        
        HBox.setMargin(textElements, new Insets(20, 10, 0, 0));
        HBox.setMargin(gradesElements, new Insets(20, 10, 0, 0));
        HBox.setMargin(drawElements, new Insets(20, 10, 0, 0));
        HBox.setMargin(skillElements, new Insets(20, 0, 0, 0));
        
        // BUTTONS
        export = new Button(TR.tr("actions.export"));
        Button cancel = new Button(TR.tr("actions.cancel"));
        cancel.setOnAction(event -> close());
        
        setButtons(cancel, export);
        
        if(files.size() == 1) setupSimplePanel();
        else setupComplexPanel();
    }
    
    @Override
    public void afterShown(){
    }
    
    public void setupSimplePanel(){
        setSubHeaderText(TR.tr("exportWindow.oneFile.header"));
        
        HBox name = new HBox();
        TextField fileName = new TextField(files.getFirst().getName());
        fileName.setPromptText(TR.tr("file.documentName"));
        fileName.setMinWidth(1);
        HBox.setHgrow(fileName, Priority.ALWAYS);
        PaneUtils.setHBoxPosition(fileName, 0, 30, 0);
        name.getChildren().addAll(fileName);
        
        Label dpiLabel = new Label(TR.tr("exportWindow.options.dpi"));
        PaneUtils.setHBoxPosition(dpiLabel, 0, 25, 0);
        
        Spinner<Integer> imagesDPI = new Spinner<>(50, 900, (int) MainWindow.userData.settingsExportImagesDPI, 50);
        imagesDPI.setEditable(true);
        imagesDPI.getValueFactory().setConverter(new StringToIntConverter((int) MainWindow.userData.settingsExportImagesDPI));
        PaneUtils.setHBoxPosition(imagesDPI, 85, 25, 0);
        imagesDPI.valueProperty().addListener((observable, oldValue, newValue) -> {
            MainWindow.userData.settingsExportImagesDPI = newValue;
        });
        
        HBox dpiSettings = new HBox(10, dpiLabel, imagesDPI);
        
        VBox settings = new VBox(10, dpiSettings);
        
        root.getChildren().addAll(name, path, types, settings);
        
        VBox.setMargin(settings, new Insets(20, 0, 0, 0));
        
        export.setOnAction(event -> {
            
            if(!fileName.getText().endsWith(".pdf")) fileName.setText(fileName.getText() + ".pdf");
            
            startExportation(new File(filePath.getText()), "", "", "", "", fileName.getText(),
                    imagesDPI.getValue(), false, textElements.isSelected(), gradesElements.isSelected(), drawElements.isSelected(), skillElements.isSelected());
        });
    }
    
    CheckBox onlyEdited;
    public void setupComplexPanel(){
        setSubHeaderText(TR.tr("exportWindow.multipleFiles.header"));
        
        HBox name = new HBox();
        
        TextField prefix = new TextField(MainWindow.userData.lastExportFileNamePrefix);
        prefix.setPromptText(TR.tr("string.prefix"));
        prefix.setMinWidth(1);
        //prefix.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(prefix, Priority.ALWAYS);
        PaneUtils.setHBoxPosition(prefix, 0, 30, 0);
        prefix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNamePrefix = newValue);
        
        TextField fileName = new TextField(TR.tr("file.documentName"));
        fileName.setDisable(true);
        fileName.setAlignment(Pos.CENTER);
        fileName.setMinHeight(30);
        PaneUtils.setHBoxPosition(fileName, 0, 30, 0);
        
        TextField suffix = new TextField(MainWindow.userData.lastExportFileNameSuffix);
        suffix.setPromptText(TR.tr("string.suffix"));
        suffix.setMinWidth(1);
        HBox.setHgrow(suffix, Priority.ALWAYS);
        PaneUtils.setHBoxPosition(suffix, 0, 30, 0);
        suffix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameSuffix = newValue);
        
        name.getChildren().addAll(prefix, fileName, suffix);
        
        HBox replace = new HBox();
        
        Label replaceText = new Label(TR.tr("exportWindow.multipleFiles.replaceFields.replace"));
        
        TextField replaceInput = new TextField(MainWindow.userData.lastExportFileNameReplace);
        replaceInput.setMinWidth(1);
        HBox.setHgrow(replaceInput, Priority.ALWAYS);
        PaneUtils.setHBoxPosition(replaceInput, 0, 30, 0);
        replaceInput.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameReplace = newValue);
        
        Label byText = new Label(TR.tr("exportWindow.multipleFiles.replaceFields.by"));
        
        TextField byInput = new TextField(MainWindow.userData.lastExportFileNameBy);
        byInput.setMinWidth(1);
        HBox.setHgrow(byInput, Priority.ALWAYS);
        PaneUtils.setHBoxPosition(byInput, 0, 30, 0);
        byInput.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameBy = newValue);
        
        replace.getChildren().addAll(replaceText, replaceInput, byText, byInput);
        
        
        // SETTINGS
        
        
        onlyEdited = new CheckBox(TR.tr("exportWindow.options.onlyEdited"));
        onlyEdited.setSelected(true);
        onlyEdited.setWrapText(true);
        
        Label dpiLabel = new Label(TR.tr("exportWindow.options.dpi"));
        PaneUtils.setHBoxPosition(dpiLabel, 0, 25, 0);
        
        Spinner<Integer> imagesDPI = new Spinner<>(50, 900, (int) MainWindow.userData.settingsExportImagesDPI, 50);
        imagesDPI.setEditable(true);
        imagesDPI.getValueFactory().setConverter(new StringToIntConverter((int) MainWindow.userData.settingsExportImagesDPI));
        PaneUtils.setHBoxPosition(imagesDPI, 85, 25, 0);
        imagesDPI.valueProperty().addListener((observable, oldValue, newValue) -> {
            MainWindow.userData.settingsExportImagesDPI = newValue;
        });
        
        HBox dpiSettings = new HBox(10, dpiLabel, imagesDPI);
        VBox settings = new VBox(10, onlyEdited, dpiSettings);
        
        root.getChildren().addAll(name, replace, path, types, settings);
        
        HBox.setMargin(fileName, new Insets(0, 0, 0, 0));
        HBox.setMargin(prefix, new Insets(0, 0, 0, 0));
        HBox.setMargin(suffix, new Insets(0, 0, 0, 0));
        
        HBox.setMargin(replaceText, new Insets(10, 5, 0, 0));
        HBox.setMargin(replaceInput, new Insets(5, 0, 0, 0));
        HBox.setMargin(byText, new Insets(10, 5, 0, 5));
        HBox.setMargin(byInput, new Insets(5, 0, 0, 0));
        
        VBox.setMargin(onlyEdited, new Insets(20, 0, 5, 0));
        
        export.setOnAction(event -> startExportation(new File(filePath.getText()), prefix.getText(), suffix.getText(), replaceInput.getText(), byInput.getText(), "",
                imagesDPI.getValue(), onlyEdited.isSelected(), textElements.isSelected(), gradesElements.isSelected(), drawElements.isSelected(), skillElements.isSelected()));
        
        onlyEdited.selectedProperty().addListener((observable, oldValue, newValue) -> updateMultipleFilesTitle());
        updateMultipleFilesTitle();
    }
    private void updateMultipleFilesTitle(){
        String title;
        if(onlyEdited.isSelected())
            title = TR.tr("exportWindow.title.multipleFiles", files.stream().filter((f) -> Edition.getEditFile(f).exists()).toArray().length);
        else title = TR.tr("exportWindow.title.multipleFiles", files.size());
        setHeaderText(title);
        setTitle(title);
    }
    
    public void startExportation(File directory, String prefix, String suffix, String replaceText, String replaceByText, String customName,
                                 int imagesDPI, boolean onlyEdited, boolean textElements, boolean gradesElements, boolean drawElements, boolean skillElements){
        
        directory.mkdirs();
        
        AlreadyExistDialogManager alreadyExistDialogManager = new AlreadyExistDialogManager(customName.isEmpty());
        new TwoStepListAction<>(true, customName.isEmpty(), new TwoStepListInterface<File, Map.Entry<File, File>>() {
            @Override
            public List<File> prepare(boolean recursive){
                return files;
            }
            
            @Override
            public Map.Entry<Map.Entry<File, File>, Integer> filterData(File pdfFile, boolean recursive){
                
                if(onlyEdited){ // Check only edited export
                    if(!Edition.getEditFile(pdfFile).exists()){
                        return Map.entry(Map.entry(new File(""), new File("")), 1);
                    }
                }
                
                String fileName = pdfFile.getName();
                if(recursive){
                    
                    fileName = StringUtils.removeAfterLastOccurrenceIgnoringCase(fileName, ".pdf");
                    fileName = fileName.replace(replaceText, replaceByText);
                    fileName = prefix + fileName + suffix + ".pdf";
                }else{
                    fileName = StringUtils.removeAfterLastOccurrenceIgnoringCase(customName, ".pdf") + ".pdf";
                }
                
                File toFile = new File(directory.getAbsolutePath() + File.separator + fileName);
                
                if(toFile.exists()){ // Check Already Exist
                    AlreadyExistDialogManager.ResultType result = alreadyExistDialogManager.showAndWait(toFile);
                    if(result == AlreadyExistDialogManager.ResultType.SKIP)
                        return Map.entry(Map.entry(new File(""), new File("")), 2);
                    if(result == AlreadyExistDialogManager.ResultType.STOP)
                        return Map.entry(Map.entry(new File(""), new File("")), TwoStepListAction.CODE_STOP);
                    if(result == AlreadyExistDialogManager.ResultType.RENAME)
                        toFile = AlreadyExistDialogManager.rename(toFile);
                }
                
                return Map.entry(Map.entry(pdfFile, toFile), TwoStepListAction.CODE_OK);
            }
            
            @Override
            public String getSortedDataName(Map.Entry<File, File> data, boolean recursive){
                return data.getKey().getName();
            }
            
            @Override
            public TwoStepListAction.ProcessResult completeData(Map.Entry<File, File> data, boolean recursive){
                try{
                    boolean ok = new ExportRenderer().exportFile(data.getKey(), data.getValue(), imagesDPI, textElements, gradesElements, drawElements, skillElements);
                    if(ok) return TwoStepListAction.ProcessResult.OK;
                    return TwoStepListAction.ProcessResult.SKIPPED;
                }catch(Exception e){
                    Log.e(e);
                    if(PlatformUtils.runAndWait(() -> new ErrorAlert(TR.tr("exportWindow.dialogs.exportError.header", data.getKey().getName()), e.getMessage(), recursive).execute())){
                        return TwoStepListAction.ProcessResult.STOP;
                    }
                    if(!recursive){
                        return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                    }
                    return TwoStepListAction.ProcessResult.SKIPPED;
                }
            }
            
            @Override
            public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive){
                close();
                
                String header;
                if(completedSize == 0) header = TR.tr("exportWindow.dialogs.completed.header.noDocument");
                else if(completedSize == 1) header = TR.tr("exportWindow.dialogs.completed.header.oneDocument");
                else header = TR.tr("exportWindow.dialogs.completed.header.multipleDocument", completedSize);
                
                String details;
                String noEditText = !excludedReasons.containsKey(1) ? "" : "\n(" + TR.tr("exportWindow.dialogs.completed.ignored.noEdit", excludedReasons.get(1)) + ")";
                String alreadyExistText = !excludedReasons.containsKey(2) ? "" : "\n(" + TR.tr("exportWindow.dialogs.completed.ignored.alreadyExisting", excludedReasons.get(2)) + ")";
                details = TR.tr("exportWindow.dialogs.completed.exported", completedSize, originSize) + noEditText + alreadyExistText;
                
                DialogBuilder.showAlertWithOpenDirButton(TR.tr("actions.export.completedMessage"), header, details, directory.getAbsolutePath());
            }
        });
    }
}
