/*
 * Copyright (c) 2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.skills.parsers;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.SkillTableElement;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SACocheWriter {
    
    private final SkillsAssessment assessment;
    public SACocheWriter(SkillsAssessment assessment){
        this.assessment = assessment;
    }
    
    public void exportAndSave(){
        try{
    
            File dest = getDestinationFile();
            if(dest == null) return;
    
            List<StudentGrades> studentGrades = getMatchingEdits();
    
            BufferedWriter writer  = new BufferedWriter(new FileWriter(dest, Charset.defaultCharset()));
            ICSVWriter csvWriter = new CSVWriterBuilder(writer).withSeparator(';').build();
           
            // Write Student ids
            csvWriter.writeNext(Stream.concat(Stream.of(""), studentGrades.stream().map(s -> Long.toString(s.studentId()))).toArray(String[]::new));
            
            // Write Skills with values
            LinkedHashMap<Long, String> notationIds = assessment.getNotationsWithDefaults().stream().collect(Collectors.toMap(Notation::getId, Notation::getKeyboardChar, (x, y) -> y, LinkedHashMap::new));
            
            assessment.getSkills().forEach(skill -> {
                csvWriter.writeNext(Stream.concat(Stream.concat(
                                Stream.of(Long.toString(skill.getId())), // First column: notations ids
                                studentGrades.stream()
                                        .map(sg -> sg.skills().stream().filter(s -> s.getSkillId() == skill.getId()).findAny().orElse(null)) // map to matching skill
                                        .map(s -> s == null ? "" : notationIds.get(s.getNotationId()))), // map to matching notation keyboard char
                                Stream.of(skill.getAcronym() + " " + skill.getName())).toArray(String[]::new)); // Last column: notations names
            });
    
            // Write Student names
            csvWriter.writeNext(Stream.concat(Stream.of(""), studentGrades.stream().map(StudentGrades::studentName)).toArray(String[]::new));
            
            // Assessment meta data
            csvWriter.writeNext(new String[]{});
            csvWriter.writeNext(new String[]{});
            csvWriter.writeNext(new String[]{assessment.getClasz()});
            csvWriter.writeNext(new String[]{assessment.getDate()});
            csvWriter.writeNext(new String[]{assessment.getName()});
            csvWriter.writeNext(new String[]{});
            csvWriter.writeNext(new String[]{"CODAGES AUTORISÉS : " + String.join(",", notationIds.values())});
            csvWriter.writeNext(new String[]{});
            
            // Notations
            csvWriter.writeNext(new String[]{"PDF4Teachers"});
            csvWriter.writeNext(new String[]{"CLAVIER", "SIGLE", "LEGENDE", "IMAGE"});
            assessment.getNotations().forEach(notation -> {
                csvWriter.writeNext(new String[]{notation.getKeyboardChar(), notation.getAcronym(), notation.getName(),
                        (assessment.getNotationType() == Notation.NotationType.ICON ? "data:image/gif;base64," : "") + notation.getData()});
            });
    
            csvWriter.close();
            writer.close();
            
        }catch(Exception e){
            e.printStackTrace();
            ErrorAlert.showErrorAlert(e);
        }
    }
    
    
    private File getDestinationFile(){
        return FilesChooserManager.showSaveDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR,
                "saisie_deportee_rempli_" + assessment.getClasz() + "_" + assessment.getName() + ".csv",
                TR.tr("dialog.file.extensionType.csv"), ".csv");
    }
    
    private List<StudentGrades> getMatchingEdits(){
    
        ArrayList<StudentGrades> studentGrades = new ArrayList<>();
        
        File editDir = new File(Main.dataFolder + "editions");
        if(!editDir.exists()) return List.of();
        
        for(File edit : Objects.requireNonNull(editDir.listFiles())){
            Element[] elements = new Element[]{};
            try{
                elements = Edition.simpleLoad(edit);
            }catch(Exception e){ e.printStackTrace(); }
            
            SkillTableElement skillTableElement = (SkillTableElement) Arrays.stream(elements).filter(e -> e instanceof SkillTableElement).findFirst().orElse(null);
            
            if(skillTableElement != null && skillTableElement.getAssessmentId() == assessment.getId()){
                assessment.getStudents().stream().filter(s -> s.id() == skillTableElement.getStudentId()).findAny().ifPresent(student -> {
                    studentGrades.add(new StudentGrades(student.id(), student.name(), Edition.getFileFromEdit(edit).getName(), skillTableElement.getEditionSkills()));
                });
            }
    
        }
        return studentGrades.stream().sorted(Comparator.comparing(StudentGrades::studentName)).collect(Collectors.toList());
    }
}
