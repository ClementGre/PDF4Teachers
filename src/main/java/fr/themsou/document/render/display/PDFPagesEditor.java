package fr.themsou.document.render.display;

import fr.themsou.document.editions.Edition;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class PDFPagesEditor{

    private PDDocument document;
    private File file;
    public PDFPagesEditor(PDDocument document, File file){
        this.document = document;
        this.file = file;
    }

    public void ascendPage(PageRenderer page){

    }
    public void descendPage(PageRenderer page){

    }
    public void rotateLeftPage(PageRenderer page){
        document.getPage(page.getPage()).setRotation(document.getPage(page.getPage()).getRotation() - 90);
        try{ document.save(file); }catch(IOException e){ e.printStackTrace(); }

        page.updatePosition((int) page.getTranslateY());
        page.updateRender();
    }
    public void rotateRightPage(PageRenderer page){
        document.getPage(page.getPage()).setRotation(document.getPage(page.getPage()).getRotation() + 90);
        try{ document.save(file); }catch(IOException e){ e.printStackTrace(); }

        page.updatePosition((int) page.getTranslateY());
        page.updateRender();
    }
    public void deletePage(PageRenderer page){

        if(MainWindow.mainScreen.document.save() && Edition.isSave()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            Builders.secureAlert(alert);
            alert.setTitle(TR.tr("Confirmation"));
            alert.setHeaderText(TR.tr("Vous allez supprimer la page") + " n°" + (page.getPage()+1) + " " + TR.tr("du document") + "\n" + TR.tr("Le éléments de cette page seront supprimés et les notes seront réinitialisés"));
            alert.setContentText(TR.tr("Cette action est irréversible."));

            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                document.removePage(page.getPage());
                try{ document.save(file); }catch(IOException e){ e.printStackTrace(); }

                MainWindow.mainScreen.document.removePage(page);
                MainWindow.mainScreen.document.edition.save();
            }
        }
    }
    public void newPage(int index){

    }
}
