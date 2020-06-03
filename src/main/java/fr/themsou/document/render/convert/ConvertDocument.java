package fr.themsou.document.render.convert;

import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;

public class ConvertDocument {

    public ConvertDocument(){
        new ConvertWindow(null, (convertedFiles) -> {

            int converted = 0;
            for(ConvertedFile file : convertedFiles){

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

            if(converted == 0) alert.setHeaderText(TR.tr("Aucun document n'a été convertis !"));
            else if(converted == 1) alert.setHeaderText(TR.tr("Le document a bien été créé !"));
            else alert.setHeaderText(converted + " " + TR.tr("documents ont été créées !"));


            if(converted > 1) alert.setContentText(TR.tr("Les documents ont été ouverts dans le panneau latéral"));
            else if(converted != 0) alert.setContentText(TR.tr("Le document a été ouvert dans le panneau latéral"));

            alert.show();
        });
    }

}
