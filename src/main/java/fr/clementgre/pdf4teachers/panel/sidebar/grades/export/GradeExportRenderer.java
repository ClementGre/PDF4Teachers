/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades.export;

import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeRating;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradeExportRenderer {
    
    String text = "";
    
    public ArrayList<GradeRating> gradeScale;
    ArrayList<ExportFile> files = new ArrayList<>();
    int exportTier;
    
    int exported = 0;
    
    GradeExportWindow.ExportPane pane;
    AlreadyExistDialogManager alreadyExistDialogManager;
    
    public GradeExportRenderer(GradeExportWindow.ExportPane pane){
        this.pane = pane;
        this.exportTier = (int) pane.settingsTiersExportSlider.getValue();
        
        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save(true);
        
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
                e.printStackTrace();
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
                    e.printStackTrace();
                    boolean result = new ErrorAlert(TR.tr("gradeTab.gradeExportWindow.error.title", file.file.getName()), e.getMessage(), true).execute();
                    if(result) return exported;
                }
            }
            
        }
        return exported;
    }
    
    // GENERATORS
    
    public void generateNamesLine(boolean includeGradeScale){
        
        text += TR.tr("gradeTab.gradeExportWindow.csv.titles.parts");
        
        for(GradeRating rating : gradeScale){
            if(rating.isRoot() && rating.outOfTotal > 0){
                if(includeGradeScale) text += ";" + rating.name + " /" + rating.outOfTotal;
                else text += ";" + rating.name + " (/" + rating.outOfTotal + ")";
            }
            text += ";" + rating.name + (includeGradeScale ? " /" + rating.total : "");
        }
        text += "\n";
    }
    
    public void generateGradeScaleLine(){
        
        text += TR.tr("gradeTab.gradeExportWindow.csv.titles.gradeScale");
        
        for(GradeRating rating : gradeScale){
            if(rating.isRoot() && rating.outOfTotal > 0){
                text += ";" + rating.outOfTotal;
            }
            text += ";" + rating.total;
        }
        text += "\n";
        
    }
    
    public void generateMoyLine(){
        
        char x = 'B';
        int startY = pane.settingsAttributeTotalLine.isSelected() ? 4 : 3;
        int endY = startY + files.size() - 1;
        
        text += TR.tr("gradeTab.gradeExportWindow.csv.titles.average");
        
        for(GradeRating rating : gradeScale){
            if(rating.isRoot() && rating.outOfTotal > 0){
                text += ";=" + TR.tr("gradeTab.gradeExportWindow.csv.formulas.average.name").toUpperCase() + "(" + x + startY + ":" + x + endY + ")";
                x++;
            }
            text += ";=" + TR.tr("gradeTab.gradeExportWindow.csv.formulas.average.name").toUpperCase() + "(" + x + startY + ":" + x + endY + ")";
            x++;
        }
        text += "\n";
    }
    
    public void generateStudentLine(ExportFile file){
        
        if(pane.studentNameSimple != null){
            text += pane.studentNameSimple.getText();
        }else{
            text += StringUtils.removeAfterLastRegex(file.file.getName(), ".pdf").replaceAll(Pattern.quote(pane.studentNameReplace.getText()), pane.studentNameBy.getText());
        }
        
        boolean hasOutOfColumn = false;
        for(GradeElement rating : file.grades){
            if(rating.isRoot() && rating.getOutOfTotal() > 0){
                hasOutOfColumn = true;
                text += ";" + (rating.getValue() == -1 ? "" :
                        MainWindow.twoDigFormat.format(rating.getValue() / rating.getTotal() * rating.getOutOfTotal())
                                .replaceAll(",", "."));
            }
            text += ";" + (rating.getValue() == -1 ? "" : rating.getValue());
        }
        text += "\n";
        
        if(pane.settingsWithTxtElements.isSelected()){
            generateCommentsLines(file, hasOutOfColumn);
        }
    }
    
    
    public void generateCommentsLines(ExportFile file, boolean hasOutOfColumn){
        
        text += TR.tr("gradeTab.gradeExportWindow.csv.titles.comments");
        
        if(file.comments.size() >= 1){
            
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
                TextElement element = file.comments.size() > 0 ? file.comments.get(0) : null;
                while(element != null){
                    if(element.getPageNumber() == minPage && element.getRealY() > minY || element.getPageNumber() > minPage){
                        comments.add(element.getText());
                        file.comments.remove(0);
                        element = file.comments.size() > 0 ? file.comments.get(0) : null;
                    }else{
                        element = null;
                    }
                }
            }
            // Adding all others comments
            while(file.comments.size() > 0 && lastGrade != null){
                matches.get(lastGrade).add(file.comments.get(0).getText());
                file.comments.remove(0);
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
                    if(rows.size() <= rowNumber) rows.add(";" + comment);
                    else rows.set(rowNumber, rows.get(rowNumber) + ";" + comment);
                    rowNumber++;
                }
                // Fill rows until rowNumber == 100 (it is needed to add semicolon even for void cells)
                while(rowNumber < 100){
                    if(rows.size() <= rowNumber) rows.add(";");
                    else rows.set(rowNumber, rows.get(rowNumber) + ";");
                    rowNumber++;
                }
            }
            
            // Filling rows
            for(String line : rows){
                text += (hasOutOfColumn ? ";" : "") + line + "\n";
            }
        }else text += "\n";
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
            e.printStackTrace();
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
                    e.printStackTrace();
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
            fileName = pane.fileNamePrefix.getText() + StringUtils.removeAfterLastRegex(source.file.getName(), ".pdf")
                    .replaceAll(Pattern.quote(pane.fileNameReplace.getText()), pane.fileNameBy.getText()) + pane.fileNameSuffix.getText();
        }else{ // other
            fileName = StringUtils.removeAfterLastRegex(pane.fileNameSimple.getText(), ".csv");
        }
        
        File file = new File(filePath + File.separator + fileName + ".csv");
        file.getParentFile().mkdirs();
        
        if(file.exists()){
            AlreadyExistDialogManager.ResultType result = alreadyExistDialogManager.showAndWait(file);
            if(result == AlreadyExistDialogManager.ResultType.SKIP) return true;
            else if(result == AlreadyExistDialogManager.ResultType.STOP) return false;
            else if(result == AlreadyExistDialogManager.ResultType.RENAME)
                file = AlreadyExistDialogManager.rename(file);
        }
        
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
        
        writer.write(text);
        
        writer.flush();
        writer.close();
        
        exported++;
        text = "";
        return true;
    }
    
}
