package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class GradeCopyGradeScaleDialog{
    
    public ArrayList<GradeRating> ratings = new ArrayList<>();
    
    boolean ignoreAlreadyExist = false;
    boolean ignoreErase = false;
    
    public void show(){
        
        Alert dialog = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("gradeTab.copyGradeScaleDialog.confirmation.title"));
        dialog.setHeaderText(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.header"));
        
        CheckBox copyLocations = new CheckBox(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.copyLocations"));
        dialog.getDialogPane().setContent(copyLocations);
        
        ButtonType cancel = new ButtonType(TR.tr("actions.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType yes = new ButtonType(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.actions.copySameFolder"), ButtonBar.ButtonData.OK_DONE);
        ButtonType yesAll = new ButtonType(TR.tr("gradeTab.copyGradeScaleDialog.confirmation.actions.copyAll"), ButtonBar.ButtonData.OTHER);
        dialog.getButtonTypes().setAll(yesAll, yes, cancel);
        
        Optional<ButtonType> option = dialog.showAndWait();
        int copiedEditions = 0;
        if(option.get() == yes){
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
        }else if(option.get() == yesAll){
            prepareCopyEditions();
            boolean recursive = MainWindow.filesTab.getOpenedFiles().size() != 1;
            for(File file : MainWindow.filesTab.getOpenedFiles()){
                if(MainWindow.mainScreen.document.getFile().equals(file)) continue;
                int result = copyToFile(file, recursive, copyLocations.isSelected());
                if(result == 0) copiedEditions++;
                else if(result == 2) break;
            }
        }else return;
        
        Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("gradeTab.copyGradeScaleDialog.completed.title"));
        alert.setHeaderText(TR.tr("gradeTab.copyGradeScaleDialog.completed.header"));
        alert.setContentText("(" + TR.tr("gradeTab.copyGradeScaleDialog.completed.details", copiedEditions) + ")");
        alert.show();
        
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
                Alert dialog = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScale.title"));
                dialog.setHeaderText(TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScale.header", file.getName()));
                dialog.setContentText(TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScale.details"));
                
                ButtonType ignore = new ButtonType(TR.tr("dialog.actionError.continue"), ButtonBar.ButtonData.OK_DONE);
                ButtonType ignoreAll = new ButtonType(TR.tr("dialog.actionError.continueAlways"), ButtonBar.ButtonData.OK_DONE);
                ButtonType stop = new ButtonType(TR.tr("dialog.actionError.skip"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType stopAll = new ButtonType(TR.tr("dialog.actionError.stopAll"), ButtonBar.ButtonData.CANCEL_CLOSE);
                
                if(recursive) dialog.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);
                else dialog.getButtonTypes().setAll(ignore, stopAll);
                
                Optional<ButtonType> option = dialog.showAndWait();
                if(option.get() == stop){
                    return 1;
                }else if(option.get() == stopAll){
                    return 2;
                }else if(option.get() == ignoreAll){
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
                    grades += "\n" + grade.getParentPath().replaceAll(Pattern.quote("\\"), "/") + "/" + grade.getName() + "  (" + MainWindow.format.format(grade.getValue()).replaceAll("-1", "?") + "/" + MainWindow.format.format(grade.getTotal()) + ")";
                }
                
                Alert dialog = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScaleErase.title"));
                dialog.setHeaderText(TR.tr("gradeTab.copyGradeScaleDialog.error.alreadyGradeScaleErase.header", grades, file.getName()));
                
                ButtonType ignore = new ButtonType(TR.tr("dialog.actionError.overwrite"), ButtonBar.ButtonData.OK_DONE);
                ButtonType ignoreAll = new ButtonType(TR.tr("dialog.actionError.overwriteAlways"), ButtonBar.ButtonData.OK_DONE);
                ButtonType stop = new ButtonType(TR.tr("dialog.actionError.skip"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType stopAll = new ButtonType(TR.tr("dialog.actionError.stopAll"), ButtonBar.ButtonData.CANCEL_CLOSE);
                
                if(recursive) dialog.getButtonTypes().setAll(ignore, ignoreAll, stop, stopAll);
                else dialog.getButtonTypes().setAll(ignore, stop);
                
                Optional<ButtonType> option = dialog.showAndWait();
                if(option.get() == stop){
                    return 1;
                }else if(option.get() == stopAll){
                    return 2;
                }else if(option.get() == ignoreAll){
                    ignoreErase = true;
                }
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
