/*
 * Copyright (c) 2022-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.parsers;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
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
import java.util.regex.Pattern;
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
            if(loadedAssessment == null) return;
            // Assessment infos
            assessmentWindow.name.setText(loadedAssessment.getName());
            assessmentWindow.clasz.setText(loadedAssessment.getClasz());
            assessmentWindow.date.getEditor().setText(loadedAssessment.getDate());
            // Students
            assessment.getStudents().clear(); assessment.getStudents().addAll(loadedAssessment.getStudents());
            // Notations
            if(!loadedAssessment.getNotations().isEmpty()){
                assessment.setNotationType(Notation.NotationType.ICON);
                assessment.getNotations().clear(); assessment.getNotations().addAll(loadedAssessment.getNotations());
                assessmentWindow.notationsListingPane.updateNotationMode();
                assessmentWindow.notationsListingPane.updateGrid();
            }
            // Skills
            assessment.getSkills().clear(); assessment.getSkills().addAll(loadedAssessment.getSkills());
            assessmentWindow.skillsListingPane.updateList();
        }catch(Exception ex){
            ex.printStackTrace();
            new ErrorAlert(null, ex.getMessage(), false).show();
        }
    }
    
    private SkillsAssessment getFromCsv() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file, charset == null ? Charset.defaultCharset() : charset));
        
        SkillsAssessment assessment = new SkillsAssessment();
        
        // Sections:
        // 0: trash, blank lines
        // 1: Skills & Students
        // 2: Empty
        // 3: Assessment Class/Date/Name
        // 4: Authorized notation recap list
        // 5: All notations with details (if exported checking PDF4Teachers).
        AtomicInteger section = new AtomicInteger(1);
        Map<Integer, List<String>> sections = reader.lines().collect(Collectors.groupingBy(line -> { // Grouping lines, separated by blank lines
            if(isBlankLine(line)){ // Blank line
                section.incrementAndGet();
                return 0;
            }
            return section.get();
        }));
        
        // 1: Skills & Students
        long[] studentIds = null;
        for(String line : sections.get(1)){
            String[] args = splitQuotedLine(line);
            
            // First/last line: Students ids/names
            if(args[0].isBlank()){
                args = StringUtils.cleanArray(args);
                if(studentIds == null){ // Ids
                    studentIds = new long[args.length];
                    for(int i = 0; i < args.length; i++){ // First/last args are always empty
                        if(!args[i].isBlank()) studentIds[i] = Long.parseLong(args[i]);
                    }
                }else{ // Names
                    for(int i = 0; i < args.length; i++){ // First/last args are always empty
                        if(!args[i].isBlank()) assessment.getStudents().add(new Student(args[i], studentIds[i]));
                    }
                }
            }else{ // Skill line
                args = StringUtils.cleanArray(args);
                long id = Long.parseLong(args[0]);
                String[] textSplit = args[args.length-1].split("[\\[\\]]");
                assessment.getSkills().add(new Skill(id, textSplit[0], textSplit[textSplit.length-1]));
            }
        }
        
        // 3: Assessment Class/Date/Name
        for(String line : sections.get(3)){
            String[] args = splitQuotedLineClean(line);
            if(assessment.getClasz().isBlank()) assessment.setClasz(args[0]);
            else if(assessment.getDate().isBlank()) assessment.setDate(args[0]);
            else assessment.setName(args[0]);
        }
        
        // 4: Authorized notation recap list (Useless)
        
        // 5: All notations with details (if exported checking PDF4Teachers).
        assessment.setNotationType(Notation.NotationType.ICON);
        assessment.getNotations().clear();
        
        if(sections.get(5) == null){ // Not exported checking PDF4Teachers export
            CustomAlert alert = new WrongAlert(TR.tr("skillsSettingsWindow.sacocheImport.notPDF4TeachersImport.title"), TR.tr("skillsSettingsWindow.sacocheImport.notPDF4TeachersImport.details"), false);
            alert.getButtonTypes().clear();
            alert.addIgnoreButton(ButtonPosition.CLOSE);
            alert.addButton(TR.tr("dialog.actionError.cancelAll"), ButtonPosition.DEFAULT);
            if(alert.getShowAndWaitIsDefaultButton()){
                return null; // Cancelled by user.
            }// else when notations are empty, old notations are not cleared.
        }else{
            int i = 0;
            for(String line : sections.get(5)){
                String[] args = splitQuotedLineClean(line);
        
                // First line must be PDF4Teachers
                if(i == 0 && !args[0].equalsIgnoreCase("PDF4Teachers")) break;
        
                // Data starts at line 2
                if(i >= 2 && args.length >= 4){
                    // Notation id has no importance (the keyboard char will be used).
                    assessment.getNotations().add(new Notation(assessment, args[1], args[2], args[0], args[3].replace("data:image/gif;base64,", "")));
                }
                i++;
            }
        }
        
        
        
        return assessment;
    }
    
    private boolean isBlankLine(String line){
        return Arrays.stream(splitQuotedLine(line)).allMatch(String::isBlank);
    }
    
    // Removes first/last character if it is an apostrophe.
    private String[] splitQuotedLine(String line){
        String[] split = line.split(Pattern.quote(";"));
        // Remove quotes
        for(int i = 0; i < split.length; i++){
            if(split[i].startsWith("\"")) split[i] = split[i].substring(1);
            if(split[i].endsWith("\"")) split[i] = split[i].substring(0, split[i].length()-1);
        }
        return split;
    }
    // + Removes blank arguments.
    private String[] splitQuotedLineClean(String line){
        return StringUtils.cleanArray(splitQuotedLine(line));
    }
}
