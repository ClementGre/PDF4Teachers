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
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.EditionSkill;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.Notation;
import fr.clementgre.pdf4teachers.panel.sidebar.skills.data.SkillsAssessment;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.dialogs.FilesChooserManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkillsCSVWriter {
    
    private final SkillsAssessment assessment;
    public SkillsCSVWriter(SkillsAssessment assessment){
        this.assessment = assessment;
    }
    
    public void exportAndSave(){
        
        if(!MainWindow.mainScreen.document.save(true)) return;
        
        try{
            List<EditionGrades> editionGrades = getMatchingEdits();
            
            File dest = getDestinationFile();
            if(dest == null) return;
            
            BufferedWriter writer  = new BufferedWriter(new FileWriter(dest, Charset.defaultCharset()));
            ICSVWriter csvWriter = new CSVWriterBuilder(writer).withSeparator(StringUtils.getCsvSeparator()).build();
            
            // Mapping notations keyboard char by ID
            HashMap<Long, String> notationIds = assessment.getNotationsWithDefaults().stream().collect(Collectors.toMap(Notation::getId, Notation::getKeyboardChar, (x, y) -> y, HashMap::new));
    
            
            // Assessment metadata
            csvWriter.writeNext(new String[]{assessment.getName(), assessment.getClasz(), assessment.getDate()});
            csvWriter.writeNext(new String[]{});
            
            
            // Other notation style with students in lines and skills in columns.
            /*// Header (one column for each skill)
            csvWriter.writeNext(Stream.concat(Stream.of(""),
                            assessment.getSkills().stream().map(s -> s.getAcronym() + " - " + s.getName()))
                    .toArray(String[]::new));
            
            // One line for each student
            editionGrades.forEach(editionGrade -> {
                csvWriter.writeNext(Stream.concat(Stream.of(editionGrade.fileName()), // First column: student name
                        assessment.getSkills().stream()
                                .map(s -> editionGrade.skills().stream().filter(es -> es.getSkillId() == s.getId()).findAny().orElse(null)) // map to matching skill
                                .map(s -> s == null ? "" : notationIds.get(s.getNotationId()))) // map to matching notation keyboard char
                                .toArray(String[]::new));
            });*/
    
            // One line for each skill, one line for each file
            csvWriter.writeNext(editionGrades.stream().map(EditionGrades::fileName).toArray(String[]::new));
            
            assessment.getSkills().forEach(skill -> {
                csvWriter.writeNext(Stream.concat(editionGrades.stream()
                                        .map(sg -> sg.skills().stream().filter(s -> s.getSkillId() == skill.getId()).findAny().orElse(null)) // map to matching skill
                                        .map(s -> s == null ? "" : notationIds.get(s.getNotationId())), // map to matching notation keyboard char
                        Stream.of(skill.getAcronym() + " - " + skill.getName())).toArray(String[]::new)); // Last column: notations names
            });
            
            
            // Notations keyboard chat legend
            csvWriter.writeNext(new String[]{});
            assessment.getNotationsWithDefaults()
                    .forEach(n -> csvWriter.writeNext(new String[]{n.getName(), n.getKeyboardChar()}));
            
            csvWriter.close();
            writer.close();
    
            DialogBuilder.showAlertWithOpenDirOrFileButton(TR.tr("actions.export.completedMessage"), TR.tr("actions.export.completedMessage"),
                    TR.tr("actions.export.fileAvailable"), dest.getParentFile().getAbsolutePath(), dest.getAbsolutePath());
            
        }catch(Exception e){
            Log.eAlerted(e);
        }
    }
    
    
    private File getDestinationFile(){
        return FilesChooserManager.showSaveDialog(FilesChooserManager.SyncVar.LAST_OPEN_DIR,
                TR.tr("skillsTab.skill.name")
                        + (assessment.getClasz().isBlank() ? "" : "_" + assessment.getClasz())
                        + (assessment.getName().isBlank() ? "" : "_" + assessment.getName())
                        + (assessment.getDate().isBlank() ? "" : "_" + assessment.getDate().replaceAll("/", "-"))
                        + ".csv",
                TR.tr("dialog.file.extensionType.csv"), ".csv");
    }
    
    public record EditionGrades(String fileName, List<EditionSkill> skills) {}
    
    // Returns null if cancelled by user.
    private List<EditionGrades> getMatchingEdits(){
        
        ArrayList<EditionGrades> fileGrades = new ArrayList<>();
        
        File editDir = new File(Main.dataFolder + "editions");
        if(!editDir.exists()) return List.of();
        
        
        for(File edit : Objects.requireNonNull(editDir.listFiles())){
            try{
    
                Element[] elements = Edition.simpleLoad(edit);
                SkillTableElement skillTableElement = (SkillTableElement) Arrays.stream(elements).filter(e -> e instanceof SkillTableElement).findFirst().orElse(null);
    
                if(skillTableElement != null && skillTableElement.getAssessmentId() == assessment.getId()){
                    fileGrades.add(new EditionGrades(FilesUtils.getNameWithoutExtension(Edition.getFileFromEdit(edit).toPath()), skillTableElement.getEditionSkills()));
                }
                
                
            }catch(Exception e){ Log.eNotified(e); }
        }
        
        return fileGrades.stream().sorted(Comparator.comparing(EditionGrades::fileName)).collect(Collectors.toList());
    }
    
}
