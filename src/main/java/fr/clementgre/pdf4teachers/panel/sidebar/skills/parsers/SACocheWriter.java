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
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Student;
import fr.clementgre.pdf4teachers.utils.dialogs.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.CustomAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.OKAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.WrongAlert;

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
        
        if(!MainWindow.mainScreen.document.save(true)) return;
        
        OKAlert alert = new OKAlert(TR.tr("skillsSettingsWindow.sacocheExport.dialog.header"), TR.tr("skillsSettingsWindow.sacocheExport.dialog.header"),
                TR.tr("skillsSettingsWindow.sacocheExport.dialog.details"));
        alert.addCancelButton(ButtonPosition.CLOSE);
        if(alert.getShowAndWaitIsCancelButton()) return;
        
        try{
    
            List<StudentGrades> studentGrades = getMatchingEdits();
            if(studentGrades == null) return;
            
            File dest = getDestinationFile();
            if(dest == null) return;
            
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(dest, Charset.defaultCharset()));
                ICSVWriter csvWriter = new CSVWriterBuilder(writer).withSeparator(';').build()){
                
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
                                    Stream.of(skill.getAcronym() + " [] [] " + skill.getName())).toArray(String[]::new)); // Last column: notations names
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
                // Do not write the notations because it can give false information to the SACoche parser. (ex: "2";"R";... can put an R to the skill of ID 2)
                /*csvWriter.writeNext(new String[]{"PDF4Teachers"});
                csvWriter.writeNext(new String[]{"CLAVIER", "SIGLE", "LEGENDE", "IMAGE"});
                assessment.getNotations().forEach(notation -> {
                    csvWriter.writeNext(new String[]{notation.getKeyboardChar(), notation.getAcronym(), notation.getName(),
                            (assessment.getNotationType() == Notation.NotationType.ICON ? "data:image/gif;base64," : "") + notation.getData()});
                });*/
            }
    
            DialogBuilder.showAlertWithOpenDirOrFileButton(TR.tr("actions.export.completedMessage"), TR.tr("actions.export.completedMessage"),
                    TR.tr("actions.export.fileAvailable"), dest.getParentFile().getAbsolutePath(), dest.getAbsolutePath());
            
        }catch(Exception e){
            Log.eAlerted(e);
        }
    }
    
    
    private File getDestinationFile(){
        return FilesChooserManager.showSaveDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR,
                "saisie_deportee_rempli_"
                        + (assessment.getClasz().isBlank() ? "" : "_" + assessment.getClasz())
                        + (assessment.getName().isBlank() ? "" : "_" + assessment.getName())
                        + (assessment.getDate().isBlank() ? "" : "_" + assessment.getDate().replaceAll("/", "-"))
                        + ".csv",
                TR.tr("dialog.file.extensionType.csv"), ".csv");
    }
    
    // Returns null if cancelled by user.
    private List<StudentGrades> getMatchingEdits(){
    
        ArrayList<StudentGrades> studentGrades = new ArrayList<>();
        
        File editDir = new File(Main.dataFolder + "editions");
        if(!editDir.exists()) return List.of();
    
        HashMap<String, ArrayList<String>> doubleAffectation = new HashMap<>();
        ArrayList<String> aloneStudents;
        ArrayList<String> aloneDocuments = new ArrayList<>();
        
        
        for(File edit : Objects.requireNonNull(editDir.listFiles())){
            Element[] elements = {};
            try{
                elements = Edition.simpleLoad(edit);
            }catch(Exception e){ Log.eNotified(e); }
            
            SkillTableElement skillTableElement = (SkillTableElement) Arrays.stream(elements).filter(e -> e instanceof SkillTableElement).findFirst().orElse(null);
            
            if(skillTableElement != null && skillTableElement.getAssessmentId() == assessment.getId()){
                
                Optional<Student> studentOptional = assessment.getStudents().stream().filter(s -> s.id() == skillTableElement.getStudentId()).findAny();
                studentOptional.ifPresentOrElse(student -> {
                    studentGrades.stream().filter(s -> s.studentName().equals(student.name())).forEach(sg -> {
                        if(!doubleAffectation.containsKey(student.name())){
                            doubleAffectation.put(student.name(), new ArrayList<>(Arrays.asList(sg.fileName(), Edition.getFileFromEdit(edit).getName())));
                        }else{
                            doubleAffectation.put(student.name(), new ArrayList<>(
                                    Stream.concat(
                                            doubleAffectation.get(student.name()).stream(),
                                            Stream.of(sg.fileName(), Edition.getFileFromEdit(edit).getName())
                                    ).distinct().collect(Collectors.toList())));
                        }
                    });
                    studentGrades.add(new StudentGrades(student.id(), student.name(), Edition.getFileFromEdit(edit).getName(), skillTableElement.getEditionSkills()));
                }, () -> aloneDocuments.add(Edition.getFileFromEdit(edit).getName()));
            }
        }
        aloneStudents = assessment.getStudents()
                .stream()
                .filter(s -> studentGrades.stream().noneMatch(sg -> sg.studentId() == s.id()))
                .map(Student::name)
                .collect(Collectors.toCollection(ArrayList::new));
    
        StringBuilder details = new StringBuilder();
        if(!doubleAffectation.isEmpty()){
            details.append(TR.tr("skillsSettingsWindow.export.cohesionError.details.doubleAffectation")).append("\n");
            doubleAffectation.forEach((studentName, files) -> {
                String lastFile = files.getLast();
                files.remove(lastFile);
                
                details.append("  - ")
                        .append(TR.tr("skillsSettingsWindow.export.cohesionError.details.doubleAffectation.details", String.join(", ", files), lastFile, studentName))
                        .append("\n");
            });
            details.append("\n");
        }
        if(!aloneStudents.isEmpty()){
            details.append(TR.tr("skillsSettingsWindow.export.cohesionError.details.aloneStudent")).append("\n");
            aloneStudents.forEach(file -> {
                details.append("  - ").append(file).append("\n");
            });
            details.append("\n");
        }
        if(!aloneDocuments.isEmpty()){
            details.append(TR.tr("skillsSettingsWindow.export.cohesionError.details.aloneDocument")).append("\n");
            aloneDocuments.forEach(file -> {
                details.append("  - ").append(file).append("\n");
            });
            details.append("\n");
        }
        
        if(!details.isEmpty()){
            CustomAlert alert = new WrongAlert(TR.tr("skillsSettingsWindow.export.cohesionError.header"), details.toString(), false);
            alert.getButtonTypes().clear();
            alert.addIgnoreButton(ButtonPosition.CLOSE);
            alert.addButton(TR.tr("dialog.actionError.cancelAll"), ButtonPosition.DEFAULT);
            if(alert.getShowAndWaitIsDefaultButton()){
                return null; // Cancelled by user.
            }
        }
        
        return studentGrades.stream().sorted(Comparator.comparing(StudentGrades::studentName)).collect(Collectors.toList());
    }
}
