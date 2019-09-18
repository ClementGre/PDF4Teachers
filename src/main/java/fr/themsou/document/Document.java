package fr.themsou.document;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PDFPagesRender;

import java.awt.*;
import java.io.File;

public class Document {

    private File file;
    public Edition edition;

    public Image[] rendered;

    public int currentPage = -1;
    public int totalPages = -1;

    public Document(File file){
        this.file = file;

    }

    public boolean renderPDFPages(){

        rendered = new PDFPagesRender().render(file, 0, 4);
        if(rendered != null){

            totalPages = rendered.length;
            this.edition = new Edition(file, this);
            return true;
        }
        return false;
    }

    public void save(){
        edition.save();
    }
    public String getFileName(){
        return file.getName();
    }
    public File getFile(){
        return file;
    }
}
