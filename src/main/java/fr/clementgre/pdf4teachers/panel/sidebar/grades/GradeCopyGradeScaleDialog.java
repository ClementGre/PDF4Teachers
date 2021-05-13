package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.CustomAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.OKAlert;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GradeCopyGradeScaleDialog{
    
    public ArrayList<GradeRating> ratings = new ArrayList<>();
    
    boolean ignoreAlreadyExist = false;
    boolean ignoreErase = false;
    
    public void show(){
        
        CustomAlert dialog = new CustomAlert(Alert.AlertType.CONFIRMATION, TR.tr("gradeTab.copyGradeScaleDialog.confirmation.title"), TR.tr("gradeTab.copyGradeScaleDialog.confirmation.header"));
        
        CheckBox copyLocations = new CheckBox(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.copyLocations"));
        dialog.getDialogPane().setContent(copyLocations);
        
        dialog.addCancelButton(ButtonPosition.CLOSE);
        dialog.addButton(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.actions.copySameFolder"), ButtonPosition.DEFAULT);
        dialog.addButton(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.actions.copyAll"), ButtonPosition.OTHER_RIGHT);
        
        ButtonPosition option = dialog.getShowAndWaitGetButtonPosition(ButtonPosition.CLOSE);
        int copiedEditions = 0;
        if(option == ButtonPosition.DEFAULT){
            prepareCopyEditions();
            boolean recursive = MainWindow.filesTab.getOpenedFiles().size() != 1;
            for(File file : MainWindow.filesTab.getOpenedFiles()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                if(MainWindow.mainScreen.document.getFile().getParent().equals(file.getParent())){
                    int result = copyToFile(file, recursive, copyLocations.isSelected());
                    if(result == 0) copiedEditions++;
                    else if(result == 2) break;
                }
            }
        }else if(option == ButtonPosition.OTHER_RIGHT){
            prepareCopyEditions();
            boolean recursive = MainWindow.filesTab.getOpenedFiles().size() != 1;
            for(File file : MainWindow.filesTab.getOpenedFiles()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                int result = copyToFile(file, recursive, copyLocations.isSelected());
                if(result == 0) copiedEditions++;
                else if(result == 2) break;
            }
        }else return;
        
        new OKAlert(TR.tr("gradeTab.copyGradeScaleDialog.completed.title"),
                TR.tr("gradeTab.copyGradeScaleDialog.completed.header"), "(" + TR.tr("gradeTab.copyGradeScaleDialog.completed.details", copiedEditions) + ")").show();
        
        MainWindow.filesTab.refresh();
        
    }
    
    public void prepareCopyEditions(){
        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save();
        File editFile = Edition.getEditFile(MainWindow.mainScreen.document.getFile());
        
        try{
            Element[] elements = Edition.simpleLoad(editFile);
            for(Element element : elements){
                if(element instanceof GradeElement){
                    ratings.add(((GradeElement) element).toGradeRating());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // 0 : Copied | 1 : Canceled | 2 : Cancel All
    public int copyToFile(File file, boolean recursive, boolean copyLocations){
        try{
            File editFile = Edition.getEditFile(file);
            
            Element[] elementsArray = Edition.simpleLoad(editFile);
            List<GradeElement> gradeElements = new ArrayList<>();
            List<Element> otherElements = new ArrayList<>();
            for(Element element : elementsArray){
                if(element instanceof GradeElement) gradeElements.add((GradeElement) element);
                else otherElements.add(element);
            }
            
            if(gradeElements.size() >= 1 && !ignoreAlreadyExist){
                CustomAlert alert = new CustomAlert(Alert.AlertType.WARNING, TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScale.title"),
                        TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScale.header", file.getName()), TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScale.details"));
    
                ButtonType ignore = alert.getButton(TR.tr("dialog.actionError.continue"), ButtonPosition.DEFAULT);
                ButtonType ignoreAll = alert.getButton(TR.tr("dialog.actionError.continueAlways"), ButtonPosition.OTHER_RIGHT);
                ButtonType stop = alert.getButton(TR.tr("dialog.actionError.skip"), ButtonPosition.CLOSE);
                ButtonType stopAll = alert.getButton(TR.tr("dialog.actionError.stopAll"), ButtonPosition.CLOSE);
    
                if(recursive) alert.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);
                else alert.getButtonTypes().setAll(ignore, stop);
    
                ButtonType option = alert.getShowAndWait();
                if(option == stop){
                    return 1;
                }else if(option == stopAll){
                    return 2;
                }else if(option == ignoreAll){
                    ignoreAlreadyExist = true;
                }
            }
            
            for(GradeRating rating : ratings){
                rating.alwaysVisible = copyLocations;
                
                GradeElement element = rating.getSamePathIn((ArrayList<GradeElement>) gradeElements);
                if(element != null){
                    if(copyLocations){
                        otherElements.add(rating.toGradeElement(-1));
                    }else{
                        otherElements.add(rating.toGradeElement(element.getValue(), element.getRealX(), element.getRealY(), element.getPageNumber()));
                    }
                    gradeElements.remove(element);
                }else{
                    otherElements.add(rating.toGradeElement(-1));
                }
            }
            
            if(gradeElements.size() >= 1 && !ignoreErase){
                String grades = "";
                for(GradeElement grade : gradeElements){
                    grades += "\n" + grade.getParentPath().replaceAll(Pattern.quote("\\"), "/") + "/" + grade.getName() + "  (" + MainWindow.gradesDigFormat.format(grade.getValue()).replaceAll("-1", "?") + "/" + MainWindow.gradesDigFormat.format(grade.getTotal()) + ")";
                }
    
                CustomAlert alert = new CustomAlert(Alert.AlertType.WARNING, TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScaleErase.title"),
                        TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScaleErase.header", grades, file.getName()));
                
                ButtonType ignore = alert.getButton(TR.tr("dialog.actionError.overwrite"), ButtonPosition.DEFAULT);
                ButtonType ignoreAll = alert.getButton(TR.tr("dialog.actionError.overwriteAlways"), ButtonPosition.OTHER_RIGHT);
                ButtonType stop = alert.getButton(TR.tr("dialog.actionError.skip"), ButtonPosition.CLOSE);
                ButtonType stopAll = alert.getButton(TR.tr("dialog.actionError.stopAll"), ButtonPosition.CLOSE);
    
                if(recursive) alert.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);
                else alert.getButtonTypes().setAll(ignore, stop);
    
                ButtonType option = alert.getShowAndWait();
                if(option == stop) return 1;
                else if(option == stopAll) return 2;
                else if(option == ignoreAll) ignoreErase = true;
                
            }
            
            otherElements = GradeElement.sortGradesBetweenNormalElements(otherElements);
            
            Edition.simpleSave(editFile, otherElements.toArray(new Element[0]));
            return 0;
            
        }catch(Exception e){
            e.printStackTrace();
            return 1;
        }
    }
}
