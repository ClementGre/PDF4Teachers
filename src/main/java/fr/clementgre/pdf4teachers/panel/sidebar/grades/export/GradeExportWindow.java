/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades.export;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.AlternativeWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.regex.Pattern;

public class GradeExportWindow extends AlternativeWindow<TabPane> {
    
    ExportPane exportAllTab = new ExportPane(this, TR.tr("gradeTab.gradeExportWindow.exportMode.together.name"), 0, false, true, true, false);
    ExportPane exportAllSplitTab = new ExportPane(this, TR.tr("gradeTab.gradeExportWindow.exportMode.separately.name"), 1, true, true, true, true);
    ExportPane exportThisTab = new ExportPane(this, TR.tr("gradeTab.gradeExportWindow.exportMode.onlyThis.name"), 2, false, false, false, true);
    
    public GradeExportWindow(){
        super(new TabPane(), StageWidth.LARGE, TR.tr("gradeTab.gradeExportWindow.title"),
                TR.tr("gradeTab.gradeExportWindow.title"), TR.tr("gradeTab.gradeExportWindow.header"));
    }
    
    @Override
    public void setupSubClass(){
        root.setStyle("-fx-padding: 0;");
        root.getTabs().addAll(exportAllTab, exportAllSplitTab, exportThisTab);
        
        setupButtons();
    }
    
    private void setupButtons(){
        Button cancel = new Button(TR.tr("actions.cancel"));
        Button export = new Button(TR.tr("actions.export"));
        
        export.setOnAction(event -> {
            ExportPane pane = (ExportPane) root.getSelectionModel().getSelectedItem();
            end(new GradeExportRenderer(pane).start(), pane);
        });
        cancel.setOnAction(event -> {
            close();
        });
        
        setButtons(cancel, export);
    }
    
    private void end(int exported, ExportPane pane){
        close();
        
        String header;
        if(exported == 0) header = TR.tr("exportWindow.dialogs.completed.header.noDocument");
        else if(exported == 1) header = TR.tr("exportWindow.dialogs.completed.header.oneDocument");
        else header = TR.tr("exportWindow.dialogs.completed.header.multipleDocument", exported);
        
        DialogBuilder.showAlertWithOpenDirButton(TR.tr("actions.export.completedMessage"), header, null, pane.filePath.getText());
        
    }
    
    @Override
    public void afterShown(){
    
    }
    
    static class ExportPane extends Tab {
        
        public int type;
        GradeExportWindow window;
        
        boolean fileNameCustom, studentNameCustom, multipleFilesCustom, canExportTextElements;
        
        VBox root = new VBox();
        
        public TextField fileNameSimple, fileNamePrefix, fileNameSuffix, fileNameReplace, fileNameBy;
        public TextField studentNameSimple, studentNameReplace, studentNameBy;
        public TextField filePath;
        
        public CheckBox settingsOnlySameGradeScale = new CheckBox(TR.tr("gradeTab.gradeExportWindow.options.onlySameGradeScale"));
        public CheckBox settingsOnlyCompleted = new CheckBox(TR.tr("gradeTab.gradeExportWindow.options.onlyCompleted"));
        public CheckBox settingsOnlySameDir = new CheckBox(TR.tr("gradeTab.gradeExportWindow.options.onlySameDir"));
        public CheckBox settingsAttributeTotalLine = new CheckBox(TR.tr("gradeTab.gradeExportWindow.options.attributeTotalLine"));
        public CheckBox settingsAttributeAverageLine = new CheckBox(TR.tr("gradeTab.gradeExportWindow.options.attributeAverageLine"));
        public CheckBox settingsWithTxtElements = new CheckBox(TR.tr("gradeTab.gradeExportWindow.options.withTxtElements"));
        public Slider settingsTiersExportSlider = new Slider(1, 5, MainWindow.userData.settingsTiersExportSlider);
    
        public ToggleButton settingsCSVSeparatorComma = new ToggleButton(TR.tr("gradeTab.gradeExportWindow.options.csvSeparator.comma"));
        public ToggleButton settingsCSVSeparatorSemicolon = new ToggleButton(TR.tr("gradeTab.gradeExportWindow.options.csvSeparator.semicolon"));
        public ToggleButton settingsCSVFormulaEnglish = new ToggleButton(TR.tr("gradeTab.gradeExportWindow.options.csvLanguage.english"));
        public ToggleButton settingsCSVFormulaLanguage = new ToggleButton(TR.tr("language.name").split(Pattern.quote(" "))[0]);
        
