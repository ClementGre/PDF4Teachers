package fr.clementgre.pdf4teachers.utils.dialog;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import fr.clementgre.pdf4teachers.utils.dialog.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.utils.dialog.alerts.CustomAlert;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.util.Optional;

public class AlreadyExistDialogManager{

    private boolean alwaysSkip = false;
    private boolean alwaysOverwrite = false;
    private boolean alwaysRename = false;
    public CustomAlert alert;

    private ButtonType stopAll;
    private ButtonType skip;
    private ButtonType skipAlways;
    private ButtonType rename;
    private ButtonType renameAlways;
    private ButtonType overwrite;
    private ButtonType overwriteAlways;

    private boolean recursive;
    private int recursions = 0;

    public AlreadyExistDialogManager(boolean recursive){
        this.recursive = recursive;
        alert = new CustomAlert(Alert.AlertType.ERROR, TR.tr("dialog.file.alreadyExist.title"));
    
        stopAll = alert.getButton(TR.tr("dialog.actionError.cancelAll"), ButtonPosition.CLOSE);
        overwrite = alert.getButton(TR.tr("dialog.actionError.overwrite"), ButtonPosition.OTHER_RIGHT);
        
        if(recursive){
            skip = alert.getButton(TR.tr("dialog.actionError.skip"), ButtonPosition.DEFAULT);
            skipAlways = alert.getButton(TR.tr("dialog.actionError.skipAlways"), ButtonPosition.OTHER_RIGHT);
            rename = alert.getButton(TR.tr("dialog.actionError.rename"), ButtonPosition.OTHER_RIGHT);
            renameAlways = alert.getButton(TR.tr("dialog.actionError.renameAlways"), ButtonPosition.OTHER_RIGHT);
            overwriteAlways = alert.getButton(TR.tr("dialog.actionError.overwriteAlways"), ButtonPosition.OTHER_RIGHT);

            alert.getButtonTypes().setAll(overwrite, rename, skip, stopAll);
        }else{
            rename = alert.getButton(TR.tr("dialog.actionError.rename"), ButtonPosition.DEFAULT);
            
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
        recursions++;

        if(alwaysSkip) return ResultType.SKIP;
        if(alwaysOverwrite) return ResultType.ERASE;
        if(alwaysRename) return ResultType.RENAME;

        if(recursions == 2)
            alert.getButtonTypes().setAll(overwriteAlways, overwrite, renameAlways, rename, skipAlways, skip, stopAll);

        alert.setHeaderText(TR.tr("dialog.file.alreadyExist.header", file.getName()));
        alert.setContentText(TR.tr("dialog.file.alreadyExist.details", FilesUtils.getPathReplacingUserHome(file.getParentFile())));

        ButtonType option = alert.getShowAndWait();
        if(option != null){
            if(option == skip){
                return ResultType.SKIP;
            }else if(option == skipAlways){
                alwaysSkip = true;
                return ResultType.SKIP;
            }else if(option == overwrite){
                return ResultType.ERASE;
            }else if(option == overwriteAlways){
                alwaysOverwrite = true;
                return ResultType.ERASE;
            }else if(option == rename){
                return ResultType.RENAME;
            }else if(option == renameAlways){
                alwaysRename = true;
                return ResultType.RENAME;
            }else{
                return ResultType.STOP;
            }
        }
        return ResultType.SKIP;
    }

    public static File rename(File file){
        int counter = 1;
        String extension = "." + FilesUtils.getExtension(file.getName());
        String fileName = StringUtils.removeAfterLastRegex(file.getName(), extension);
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
