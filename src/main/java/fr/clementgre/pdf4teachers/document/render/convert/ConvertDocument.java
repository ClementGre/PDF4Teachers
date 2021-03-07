package fr.clementgre.pdf4teachers.document.render.convert;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.StringUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ConvertDocument {

    public ConvertWindow convertWindow;

    public ConvertDocument(){
        convertWindow = new ConvertWindow(null, (convertedFiles) -> {

            boolean eraseAll = false;
            int converted = 0;
            for(ConvertedFile file : convertedFiles){

                if(file.file.exists() && !eraseAll){
                    Alert alert = DialogBuilder.getAlert(Alert.AlertType.WARNING, TR.trO("Fichier déjà existant"));
                    alert.setHeaderText(TR.trO("Le fichier de destination") + " \"" + file.file.getName() + "\" " +TR.trO("existe déjà"));
                    alert.setContentText(TR.trO("Voulez-vous l'écraser ?"));

                    ButtonType yesButton = new ButtonType(TR.trO("Écraser"), ButtonBar.ButtonData.YES);
                    ButtonType yesAlwaysButton = new ButtonType(TR.trO("Toujours écraser"), ButtonBar.ButtonData.YES);
                    ButtonType renameButton = new ButtonType(TR.trO("Renommer"), ButtonBar.ButtonData.OTHER);
                    ButtonType cancelButton = new ButtonType(TR.trO("Sauter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType cancelAllButton = new ButtonType(TR.trO("Tout Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(yesButton, yesAlwaysButton, renameButton, cancelButton, cancelAllButton);

                    Optional<ButtonType> option = alert.showAndWait();
                    if(option.get() == cancelAllButton){
                        try{ file.document.close(); }catch(IOException e){ e.printStackTrace(); }
                        break;
                    }else if(option.get() == cancelButton){
                        try{ file.document.close(); }catch(IOException e){ e.printStackTrace(); }
                        continue;
                    }else if(option.get() == yesAlwaysButton) eraseAll = true;
                    else if(option.get() == renameButton){
                        int k = 1; String tmpUri = file.file.getAbsolutePath();
                        while(new File(tmpUri).exists()){
                            tmpUri = StringUtils.removeAfterLastRegex(file.file.getAbsolutePath(), ".pdf") + " (" + k + ").pdf";
                            k++;
                        }
                        file.file = new File(tmpUri);
                    }
                }

                if(MainWindow.mainScreen.hasDocument(false)){
                    if(MainWindow.mainScreen.document.getFile().getAbsolutePath().equals(file.file.getAbsolutePath())){
                        if(!MainWindow.mainScreen.closeFile(true)) continue;
                    }
                }

                try{
                    file.document.save(file.file);
                    file.document.close();
                }catch(IOException e){ e.printStackTrace(); }


                MainWindow.filesTab.openFiles(new File[]{file.file});
                converted++;
            }

            Alert alert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.trO("Conversion terminée"));

            if(converted == 0) alert.setHeaderText(TR.trO("Aucun document n'a été converti !"));
            else if(converted == 1) alert.setHeaderText(TR.trO("Le document a bien été créé !"));
            else alert.setHeaderText(converted + " " + TR.trO("documents ont été créées !"));


            if(converted > 1) alert.setContentText(TR.trO("Les documents ont été ouverts dans le panneau latéral"));
            else if(converted != 0) alert.setContentText(TR.trO("Le document a été ouvert dans le panneau latéral"));

            alert.show();
        });
    }

}