        public ExportPane(GradeExportWindow window, String tabName, int type, boolean fileNameCustom, boolean studentNameCustom, boolean multipleFilesCustom, boolean canExportTextElements){
            
            super(tabName);
            this.window = window;
            this.type = type;
            this.fileNameCustom = fileNameCustom;
            this.studentNameCustom = studentNameCustom;
            this.multipleFilesCustom = multipleFilesCustom;
            this.canExportTextElements = canExportTextElements;
            
            setClosable(false);
            setContent(root);
            root.setStyle("-fx-padding: 10;");
            
            setupFileNameForm();
            setupStudentNameForm();
            setupPathForm();
            setupSettingsForm();
            setupExportLanguageSettings();
            
        }
        
        public void setupFileNameForm(){
            
            VBox info = generateInfo(TR.tr("file.documentName") + " :", false);
            
            if(fileNameCustom){
                HBox fileNamePrefixSuffixBox = new HBox();
                HBox fileNameReplaceBox = new HBox();
                
                fileNamePrefix = new TextField(MainWindow.userData.lastExportFileNamePrefix);
                fileNamePrefix.setPromptText(TR.tr("string.prefix"));
                PaneUtils.setHBoxPosition(fileNamePrefix, -1, 30, 0, 2.5);
                fileNamePrefix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNamePrefix = newValue);
                
                TextField fileName = new TextField(TR.tr("file.documentName"));
                fileName.setDisable(true);
                fileName.setAlignment(Pos.CENTER);
                PaneUtils.setHBoxPosition(fileName, 0, 30, 0, 2.5);
                
                fileNameSuffix = new TextField(MainWindow.userData.lastExportFileNameSuffix);
                fileNameSuffix.setPromptText(TR.tr("string.suffix"));
                PaneUtils.setHBoxPosition(fileNameSuffix, -1, 30, 0, 2.5);
                fileNameSuffix.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameSuffix = newValue);
                
                fileNamePrefixSuffixBox.getChildren().addAll(fileNamePrefix, fileName, fileNameSuffix);
                
                
                Label replaceText = new Label(TR.tr("exportWindow.multipleFiles.replaceFields.replace"));
                PaneUtils.setHBoxPosition(replaceText, 0, 30, 2.5);
                
                fileNameReplace = new TextField(MainWindow.userData.lastExportFileNameReplace);
                PaneUtils.setHBoxPosition(fileNameReplace, -1, 30, 0, 2.5);
                fileNameReplace.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameReplace = newValue);
                
                Label byText = new Label(TR.tr("exportWindow.multipleFiles.replaceFields.by"));
                PaneUtils.setHBoxPosition(byText, 0, 30, 2.5);
                
