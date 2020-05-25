package fr.themsou.document.render.display;

import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

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
        try{
            document.save(file);
        }catch(IOException e){
            e.printStackTrace();
        }
        page.updatePosition((int) page.getTranslateY());
        page.updateRender();
    }
    public void rotateRightPage(PageRenderer page){
        document.getPage(page.getPage()).setRotation(document.getPage(page.getPage()).getRotation() + 90);
        try{
            document.save(file);
        }catch(IOException e){
            e.printStackTrace();
        }
        page.updatePosition((int) page.getTranslateY());
        page.updateRender();
    }
    public void deletePage(PageRenderer page){

    }
    public void newPage(int index){

    }
}
