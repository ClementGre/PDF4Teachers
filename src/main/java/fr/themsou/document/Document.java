package fr.themsou.document;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PDFPagesRender;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class Document {

    private File file;
    public Edition edition;
    public ArrayList<PageRenderer> pages = new ArrayList<>();

    private Image[] rendered;

    private int currentPage = -1;
    public int totalPages = -1;

    public Thread documentSaver = new Thread(new Runnable() {
        @Override public void run() {

            while(true){
                if(Main.settings.getRegularSaving() != -1){
                    try{
                        Thread.sleep(Main.settings.getRegularSaving() * 60000);
                    }catch(InterruptedException e){ e.printStackTrace(); }

                    if(!Edition.isSave()) Platform.runLater(new Runnable(){public void run(){  edition.save();  }});

                }else{
                    try{
                        Thread.sleep(60000);
                    }catch(InterruptedException e){ e.printStackTrace(); }
                }
            }
        }
    }, "Document AutoSaver");

    public Document(File file){
        this.file = file;

    }

    public boolean renderPDFPages(){

        pages = new ArrayList<>();
        rendered = new PDFPagesRender().render(file);
        if(rendered != null){
            totalPages = rendered.length;
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

    }
    public void loadEdition(){
        this.edition = new Edition(file, this);
        Edition.isSaveProperty().set(true);
        documentSaver.start();
    }
    public boolean hasRendered(){
        return rendered != null;
    }

    public boolean save(){

        if(Edition.isSave()){
            return true;
        }

        if(Main.settings.isAutoSave()){
            edition.save();
        }else{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            alert.setTitle("Édition non sauvegardée");
            alert.setHeaderText("L'édition du document n'est pas enregistrée.");
            alert.setContentText("Voulez-vous l'enregistrer ?");
            ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
            Builders.secureAlert(alert);
            Optional<ButtonType> option = alert.showAndWait();
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
    public int getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        Main.footerBar.repaint();
    }
}