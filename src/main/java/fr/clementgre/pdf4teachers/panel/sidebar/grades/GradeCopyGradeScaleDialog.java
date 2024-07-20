/*
 * Copyright (c) 2020-2024. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.grades;

import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
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
import java.util.stream.Collectors;

public class GradeCopyGradeScaleDialog {
    
    public ArrayList<GradeRating> ratings = new ArrayList<>();
    
    boolean ignoreAlreadyExist;
    boolean ignoreErase;
    
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
        if(MainWindow.mainScreen.hasDocument(false)) MainWindow.mainScreen.document.save(true);
        File editFile = Edition.getEditFile(MainWindow.mainScreen.document.getFile());
        
        try{
            Element[] elements = Edition.simpleLoad(editFile);
            for(Element element : elements){
                if(element instanceof GradeElement){
                    ratings.add(((GradeElement) element).toGradeRating());
                }
            }
        }catch(Exception e){
            Log.eNotified(e);
        }
    }
    
    // 0 : Copied | 1 : Canceled | 2 : Cancel All
    public int copyToFile(File file, boolean recursive, boolean copyLocations){
        try{
            File editFile = Edition.getEditFile(file);
            
            Element[] elementsArray = Edition.simpleLoad(editFile);
            ArrayList<GradeElement> gradeElements = new ArrayList<>();
            List<Element> otherElements = new ArrayList<>();
            for(Element element : elementsArray){
                if(element instanceof GradeElement) gradeElements.add((GradeElement) element);
                else otherElements.add(element);
            }
            
            if(!gradeElements.isEmpty() && !ignoreAlreadyExist){
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
                }
                if(option == stopAll){
                    return 2;
                }
                if(option == ignoreAll){
                    ignoreAlreadyExist = true;
                }
            }
            
            for(GradeRating rating : ratings){
                
                GradeElement element = rating.getSamePathIn(gradeElements);
                if(element != null){
                    if(copyLocations && rating.isEligibleForAlwaysVisible()){ // alwaysVisible == true, use source grade position
                        otherElements.add(rating.toGradeElement(element.getValue(), true));
                    }else{ // alwaysVisible == false, use destination grade position and value
                        otherElements.add(rating.toGradeElement(element.getValue(), false, element.getRealX(), element.getRealY(), element.getPageNumber()));
                    }
                    gradeElements.remove(element);
                }else{
                    otherElements.add(rating.toGradeElement(-1, copyLocations && rating.isEligibleForAlwaysVisible()));
                }
            }
            
            if(!gradeElements.isEmpty() && !ignoreErase){
                String grades = gradeElements.stream()
                        .map(grade -> "\n" + grade.getParentPath().replaceAll(Pattern.quote("\\"), "/") + "/" + grade.getName() + "  (" +
                                MainWindow.gradesDigFormat.format(grade.getValue()).replaceAll("-1", "?") + "/" +
                                MainWindow.gradesDigFormat.format(grade.getTotal()) + ")")
                        .collect(Collectors.joining());
                
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
                if(option == stopAll) return 2;
                if(option == ignoreAll) ignoreErase = true;
                
            }
            
            ArrayList<Element> sortedElements = GradeElement.sortGradesAlongElements(otherElements);
            Edition.simpleSave(editFile, sortedElements.toArray(new Element[0]));
            return 0;
            
        }catch(Exception e){
            Log.eNotified(e);
            return 1;
        }
    }
}