                fileNameBy = new TextField(MainWindow.userData.lastExportFileNameBy);
                PaneUtils.setHBoxPosition(fileNameBy, -1, 30, 0, 2.5);
                fileNameBy.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileNameBy = newValue);
                
                fileNameReplaceBox.getChildren().addAll(replaceText, fileNameReplace, byText, fileNameBy);
                
                root.getChildren().addAll(info, fileNamePrefixSuffixBox, fileNameReplaceBox);
                
            }else{
                
                fileNameSimple = new TextField(MainWindow.userData.lastExportFileName.isEmpty() || type == 2 ? StringUtils.removeAfterLastOccurrence(MainWindow.mainScreen.document.getFileName(), ".pdf") + ".csv" : MainWindow.userData.lastExportFileName);
                fileNameSimple.setPromptText(TR.tr("file.documentName"));
                PaneUtils.setHBoxPosition(fileNameSimple, 0, 30, 0, 2.5);
                if(type != 2)
                    fileNameSimple.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportFileName = newValue);
                root.getChildren().addAll(info, fileNameSimple);
                
            }
            
        }
        
        public void setupStudentNameForm(){
            
            
            if(studentNameCustom){
                VBox info = generateInfo(TR.tr("gradeTab.gradeExportWindow.fields.studentNameAutoGenerated"), true);
                HBox studentNameReplaceBox = new HBox();
                
                Label replaceText = new Label(TR.tr("exportWindow.multipleFiles.replaceFields.replace"));
                PaneUtils.setHBoxPosition(replaceText, 0, 30, 2.5);
                
                studentNameReplace = new TextField(MainWindow.userData.lastExportStudentNameReplace);
                PaneUtils.setHBoxPosition(studentNameReplace, -1, 30, 0, 2.5);
                studentNameReplace.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportStudentNameReplace = newValue);
                
                Label byText = new Label(TR.tr("exportWindow.multipleFiles.replaceFields.by"));
                PaneUtils.setHBoxPosition(byText, 0, 30, 2.5);
                
                studentNameBy = new TextField(MainWindow.userData.lastExportStudentNameBy);
                PaneUtils.setHBoxPosition(studentNameBy, -1, 30, 0, 2.5);
                studentNameBy.textProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.lastExportStudentNameBy = newValue);
                
                studentNameReplaceBox.getChildren().addAll(replaceText, studentNameReplace, byText, studentNameBy);
                
                root.getChildren().addAll(info, studentNameReplaceBox);
                
            }else{
                VBox info = generateInfo(TR.tr("gradeTab.gradeExportWindow.fields.studentName"), true);
                
                studentNameSimple = new TextField(StringUtils.removeAfterLastOccurrence(MainWindow.mainScreen.document.getFileName(), ".pdf"));
                studentNameSimple.setPromptText(TR.tr("gradeTab.gradeExportWindow.fields.studentName"));
                PaneUtils.setHBoxPosition(studentNameSimple, 0, 30, 0, 2.5);
                
                root.getChildren().addAll(info, studentNameSimple);
                
            }
            
        }
        
        public void setupPathForm(){
            
            VBox info = generateInfo(TR.tr("file.destinationFolder") + " :", true);
            
            HBox filePathBox = new HBox();
            
            filePath = new TextField(MainWindow.mainScreen.document.getFile().getParentFile().getPath() + File.separator);
            PaneUtils.setHBoxPosition(filePath, -1, 30, 0, 2.5);
            
            Button changePath = new Button(TR.tr("file.browse"));
            PaneUtils.setHBoxPosition(changePath, 0, 30, new Insets(2.5, 0, 2.5, 2.5));
            
            filePathBox.getChildren().addAll(filePath, changePath);
            
            root.getChildren().addAll(info, filePathBox);
            
            changePath.setOnAction(event -> {
                
                final DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle(TR.tr("dialog.file.selectFolder.title"));
                chooser.setInitialDirectory((new File(filePath.getText()).exists() ? new File(filePath.getText()) : new File(MainWindow.mainScreen.document.getFile().getParentFile().getPath() + File.separator)));
                
                File file = chooser.showDialog(Main.window);
                if(file != null) filePath.setText(file.getAbsolutePath() + File.separator);
            });
            
        }
        
        public void setupSettingsForm(){
            
            VBox info = generateInfo(TR.tr("options.title"), true);
            
            HBox tiersExport = new HBox();
            Label tiersExportLabel = new Label(TR.tr("gradeTab.gradeExportWindow.options.tiersExportSlider"));
            tiersExport.getChildren().addAll(tiersExportLabel, settingsTiersExportSlider);
            settingsTiersExportSlider.valueProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsTiersExportSlider = newValue.intValue());
            
            settingsTiersExportSlider.setSnapToTicks(true);
            settingsTiersExportSlider.setMajorTickUnit(1);
            settingsTiersExportSlider.setMinorTickCount(0);
            
            PaneUtils.setVBoxPosition(settingsOnlySameGradeScale, 0, 30, 2.5, 0);
            PaneUtils.setVBoxPosition(settingsOnlyCompleted, 0, 30, 2.5, 0);
            PaneUtils.setVBoxPosition(settingsOnlySameDir, 0, 30, 2.5, 0);
            PaneUtils.setVBoxPosition(settingsAttributeTotalLine, 0, 30, 2.5, 0);
            PaneUtils.setVBoxPosition(settingsAttributeAverageLine, 0, 30, 2.5, 0);
            PaneUtils.setVBoxPosition(settingsWithTxtElements, 0, 30, 2.5, 0);
            PaneUtils.setHBoxPosition(settingsTiersExportSlider, 0, 30, 2.5, 0);
            PaneUtils.setHBoxPosition(tiersExportLabel, 0, 30, 2.5, 0);
            
            root.getChildren().add(info);
            
            settingsAttributeTotalLine.setSelected(MainWindow.userData.settingsAttributeTotalLine);
            if(multipleFilesCustom){
                settingsOnlySameGradeScale.setSelected(MainWindow.userData.settingsOnlySameGradeScale);
                settingsOnlyCompleted.setSelected(MainWindow.userData.settingsOnlyCompleted);
                settingsOnlySameDir.setSelected(MainWindow.userData.settingsOnlySameDir);
                settingsAttributeAverageLine.setSelected(MainWindow.userData.settingsAttributeMoyLine);
                root.getChildren().addAll(settingsOnlySameGradeScale, settingsOnlyCompleted, settingsOnlySameDir, settingsAttributeTotalLine, settingsAttributeAverageLine);
            }else root.getChildren().add(settingsAttributeTotalLine);
            
            if(canExportTextElements){
                settingsWithTxtElements.setSelected(MainWindow.userData.settingsWithTxtElements);
                root.getChildren().add(settingsWithTxtElements);
            }
            root.getChildren().add(tiersExport);
            
            settingsOnlySameGradeScale.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsOnlySameGradeScale = newValue);
            settingsOnlyCompleted.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsOnlyCompleted = newValue);
            settingsOnlySameDir.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsOnlySameDir = newValue);
            settingsAttributeTotalLine.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsAttributeTotalLine = newValue);
            settingsAttributeAverageLine.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsAttributeMoyLine = newValue);
            settingsWithTxtElements.selectedProperty().addListener((observable, oldValue, newValue) -> MainWindow.userData.settingsWithTxtElements = newValue);
            
        }
        
        public void setupExportLanguageSettings(){
            ToggleGroup separatorGroup = new ToggleGroup();
            separatorGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue == null) separatorGroup.selectToggle(oldValue);
            });
    
            Label separatorLabel = new Label(TR.tr("gradeTab.gradeExportWindow.options.csvSeparator"));
            HBox separatorButtons = new HBox(settingsCSVSeparatorComma, settingsCSVSeparatorSemicolon);
            VBox separatorInfo = new VBox(separatorLabel, separatorButtons);
            separatorInfo.setSpacing(5);
           
            HBox box = new HBox(separatorInfo);
            PaneUtils.setVBoxPosition(box, 0, 0, 2.5, 5);
            
            settingsCSVSeparatorComma.setToggleGroup(separatorGroup);
            settingsCSVSeparatorSemicolon.setToggleGroup(separatorGroup);
            
            if(TR.tr("chars.csvSeparator").charAt(0) == ';'){
                settingsCSVSeparatorSemicolon.setSelected(true);
            }else settingsCSVSeparatorComma.setSelected(true);
            
            if(!Main.settings.language.getValue().equals("en_us")){
                ToggleGroup formulaGroup = new ToggleGroup();
                formulaGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                    if(newValue == null) formulaGroup.selectToggle(oldValue);
                });
                settingsCSVFormulaEnglish.setToggleGroup(formulaGroup);
                settingsCSVFormulaLanguage.setToggleGroup(formulaGroup);
                settingsCSVFormulaLanguage.setSelected(true);
    
                Label formulaLabel = new Label(TR.tr("gradeTab.gradeExportWindow.options.csvLanguage"));
                HBox formulaButtons = new HBox(settingsCSVFormulaEnglish, settingsCSVFormulaLanguage);
                VBox formulaInfo = new VBox(formulaLabel, formulaButtons);
                formulaInfo.setSpacing(5);
                formulaInfo.setPadding(new Insets(0, 0, 0, 15));
                box.getChildren().add(formulaInfo);
            }
    
            VBox container = generateInfo(null, true);
            container.getChildren().add(box);
            root.getChildren().add(container);
        }
    
    }
}