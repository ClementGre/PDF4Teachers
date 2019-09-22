package fr.themsou.document;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PDFPagesRender;
import fr.themsou.main.Main;

import javax.swing.*;
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

    public boolean save(){

        if(Main.settings.isAutoSave()){
            edition.save();
        }else{
            int i = JOptionPane.showConfirmDialog(null, "L'édition du document n'est pas enregistrée, voulez-vous l'enregistrer ?");
            System.out.println(i);
            // Annuler
            if(i == 0){ // YES
                edition.save();
            }else return i == 1;
        }
        return true;
    }
    public String getFileName(){
        return file.getName();
    }
    public File getFile(){
        return file;
    }
}
