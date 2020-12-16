package fr.clementgre.pdf4teachers.utils.dialog;

import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.util.Optional;

public class AlreadyExistDialog{

    private boolean alwaysSkip = false;
    private boolean alwaysOverwrite = false;
    private boolean alwaysRename = false;
    public Alert alert;

    private ButtonType stopAll;
    private ButtonType skip;
    private ButtonType skipAlways;
    private ButtonType rename = new ButtonType(TR.tr("Renommer"), ButtonBar.ButtonData.YES);
    private ButtonType renameAlways;
    private ButtonType overwrite = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.OK_DONE);
    private ButtonType overwriteAlways;

    private boolean recursive;
    private int recursions = 0;

    public AlreadyExistDialog(boolean recursive){
        this.recursive = recursive;
        alert = DialogBuilder.getAlert(Alert.AlertType.ERROR, TR.tr("Fichier déjà existant"));

        if(recursive){
            stopAll = new ButtonType(TR.tr("Tout annuler"), ButtonBar.ButtonData.OK_DONE);
            skip = new ButtonType(TR.tr("Passer"), ButtonBar.ButtonData.YES);
            skipAlways = new ButtonType(TR.tr("Toujours Passer"), ButtonBar.ButtonData.YES);
            renameAlways = new ButtonType(TR.tr("Toujours Renommer"), ButtonBar.ButtonData.YES);
            overwriteAlways = new ButtonType(TR.tr("Toujours Écraser"), ButtonBar.ButtonData.OK_DONE);

            alert.getButtonTypes().setAll(overwrite, rename, skip, stopAll);
        }else alert.getButtonTypes().setAll(overwrite, rename, ButtonType.CANCEL);
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

        if(recursions == 2) alert.getButtonTypes().setAll(overwrite, rename, renameAlways, skip, skipAlways, overwriteAlways, stopAll);

        alert.setHeaderText(file.getName() + " " + TR.tr("existe déjà"));
        alert.setContentText(TR.tr("Situé dans le dossier : ") + " " + FilesUtils.getPathReplacingUserHome(file.getParentFile()));

        Optional<ButtonType> option = alert.showAndWait();
        if(option.isPresent()){
            if(option.get() == skip){
                return ResultType.SKIP;
            }else if(option.get() == skipAlways){
                alwaysSkip = true;
                return ResultType.SKIP;
            }else if(option.get() == overwrite){
                return ResultType.ERASE;
            }else if(option.get() == overwriteAlways){
                alwaysOverwrite = true;
                return ResultType.ERASE;
            }else if(option.get() == rename){
                return ResultType.RENAME;
            }else if(option.get() == renameAlways){
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
