/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.io.File;

public class ErrorAlert extends WrongAlert {
    
    
    public ErrorAlert(String header, String error, boolean continueAsk){
        super(Alert.AlertType.ERROR, TR.tr("dialog.error.title"), header == null ? TR.tr("dialog.error.title") : header, TR.tr("dialog.error.details"), continueAsk);
        
        TextArea textArea = new TextArea(error);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label(TR.tr("convertWindow.dialog.error.details")), 0, 0);
        expContent.add(textArea, 0, 1);
        
        if(error.endsWith("(Access is denied.)") || error.endsWith("(Accès refusé)") || error.endsWith("(Permission denied)")){
            setContentText(TR.tr("dialog.error.accessDenied.details", StringUtils.removeAfterLastRegex(error, File.separator)));
        }
        
        getDialogPane().setExpandableContent(expContent);
    }
    
    public static String unableToCopyFileHeader(String toCopyPath, String destPath, boolean simplify){
        if(simplify){
            toCopyPath = FilesUtils.getPathReplacingUserHome(toCopyPath);
            destPath = FilesUtils.getPathReplacingUserHome(destPath);
        }
        return TR.tr("dialog.copyFileError.title", toCopyPath, destPath);
    }
}
