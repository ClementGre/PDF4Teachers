package fr.themsou.document.render.convert;


import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.control.Alert;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;

public class ConvertDocument {

    public ConvertDocument(){
        new ConvertWindow(null, (convertedFiles) -> {
            for(ConvertedFile file : convertedFiles){

                PDDocument doc = new PDDocument();
                for(PDPage page : file.pages){
                    doc.addPage(page);
                }

                try{
                    doc.save(file.file);
                }catch(IOException e){ e.printStackTrace(); }

                MainWindow.lbFilesTab.openFiles(new File[]{file.file});

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                Builders.secureAlert(alert);
                alert.setTitle(TR.tr("Conversion terminée"));

                if(convertedFiles.size() == 0) alert.setHeaderText(TR.tr("Aucun document n'a été convertis !"));
                else if(convertedFiles.size() == 1) alert.setHeaderText(TR.tr("Le document a bien été créé !"));
                else alert.setHeaderText(convertedFiles.size() + " " + TR.tr("documents ont été créées !"));


                if(convertedFiles.size() > 1) alert.setContentText(TR.tr("Les documents ont été ouverts dans le panneau latéral"));
                else if(convertedFiles.size() != 0) alert.setContentText(TR.tr("Le document a été ouvert dans le panneau latéral"));

                alert.show();
            }
        });
    }

}
