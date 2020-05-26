package fr.themsou.document.render.display;

import fr.themsou.document.Document;
import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class PDFPagesEditor{

    private PDDocument document;
    private File file;
    public PDFPagesEditor(PDDocument document, File file){
        this.document = document;
        this.file = file;
    }

    public void ascendPage(PageRenderer page){
        PDPage docPage = document.getPage(page.getPage());

        document.removePage(docPage);
        addDocumentPage(page.getPage()-1, docPage);
        try{ document.save(file); }catch(IOException e){ e.printStackTrace(); }

        Document document = MainWindow.mainScreen.document;

        // remove page
        document.pages.remove(page);
        document.pages.add(page.getPage()-1, page);

        // Update pages of all pages
        for(int i = 0 ; i < document.totalPages ; i++) document.pages.get(i).setPage(i);

        // update coordinates of the pages
        document.pages.get(0).updatePosition(30);
        document.updateShowsStatus();

        // update current page
        document.setCurrentPage(page.getPage()-1);
    }
    public void descendPage(PageRenderer page){
        PDPage docPage = document.getPage(page.getPage());

        document.removePage(docPage);
        addDocumentPage(page.getPage()+1, docPage);
        try{ document.save(file); }catch(IOException e){ e.printStackTrace(); }

        Document document = MainWindow.mainScreen.document;

        // remove page
        document.pages.remove(page);
        document.pages.add(page.getPage()+1, page);

        // Update pages of all pages
        for(int i = 0 ; i < document.totalPages ; i++) document.pages.get(i).setPage(i);

        // update coordinates of the pages
        document.pages.get(0).updatePosition(30);
        document.updateShowsStatus();

        // update current page
        document.setCurrentPage(page.getPage()+1);
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

                int pageNumber = page.getPage();

                // remove page elements
                while(page.getElements().size() != 0){
                    if(page.getElements().get(0) instanceof GradeElement){
                        GradeElement grade = (GradeElement) page.getElements().get(0);
                        grade.setValue(-1);
                        grade.switchPage(pageNumber == 0 ? 1 : pageNumber-1);
                    }else{
                        page.getElements().get(0).delete();
                    }
                }
                Document document = MainWindow.mainScreen.document;
                // remove page
                page.remove();
                document.totalPages--;
                document.pages.remove(pageNumber);
                MainWindow.mainScreen.pane.getChildren().remove(page);

                // Update pages of all pages
                for(int i = 0 ; i < document.totalPages ; i++) document.pages.get(i).setPage(i);

                // update coordinates of the pages
                document.pages.get(0).updatePosition(30);
                document.updateShowsStatus();

                // update current page
                document.setCurrentPage(document.totalPages == pageNumber ? pageNumber-1 : pageNumber);

                Edition.setUnsave();
                document.edition.save();
            }
        }
    }
    public void newPage(int index){
        PageRenderer page = new PageRenderer(index);
        PDPage docPage = new PDPage();

        addDocumentPage(index, docPage);
        try{ document.save(file); }catch(IOException e){ e.printStackTrace(); }

        Document document = MainWindow.mainScreen.document;

        // add page
        document.pages.add(index, page);
        MainWindow.mainScreen.addPage(page);
        document.totalPages++;

        // Update pages of all pages
        for(int i = 0 ; i < document.totalPages ; i++) document.pages.get(i).setPage(i);

        // update coordinates of the pages
        document.pages.get(0).updatePosition(30);
        document.updateShowsStatus();

        // update current page
        document.setCurrentPage(index);
    }

    // "UTILS"

    private void addDocumentPage(final int index, final PDPage page) {

        if(index >= document.getNumberOfPages())
            document.addPage(page);
        else{
            ArrayList<PDPage> pages = new ArrayList<>();

            // save pages
            for(int i = 0; i < document.getPages().getCount(); i++){
                if(index == i) pages.add(page);
                pages.add(document.getPage(i));
            }
            // remove pages
            while(document.getPages().getCount() != 0) document.removePage(0);

            // add pages
            for(PDPage pageToAdd : pages) document.addPage(pageToAdd);
        }
    }
}
