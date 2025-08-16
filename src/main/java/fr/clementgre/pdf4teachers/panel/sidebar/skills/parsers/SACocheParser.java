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
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.*;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.*;
import javafx.collections.FXCollections;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

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
    
        List<String[]> lines;
        try(BufferedReader reader = new BufferedReader(new FileReader(file, charset == null ? Charset.defaultCharset() : charset));
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                    .build()){
            lines = csvReader.readAll();
        }
        
        SkillsAssessment assessment = new SkillsAssessment();
        assessment.setNotationType(Notation.NotationType.ICON);
        assessment.getNotations().clear();
        
        CsvBlock block = CsvBlock.SKILLS;
        long[] studentIds = null;
        HashMap<Long, ArrayList<Map.Entry<Long, String>>> studentsSkills = new HashMap<>(); // <StudentId, <SkillId, Notation>>
        HashMap<Long, ArrayList<EditionSkill>> studentsEditionSkills = new HashMap<>(); // <StudentId, <SkillId, Notation>>
        for(int i = 0; i < lines.size(); i++){
            String[] line = lines.get(i);
            String[] nextLine = i+1 < lines.size() ? lines.get(i + 1) : new String[0];
            if(isBlankLine(line)) continue;
            
            if(block == CsvBlock.SKILLS){
                // First/last line: Students ids/names
                if(line[0].isBlank()){
                    line = StringUtils.cleanArray(line); // First args is always empty
                    if(studentIds == null){ // Ids
                        studentIds = new long[line.length];
                        for(int k = 0; k < line.length; k++){ // Array cleaned
                            if(!line[k].isBlank()) studentIds[k] = Long.parseLong(line[k]);
                        }
                    }else{ // Names
                        for(int k = 0; k < line.length; k++){ // Array cleaned
                            if(!line[k].isBlank()){
                                // Next line is not cleaned because it can be blank for some students
                                String supportModality = nextLine.length > k+1 && !nextLine[k+1].isBlank() ? nextLine[k+1] : "";
                                assessment.getStudents().add(new Student(line[k], supportModality, studentIds[k], new ArrayList<>()));
                            }
                        }
                        block = CsvBlock.DETAILS;
                    }
                }else{ // Skill line
                    if(studentIds == null) studentIds = new long[0];
                    String[] cleanLine = StringUtils.cleanArray(line);
                    
                    long id = Long.parseLong(cleanLine[0]); // Skill ID
                    String[] textSplit = cleanLine[cleanLine.length-1].split("[\\[\\]]");
                    assessment.getSkills().add(new Skill(id, textSplit[0].trim(), textSplit[textSplit.length-1].trim()));
    
                    for(int k = 1; k < line.length-1; k++){
                        if(!line[k].isBlank()){
                            long studentId = studentIds[k-1];
                            if(!studentsSkills.containsKey(studentId)) studentsSkills.put(studentId, new ArrayList<>());
                            studentsSkills.get(studentId).add(Map.entry(id, line[k]));
                        }
                    }
                }
            }else if(block == CsvBlock.DETAILS){
                if(assessment.getClasz().isBlank()) assessment.setClasz(line[0]);
                else if(assessment.getDate().isBlank()) assessment.setDate(line[0]);
                else if(assessment.getName().isBlank()) assessment.setName(line[0]);
                else if(line[0].equals("PDF4Teachers")) block = CsvBlock.NOTATIONS;
                
            }else if(line.length >= 4){ // block = CsvBlock.NOTATIONS
                if(line[0].equals("CLAVIER")) continue;
                Notation notation = new Notation(assessment, line[1], line[2], line[0], line[3].replace("data:image/gif;base64,", ""));
                
                studentsSkills.forEach((studentId, skills) ->
                        skills.stream()
                                .filter(skill -> skill.getValue().equalsIgnoreCase(notation.getKeyboardChar()))
                                .forEach(skill -> {
                                    if(!studentsEditionSkills.containsKey(studentId))
                                        studentsEditionSkills.put(studentId, new ArrayList<>());
                                    studentsEditionSkills.get(studentId).add(new EditionSkill(skill.getKey(), notation.getId()));
                                })
                );

                assessment.getNotations().add(notation);
            }
        }
        // For default notations
        SkillsAssessment.getOtherNotations().forEach(notation ->
                studentsSkills.forEach((studentId, skills) ->
                        skills.stream()
                                .filter(skill -> skill.getValue().equalsIgnoreCase(notation.getKeyboardChar()))
                                .forEach(skill -> {
                                    if(!studentsEditionSkills.containsKey(studentId))
                                        studentsEditionSkills.put(studentId, new ArrayList<>());
                                    studentsEditionSkills.get(studentId).add(new EditionSkill(skill.getKey(), notation.getId()));
                                })
                )
        );

        
        if(assessment.getNotations().isEmpty()){ // Not exported checking PDF4Teachers export
            CustomAlert alert = new WrongAlert(TR.tr("skillsSettingsWindow.sacocheImport.notPDF4TeachersImport.title"), TR.tr("skillsSettingsWindow.sacocheImport.notPDF4TeachersImport.details"), false);
            alert.getButtonTypes().clear();
            alert.addIgnoreButton(ButtonPosition.CLOSE);
            alert.addButton(TR.tr("dialog.actionError.cancelAll"), ButtonPosition.DEFAULT);
            if(alert.getShowAndWaitIsDefaultButton()){
                return null; // Cancelled by user.
            } // else when notations are empty, old notations are not cleared.
        }

        assessment.getStudents()
                .stream()
                .filter(student -> studentsEditionSkills.containsKey(student.id()))
                .forEach(student -> student.editionSkills()
                        .addAll(studentsEditionSkills.get(student.id())));
        
        return assessment;
    }
    
    private boolean isBlankLine(String[] line){
        return Arrays.stream(line).allMatch(String::isBlank);
    }
}
