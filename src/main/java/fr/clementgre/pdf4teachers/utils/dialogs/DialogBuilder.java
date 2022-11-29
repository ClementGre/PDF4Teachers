/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.utils.dialogs;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.CustomAlert;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;

public class DialogBuilder {
    
    /* OPEN DIR ALERT */
    
    public static void showAlertWithOpenDirButton(String title, String header, String details, File dirToBrowse){
        showAlertWithOpenDirButton(title, header, details, dirToBrowse.getAbsolutePath());
    }
    public static void showAlertWithOpenDirButton(String title, String header, String details, String pathToBrowse){
        CustomAlert alert = new CustomAlert(Alert.AlertType.INFORMATION, title, header, details);
        alert.addOKButton(ButtonPosition.CLOSE);
        alert.addButton(TR.tr("dialog.file.openFolderButton"), ButtonPosition.DEFAULT);
        
        if(alert.getShowAndWaitIsDefaultButton()) {
            PlatformUtils.openFile(pathToBrowse);
        }
    }
    
    public static void showAlertWithOpenDirOrFileButton(String title, String header, String details, String pathToDir, String pathToFile){
        CustomAlert alert = new CustomAlert(Alert.AlertType.INFORMATION, title, header, details);
        alert.addOKButton(ButtonPosition.CLOSE);
        ButtonType openFolderButton = alert.addButton(TR.tr("dialog.file.openFolderButton"), ButtonPosition.DEFAULT);
        ButtonType openFileButton = alert.addButton(TR.tr("dialog.file.openFileButton"), ButtonPosition.OTHER_RIGHT);
    
        ButtonType buttonType = alert.getShowAndWait();
        if(buttonType == openFolderButton) {
            PlatformUtils.openFile(pathToDir);
        } else if(buttonType == openFileButton) {
            PlatformUtils.openFile(pathToFile);
        }
    }
}
