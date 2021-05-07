package fr.clementgre.pdf4teachers.components.dialogs.alerts;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ErrorAlert extends WrongAlert{
    
    
    public ErrorAlert(String header, String error, boolean continueAsk){
        super(Alert.AlertType.ERROR, TR.tr("dialog.error.title"), header == null ? TR.tr("dialog.error.title") : header, TR.tr("dialog.error.details"), continueAsk);

        TextArea textArea = new TextArea(error);
        textArea.setEditable(false); textArea.setWrapText(true);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label(TR.tr("convertWindow.dialog.error.details")), 0, 0);
        expContent.add(textArea, 0, 1);
        
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
