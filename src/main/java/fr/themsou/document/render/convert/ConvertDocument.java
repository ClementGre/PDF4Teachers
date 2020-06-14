package fr.themsou.document.render.convert;

import fr.themsou.utils.Builders;
import fr.themsou.utils.StringUtils;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
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
                    Alert alert = Builders.getAlert(Alert.AlertType.WARNING, TR.tr("Fichier déjà existant"));
                    alert.setHeaderText(TR.tr("Le fichier de destination") + " \"" + file.file.getName() + "\" " +TR.tr("existe déjà"));
                    alert.setContentText(TR.tr("Voulez-vous l'écraser ?"));

                    ButtonType yesButton = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.YES);
                    ButtonType yesAlwaysButton = new ButtonType(TR.tr("Toujours écraser"), ButtonBar.ButtonData.YES);
                    ButtonType renameButton = new ButtonType(TR.tr("Renommer"), ButtonBar.ButtonData.OTHER);
                    ButtonType cancelButton = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType cancelAllButton = new ButtonType(TR.tr("Tout Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
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
                            tmpUri = StringUtils.removeAfterLastRejex(file.file.getAbsolutePath(), ".pdf") + " (" + k + ").pdf";
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


                MainWindow.lbFilesTab.openFiles(new File[]{file.file});
                converted++;
            }

            Alert alert = Builders.getAlert(Alert.AlertType.INFORMATION, TR.tr("Conversion terminée"));

            if(converted == 0) alert.setHeaderText(TR.tr("Aucun document n'a été converti !"));
            else if(converted == 1) alert.setHeaderText(TR.tr("Le document a bien été créé !"));
            else alert.setHeaderText(converted + " " + TR.tr("documents ont été créées !"));


            if(converted > 1) alert.setContentText(TR.tr("Les documents ont été ouverts dans le panneau latéral"));
            else if(converted != 0) alert.setContentText(TR.tr("Le document a été ouvert dans le panneau latéral"));

            alert.show();
        });
    }

}
