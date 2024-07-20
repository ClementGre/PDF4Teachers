/*
 * Copyright (c) 2020-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades.export;

import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeRating;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradeExportRenderer {
    
    private StringBuilder content = new StringBuilder();
    
    private ArrayList<GradeRating> gradeScale;
    private final ArrayList<ExportFile> files = new ArrayList<>();
    private final int exportTier;
    
    private int exported;
    private final DecimalFormat decimalFormat;
    private final String separator;
    
    private final GradeExportWindow.ExportPane pane;
    private AlreadyExistDialogManager alreadyExistDialogManager;
    
    public GradeExportRenderer(GradeExportWindow.ExportPane pane){
        this.pane = pane;
        this.exportTier = (int) pane.settingsTiersExportSlider.getValue();
    
        
        if(pane.settingsCSVFormulaEnglish.isSelected()){
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
            decimalFormat = new DecimalFormat("0.###", symbols);
            decimalFormat.setMaximumIntegerDigits(4);
        }else decimalFormat = MainWindow.gradesDigFormat;
        
        separator = pane.settingsCSVSeparatorComma.isSelected() ? "," : ";";
        
    }
    
    public int start(){
        if(!getFiles()){
            return exported;
        }
        
        if(pane.type != 1){
            alreadyExistDialogManager = new AlreadyExistDialogManager(false);
            
            try{
                if(pane.settingsAttributeTotalLine.isSelected()){
                    generateNamesLine(false);
                    generateGradeScaleLine();
                }else{
                    generateNamesLine(true);
                }
                if(pane.settingsAttributeAverageLine.isSelected()){
                    generateMoyLine();
                }
                for(ExportFile file : files){
                    generateStudentLine(file);
                }
                if(!save(null)) return exported;
            }catch(Exception e){
                Log.e(e);
                new ErrorAlert(TR.tr("gradeTab.gradeExportWindow.fatalError.title"), e.getMessage(), false).showAndWait();
                return exported;
            }
        }else{ // SPLIT
            alreadyExistDialogManager = new AlreadyExistDialogManager(true);
            
            for(ExportFile file : files){
                try{
                    if(pane.settingsAttributeTotalLine.isSelected()){
                        generateNamesLine(false);
                        generateGradeScaleLine();
                    }else{
                        generateNamesLine(true);
                        
                    }
                    generateStudentLine(file);
                    
                    if(!save(file)) return exported;
                    
                }catch(Exception e){
                    Log.e(e);
                    boolean result = new ErrorAlert(TR.tr("gradeTab.gradeExportWindow.error.title", file.file.getName()), e.getMessage(), true).execute();
                    if(result) return exported;
                }
            }
            
        }
        return exported;
    }
    
    // GENERATORS
    
    public void generateNamesLine(boolean includeGradeScale){
        
        content.append(TR.tr("gradeTab.gradeExportWindow.csv.titles.parts"));
        
        for(GradeRating rating : gradeScale){
            if(rating.isRoot() && rating.outOfTotal > 0){
                if(includeGradeScale) content.append(separator).append(rating.name).append(" /").append(decimalFormat.format(rating.outOfTotal));
                else content.append(separator).append(rating.name).append(" (/").append(decimalFormat.format(rating.outOfTotal)).append(")");
            }
            content.append(separator).append(rating.name).append(includeGradeScale ? " /" + decimalFormat.format(rating.total) : "");
        }
        content.append("\n");
    }
    
    public void generateGradeScaleLine(){
        
        content.append(TR.tr("gradeTab.gradeExportWindow.csv.titles.gradeScale"));
        
        for(GradeRating rating : gradeScale){
            if(rating.isRoot() && rating.outOfTotal > 0){
                content.append(separator).append(decimalFormat.format(rating.outOfTotal));
            }
            content.append(separator).append(decimalFormat.format(rating.total));
        }
        content.append("\n");
        
    }
    
    public void generateMoyLine(){
        
        char x = 'B';
        int startY = pane.settingsAttributeTotalLine.isSelected() ? 4 : 3;
        int endY = startY + files.size() - 1;
        
        content.append(TR.tr("gradeTab.gradeExportWindow.csv.titles.average"));
        
        for(GradeRating rating : gradeScale){
            String formula = pane.settingsCSVFormulaEnglish.isSelected() ? "AVERAGE" : TR.tr("gradeTab.gradeExportWindow.csv.formulas.average.name").toUpperCase();
            if(rating.isRoot() && rating.outOfTotal > 0){
                content.append(separator).append("=").append(formula).append("(").append(x).append(startY).append(":").append(x).append(endY).append(")");
                x++;
            }
            content.append(separator).append("=").append(formula).append("(").append(x).append(startY).append(":").append(x).append(endY).append(")");
            x++;
        }
        content.append("\n");
    }
    
    public void generateStudentLine(ExportFile file){
        
        if(pane.studentNameSimple != null){
            content.append(pane.studentNameSimple.getText());
        }else{
            content.append(StringUtils.removeAfterLastOccurrence(file.file.getName(), ".pdf").replaceAll(Pattern.quote(pane.studentNameReplace.getText()), pane.studentNameBy.getText()));
        }
        
        boolean hasOutOfColumn = false;
        for(GradeElement rating : file.grades){
            if(rating.isRoot() && rating.getOutOfTotal() > 0){ // Add outof column if needed.
                hasOutOfColumn = true;
                content.append(separator).append(rating.getValue() == -1 ? "" :
                        decimalFormat.format(rating.getValue() / rating.getTotal() * rating.getOutOfTotal()));
            }
            content.append(separator).append(rating.getValue() == -1 ? "" : decimalFormat.format(rating.getValue()));
        }
        content.append("\n");
        
        if(pane.settingsWithTxtElements.isSelected()){
            generateCommentsLines(file, hasOutOfColumn);
        }
    }
    
    
    public void generateCommentsLines(ExportFile file, boolean hasOutOfColumn){
        
        content.append(TR.tr("gradeTab.gradeExportWindow.csv.titles.comments"));
        
        if(!file.comments.isEmpty()){
            
            // Sort text elements (top to bottom)
            file.comments.sort((e1, e2) -> e2.compareTo(e1));
            
            // Sort grades (bottom to top) and put them in a HashMap
            LinkedHashMap<GradeElement, ArrayList<String>> matches = new LinkedHashMap<>();
            for(GradeElement grade : file.grades.stream().sorted((e1, e2) -> e2.compareTo(e1)).toList()){
                matches.put(grade, new ArrayList<>());
            }
            
            GradeElement lastGrade = null;
            for(Map.Entry<GradeElement, ArrayList<String>> match : matches.entrySet()){
                lastGrade = match.getKey();
                ArrayList<String> comments = match.getValue();
                int minPage = lastGrade.getPageNumber();
                int minY = lastGrade.getRealY();
                
                // For each element of this document, if they are after the grade, add them to the ArrayList
                TextElement element = !file.comments.isEmpty() ? file.comments.getFirst() : null;
                while(element != null){
                    if(element.getPageNumber() == minPage && element.getRealY() > minY || element.getPageNumber() > minPage){
                        comments.add(element.getText());
                        file.comments.removeFirst();
                        element = !file.comments.isEmpty() ? file.comments.getFirst() : null;
                    }else{
                        element = null;
                    }
                }
            }
            // Adding all others comments
            while(!file.comments.isEmpty() && lastGrade != null){
                matches.get(lastGrade).add(file.comments.getFirst().getText());
                file.comments.removeFirst();
            }
            
            // At this point, the HashMap is sorted by the grades position in the document.
            // This part is sorting comments into rows (and no more columns).
            ArrayList<String> rows = new ArrayList<>();
            
            for(GradeElement grade : file.grades){
                // Foe each comment of this grade
                int rowNumber = 0;
                Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.reverse(list);
                    return list.stream();
                });
                
                // Comments are (bottom to top) so one have to reverse the order.
                List<String> reversedComments = matches.get(grade);
                Collections.reverse(reversedComments);
                
                for(String comment : reversedComments){
                    if(rows.size() <= rowNumber) rows.add(separator + comment);
                    else rows.set(rowNumber, rows.get(rowNumber) + separator + comment);
                    rowNumber++;
                }
                // Fill rows until rowNumber == 100 (it is needed to add semicolon even for void cells)
                while(rowNumber < 100){
                    if(rows.size() <= rowNumber) rows.add(separator);
                    else rows.set(rowNumber, rows.get(rowNumber) + separator);
                    rowNumber++;
                }
            }
            
            // Filling rows
            content.append(
                    rows.stream()
                            .map(line -> (hasOutOfColumn ? separator : "") + line + "\n")
                            .collect(Collectors.joining())
            );
        }else content.append("\n");
    }
    
    // OTHERS
    
    public boolean getFiles(){
        
        // Add the file from which we are exporting
        try{
            ExportFile defaultFile = new ExportFile(MainWindow.mainScreen.document.getFile(), exportTier, pane.settingsWithTxtElements.isSelected());
            gradeScale = defaultFile.generateGradeScale();
            
            if(!(pane.settingsOnlyCompleted.isSelected() && !defaultFile.isCompleted())){
                files.add(defaultFile);
            }
            
        }catch(Exception e){
            Log.e(e);
            new ErrorAlert(TR.tr("gradeTab.gradeExportWindow.unableToReadEditionError.header", MainWindow.mainScreen.document.getFileName()) + "\n" +
                    TR.tr("gradeTab.gradeExportWindow.unableToReadEditionError.header.sourceDocument"), e.getMessage(), false).showAndWait();
            return false;
        }
        
        
        if(pane.type != 2){
            for(File file : MainWindow.filesTab.files.getItems()){
                try{
                    if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                    if(pane.settingsOnlySameDir.isSelected() && !MainWindow.mainScreen.document.getFile().getParent().equals(file.getParent()))
                        continue;
                    
                    ExportFile exportFile = new ExportFile(file, exportTier, pane.settingsWithTxtElements.isSelected());
                    
                    if(pane.settingsOnlySameGradeScale.isSelected() && !exportFile.isSameGradeScale(gradeScale))
                        continue;
                    if(pane.settingsOnlyCompleted.isSelected() && !exportFile.isCompleted()) continue;
                    
                    files.add(exportFile);
                    
                }catch(Exception e){
                    Log.e(e);
                    boolean result = new ErrorAlert(TR.tr("gradeTab.gradeExportWindow.unableToReadEditionError.header", file.getName()) + "\n" +
                            TR.tr("gradeTab.gradeExportWindow.unableToReadEditionError.header.sourceDocument"), e.getMessage(), true).execute();
                    if(result) return false;
                }
            }
        }
        return true;
    }
    
    public boolean save(ExportFile source) throws IOException{
        
        String filePath = pane.filePath.getText();
        String fileName;
        
        if(source != null){ // type = 1 -> Splited export
            fileName = pane.fileNamePrefix.getText() + StringUtils.removeAfterLastOccurrence(source.file.getName(), ".pdf")
                    .replaceAll(Pattern.quote(pane.fileNameReplace.getText()), pane.fileNameBy.getText()) + pane.fileNameSuffix.getText();
        }else{ // other
            fileName = StringUtils.removeAfterLastOccurrence(pane.fileNameSimple.getText(), ".csv");
        }
        
        File file = new File(filePath + File.separator + fileName + ".csv");
        file.getParentFile().mkdirs();
        
        if(file.exists()){
            AlreadyExistDialogManager.ResultType result = alreadyExistDialogManager.showAndWait(file);
            if(result == AlreadyExistDialogManager.ResultType.SKIP) return true;
            if(result == AlreadyExistDialogManager.ResultType.STOP) return false;
            if(result == AlreadyExistDialogManager.ResultType.RENAME)
                file = AlreadyExistDialogManager.rename(file);
        }
        
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false));
        
        writer.write(String.valueOf(content));
        
        writer.flush();
        writer.close();
        
        exported++;
        content = new StringBuilder();
        return true;
    }
    
}
