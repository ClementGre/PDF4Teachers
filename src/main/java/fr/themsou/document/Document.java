package fr.themsou.document;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.document.render.PDFPagesRender;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class Document {

    private File file;
    public Edition edition;

    public ArrayList<PageRenderer> pages = new ArrayList<>();

    private Image[] rendered;

    public int currentPage = -1;
    public int totalPages = -1;

    public Document(File file){
        this.file = file;

    }

    public boolean renderPDFPages(){

        pages = new ArrayList<>();
        rendered = new PDFPagesRender().render(file, 0, Main.settings.getMaxPages());
        if(rendered != null){

            totalPages = rendered.length;
            this.edition = new Edition(file, this);

            return true;
        }
        return false;
    }

    public void showPages(){

        int i = 0;
        for(Image render : rendered){
            PageRenderer page = new PageRenderer(render, i);
            Main.mainScreen.addPage(page);
            pages.add(page);
            i++;
        }

        pages.get(0).addElement(new TextElement(0, 0, new Font("cmr10", 20),
                "Bonjour les amis", Color.BLACK, Main.mainScreen.document.pages.get(0)));

    }

    public boolean hasRendered(){
        return rendered != null;
    }

    public boolean save(){

        if(Main.settings.isAutoSave()){
            edition.save();
        }else{

            Alert alerte = new Alert(Alert.AlertType.CONFIRMATION);
            alerte.setTitle("Édition non sauvegardée");
            alerte.setHeaderText("L'édition du document n'est pas enregistrée.");
            alerte.setContentText("Voulez-vous l'enregistrer ?");
            ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            alerte.getButtonTypes().setAll(yesButton, noButton, cancelButton);
            Optional<ButtonType> option = alerte.showAndWait();
            if(option.get() == yesButton){
                edition.save(); return true;
            }else{
                return option.get() == noButton;
            }
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
