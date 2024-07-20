/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.CustomAlert;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;

import java.io.File;

public class AlreadyExistDialogManager{

    private ResultType memorizedResult;
    public CustomAlert alert;

    private final ButtonType stopAll;
    private ButtonType skip;
    private final ButtonType rename;
    private final ButtonType overwrite;
    private final CheckBox memorize = new CheckBox(TR.tr("dialog.actionError.memorize"));
    
    private final boolean recursive;
    public AlreadyExistDialogManager(boolean recursive){
        this.recursive = recursive;
        alert = new CustomAlert(Alert.AlertType.ERROR, TR.tr("dialog.file.alreadyExist.title"));
    
        stopAll = alert.getButton(TR.tr("dialog.actionError.cancelAll"), ButtonPosition.CLOSE);
        overwrite = alert.getButton(TR.tr("dialog.actionError.overwrite"), ButtonPosition.OTHER_RIGHT);
        
        if(recursive){
            rename = alert.getButton(TR.tr("actions.rename"), ButtonPosition.OTHER_RIGHT);
            skip = alert.getButton(TR.tr("dialog.actionError.skip"), ButtonPosition.DEFAULT);
    
            memorize.setSelected(true);
            alert.getDialogPane().setContent(memorize);
            
            alert.getButtonTypes().setAll(overwrite, rename, skip, stopAll);
        }else{
            rename = alert.getButton(TR.tr("actions.rename"), ButtonPosition.DEFAULT);
            
            alert.getButtonTypes().setAll(overwrite, rename, stopAll);
        }
    }

    public enum ResultType{
        ERASE,
        SKIP,
        RENAME,
        STOP
    }

    public ResultType showAndWait(File file){
        if(memorizedResult != null) return memorizedResult;

        if(recursive){
            alert.setHeaderText(TR.tr("dialog.file.alreadyExist.header", file.getName()) + "\n"
                    + TR.tr("dialog.file.alreadyExist.details", FilesUtils.getPathReplacingUserHome(file.getParentFile().toPath())));
        }else{
            alert.setHeaderText(TR.tr("dialog.file.alreadyExist.header", file.getName()));
            alert.setContentText(TR.tr("dialog.file.alreadyExist.details", FilesUtils.getPathReplacingUserHome(file.getParentFile().toPath())));
        }


        ButtonType option = alert.getShowAndWait();
        if(option != null){
            if(option == skip){
                if(memorize.isSelected()) memorizedResult = ResultType.SKIP;
                return ResultType.SKIP;
            }
            if(option == overwrite){
                if(memorize.isSelected()) memorizedResult = ResultType.ERASE;
                return ResultType.ERASE;
            }
            if(option == rename){
                if(memorize.isSelected()) memorizedResult = ResultType.RENAME;
                return ResultType.RENAME;
            }
            return ResultType.STOP;
        }
        return ResultType.SKIP;
    }

    public static File rename(File file){
        int counter = 1;
        String extension = "." + FilesUtils.getExtension(file.getName());
        String fileName = StringUtils.removeAfterLastOccurrence(file.getName(), extension);
        while(file.exists()){
            file = new File(file.getParentFile().getAbsolutePath() + File.separator + fileName + " (" + counter + ")" + extension);
            counter++;
        }
        return file;
    }

    public boolean isRecursive(){
        return recursive;
    }


}
