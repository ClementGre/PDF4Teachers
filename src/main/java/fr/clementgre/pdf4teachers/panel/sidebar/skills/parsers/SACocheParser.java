/*
 * Copyright (c) 2022-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.parsers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.interfaces.windows.skillsassessment.SkillsAssessmentWindow;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Skill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Student;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.*;
import javafx.collections.FXCollections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SACocheParser {
    
    private File file;
    private Charset charset;
    private final SkillsAssessment assessment;
    private final SkillsAssessmentWindow assessmentWindow;
    public SACocheParser(SkillsAssessment assessment, SkillsAssessmentWindow assessmentWindow){
        this.assessment = assessment;
        this.assessmentWindow = assessmentWindow;
    }
    
    public void getAndUpdateData(){
        ComboBoxDialog<Charset> charsetChooser = new ComboBoxDialog<>(TR.tr("skillsSettingsWindow.sacocheImport.dialog.header"), TR.tr("skillsSettingsWindow.sacocheImport.dialog.header"), TR.tr("skillsSettingsWindow.sacocheImport.dialog.details"));
        charsetChooser.setItems(FXCollections.observableList(StringUtils.getAvailableCharsets()));
        charsetChooser.setSelected(Charset.defaultCharset());
        charsetChooser.getButtonTypes().clear();
        charsetChooser.addCancelButton(ButtonPosition.CLOSE);
        charsetChooser.addDefaultButton(TR.tr("file.choose"));
        charset = charsetChooser.execute();
        if(charset == null) return;
    
        file = FilesChooserManager.showFileDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR, TR.tr("dialog.file.extensionType.csv"), "*.csv");
        if(file == null || !file.exists()) return;
        try{
            SkillsAssessment loadedAssessment = getFromCsv();
            String details = TR.tr("skillsSettingsWindow.sacocheImport.endDialog.details");
            if(loadedAssessment == null) return;
            // Assessment infos
            assessmentWindow.name.setText(loadedAssessment.getName());
            assessmentWindow.clasz.setText(loadedAssessment.getClasz());
            assessmentWindow.date.getEditor().setText(loadedAssessment.getDate());
            // Students
            assessment.getStudents().clear(); assessment.getStudents().addAll(loadedAssessment.getStudents());
            assessmentWindow.sacocheExport.setDisable(assessment.getStudents().isEmpty());
            // Notations
            if(!loadedAssessment.getNotations().isEmpty()){
                assessment.setNotationType(Notation.NotationType.ICON);
                assessment.getNotations().clear(); assessment.getNotations().addAll(loadedAssessment.getNotations());
                assessmentWindow.notationsListingPane.updateNotationMode();
                assessmentWindow.notationsListingPane.updateGrid();
            }else{
                details = TR.tr("skillsSettingsWindow.sacocheImport.endDialog.details.noNotations");
            }
            // Skills
            assessment.getSkills().clear(); assessment.getSkills().addAll(loadedAssessment.getSkills());
            assessmentWindow.skillsListingPane.updateList();
            
            
            OKAlert alert = new OKAlert(TR.tr("skillsSettingsWindow.sacocheImport.endDialog.header"), TR.tr("skillsSettingsWindow.sacocheImport.endDialog.header"), details);
            alert.show();
            
        }catch(Exception ex){
            Log.eAlerted(ex);
        }
    }
    
    private enum CsvBlock { SKILLS, DETAILS, NOTATIONS}
    
    private SkillsAssessment getFromCsv() throws IOException, CsvException{
    
        BufferedReader reader = new BufferedReader(new FileReader(file, charset == null ? Charset.defaultCharset() : charset));
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build();
        List<String[]> list = csvReader.readAll();
        reader.close();
        csvReader.close();
        
        SkillsAssessment assessment = new SkillsAssessment();
        assessment.setNotationType(Notation.NotationType.ICON);
        assessment.getNotations().clear();
        
        CsvBlock block = CsvBlock.SKILLS;
        long[] studentIds = null;
        for(String[] line : list){
            if(isBlankLine(line)) continue;
            if(block == CsvBlock.SKILLS){
                // First/last line: Students ids/names
                if(line[0].isBlank()){
                    line = StringUtils.cleanArray(line);
                    if(studentIds == null){ // Ids
                        studentIds = new long[line.length];
                        for(int i = 0; i < line.length; i++){ // First/last args are always empty
                            if(!line[i].isBlank()) studentIds[i] = Long.parseLong(line[i]);
                        }
                    }else{ // Names
                        for(int i = 0; i < line.length; i++){ // First/last args are always empty
                            if(!line[i].isBlank()) assessment.getStudents().add(new Student(line[i], studentIds[i]));
                        }
                        block = CsvBlock.DETAILS;
                    }
                }else{ // Skill line
                    line = StringUtils.cleanArray(line);
                    long id = Long.parseLong(line[0]);
                    String[] textSplit = line[line.length-1].split("[\\[\\]]");
                    assessment.getSkills().add(new Skill(id, textSplit[0].trim(), textSplit[textSplit.length-1].trim()));
                }
            }else if(block == CsvBlock.DETAILS){
                if(assessment.getClasz().isBlank()) assessment.setClasz(line[0]);
                else if(assessment.getDate().isBlank()) assessment.setDate(line[0]);
                else if(assessment.getName().isBlank()) assessment.setName(line[0]);
                else if(line[0].equals("PDF4Teachers")) block = CsvBlock.NOTATIONS;
                
            }else if(line.length >= 4){ // block = CsvBlock.NOTATIONS
                if(line[0].equals("CLAVIER")) continue;
                assessment.getNotations().add(new Notation(assessment, line[1], line[2], line[0], line[3].replace("data:image/gif;base64,", "")));
            }
        }
        
        if(assessment.getNotations().isEmpty()){ // Not exported checking PDF4Teachers export
            CustomAlert alert = new WrongAlert(TR.tr("skillsSettingsWindow.sacocheImport.notPDF4TeachersImport.title"), TR.tr("skillsSettingsWindow.sacocheImport.notPDF4TeachersImport.details"), false);
            alert.getButtonTypes().clear();
            alert.addIgnoreButton(ButtonPosition.CLOSE);
            alert.addButton(TR.tr("dialog.actionError.cancelAll"), ButtonPosition.DEFAULT);
            if(alert.getShowAndWaitIsDefaultButton()){
                return null; // Cancelled by user.
            } // else when notations are empty, old notations are not cleared.
        }
        
        return assessment;
    }
    
    private boolean isBlankLine(String[] line){
        return Arrays.stream(line).allMatch(String::isBlank);
    }
}
