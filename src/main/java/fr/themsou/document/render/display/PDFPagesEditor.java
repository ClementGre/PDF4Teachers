package fr.themsou.document.render.display;

import fr.themsou.document.Document;
import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.document.render.convert.ConvertWindow;
import fr.themsou.document.render.convert.ConvertedFile;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
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
            Alert alert = Builders.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Confirmation"));
            alert.setHeaderText(TR.tr("Vous allez supprimer la page") + " n°" + (page.getPage()+1) + " " + TR.tr("du document") + "\n" + TR.tr("Les éléments de cette page seront supprimés et les notes seront réinitialisées"));
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
    public void newBlankPage(int originalPage, int index){
        PageRenderer page = new PageRenderer(index);
        PDPage docPage = new PDPage(MainWindow.mainScreen.document.pdfPagesRender.getPageSize(originalPage));

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
    public void newConvertPage(int originalPage, int index) {

        Document document = MainWindow.mainScreen.document;

        new ConvertWindow(MainWindow.mainScreen.document.pdfPagesRender.getPageSize(originalPage), (convertedFiles) -> {
            if(convertedFiles.size() == 0) return;
            ConvertedFile file = convertedFiles.get(0);

            PDFMergerUtility merger = new PDFMergerUtility();

            int addedPages = file.document.getNumberOfPages();
            try{
                merger.appendDocument(this.document, file.document);
                merger.mergeDocuments();
            }catch(IOException e){ e.printStackTrace(); }

            for(int j = 0; j < addedPages; j++){
                PageRenderer page = new PageRenderer(index);

                moveDocumentPage(this.document.getNumberOfPages()-1, index);

                try{ this.document.save(this.file); }catch(IOException e){ e.printStackTrace(); }

                // add page
                document.pages.add(index, page);
                MainWindow.mainScreen.addPage(page);
                document.totalPages++;

                // Update pages of all pages
                for(int k = 0 ; k < document.totalPages ; k++) document.pages.get(k).setPage(k);
            }

            try{ file.document.close(); }catch(IOException e){ e.printStackTrace(); }

            // update coordinates of the pages
            document.pages.get(0).updatePosition(30);
            document.updateShowsStatus();

            // update current page
            document.setCurrentPage(index);
        });
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
    private void moveDocumentPage(final int from, final int to) {

        ArrayList<PDPage> pages = new ArrayList<>();

        // save non-from pages
        for(int i = 0; i < document.getPages().getCount(); i++){
            if(i != from) pages.add(document.getPage(i));
        }
        // save from page
        pages.add(to, document.getPages().get(from));

        // remove pages
        while(document.getPages().getCount() != 0) document.removePage(0);

        // add pages
        for(PDPage pageToAdd : pages) document.addPage(pageToAdd);
    }


}
