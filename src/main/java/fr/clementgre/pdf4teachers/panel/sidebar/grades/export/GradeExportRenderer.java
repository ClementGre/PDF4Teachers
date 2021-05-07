package fr.clementgre.pdf4teachers.panel.sidebar.grades.export;

import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.grades.GradeRating;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.components.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.components.dialogs.alerts.ErrorAlert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class GradeExportRenderer{
    
    String text = "";
    
    public ArrayList<GradeRating> gradeScale;
    ArrayList<ExportFile> files = new ArrayList<>();
    int exportTier;
    
    int exported = 0;
    boolean mkdirs = true;
    boolean erase = false;
    
    GradeExportWindow.ExportPane pane;
    AlreadyExistDialogManager alreadyExistDialogManager;
    
    public GradeExportRenderer(GradeExportWindow.ExportPane pane){
        this.pane = pane;
        this.exportTier = (int) pane.settingsTiersExportSlider.getValue();
        
        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
        
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
            text += ";" + rating.name + (includeGradeScale ? " /" + rating.total : "");
        }
        text += "\n";
    }
    
    public void generateGradeScaleLine(){
        
        text += TR.tr("gradeTab.gradeExportWindow.csv.titles.gradeScale");
        
        for(GradeRating rating : gradeScale){
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
        
        for(GradeElement grade : file.grades){
            text += ";" + (grade.getValue() == -1 ? "" : grade.getValue());
        }
        text += "\n";
        
        if(pane.settingsWithTxtElements.isSelected()){
            generateCommentsLines(file);
        }
    }
    
    public void generateCommentsLines(ExportFile file){
        
        text += TR.tr("gradeTab.gradeExportWindow.csv.titles.comments");
        
        if(file.comments.size() >= 1){
            ArrayList<String> lines = new ArrayList<>();
            
            file.comments.sort((element1, element2) ->
                    (element2.getPageNumber() - 9999 + "" + (element2.getRealY() - 9999) + "" + (element2.getRealX() - 9999))
                            .compareToIgnoreCase(element1.getPageNumber() - 9999 + "" + (element1.getRealY() - 9999) + "" + (element1.getRealX() - 9999)));
            
            for(int i = 1; i < file.grades.size(); i++){
                GradeElement grade = file.grades.get(i);
                int maxPage = grade.getPageNumber();
                int maxY = grade.getRealY();
                
                TextElement element = file.comments.size() > 0 ? file.comments.get(0) : null;
                int k = -1;
                while(element != null){
                    
                    if(element.getPageNumber() == maxPage && element.getRealY() < maxY || element.getPageNumber() < maxPage){
                        k++;
                        if(lines.size() > k){
                            lines.set(k, lines.get(k) + ";" + element.getText().replaceAll(Pattern.quote("\n"), " "));
                        }else{
                            lines.add(";" + element.getText().replaceAll(Pattern.quote("\n"), ""));
                        }
                        
                        file.comments.remove(0);
                        element = file.comments.size() > 0 ? file.comments.get(0) : null;
                    }else{
                        element = null;
                    }
                }
                for(k++; k < 20; k++){
                    if(lines.size() > k){
                        lines.set(k, lines.get(k) + ";");
                    }else{
                        lines.add(";");
                    }
                }
            }
            int k = 0;
            for(TextElement element : file.comments){
                if(lines.size() > k){
                    lines.set(k, lines.get(k) + ";" + element.getText().replaceAll(Pattern.quote("\n"), " "));
                }else{
                    lines.add(";" + element.getText().replaceAll(Pattern.quote("\n"), ""));
                }
                k++;
            }
            
            for(String line : lines){
                text += line + "\n";
            }
        }else text += "\n";
    }
    
    // OTHERS
    
    public boolean getFiles(){
        
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
            else if(result == AlreadyExistDialogManager.ResultType.RENAME) file = AlreadyExistDialogManager.rename(file);
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
