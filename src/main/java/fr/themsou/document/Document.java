package fr.themsou.document;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.PDFPagesRender;
import fr.themsou.document.render.PageRenderer;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Document {

    private File file;
    public Edition edition;
    public ArrayList<PageRenderer> pages = new ArrayList<>();

    private int currentPage = -1;
    public int totalPages;

    public PDFPagesRender pdfPagesRender;

    public Thread documentSaver = new Thread(new Runnable() {
        @Override public void run() {

            while(true){
                if(Main.settings.getRegularSaving() != -1){
                    try{
                        Thread.sleep(Main.settings.getRegularSaving() * 60000);
                    }catch(InterruptedException e){ e.printStackTrace(); }

                    if(!Edition.isSave()) Platform.runLater(() -> edition.save());

                }else{
                    try{
                        Thread.sleep(60000);
                    }catch(InterruptedException e){ e.printStackTrace(); }
                }
            }
        }
    }, "Document AutoSaver");

    public Document(File file) throws IOException {
        this.file = file;

        pdfPagesRender = new PDFPagesRender(file);
        totalPages = pdfPagesRender.getNumberOfPages();
    }
    public void showPages(){

        for(int i = 0 ; i < totalPages ; i++){
            PageRenderer page = new PageRenderer(i);
            Main.mainScreen.addPage(page);
            pages.add(page);
        }
        Main.mainScreen.finalizePages();
        Main.settings.setOpenedFile(file);

        updateShowsStatus();
    }
    public void updateShowsStatus(){

        for(PageRenderer page : pages){
            page.updateShowStatus();
        }
    }
    public void loadEdition(){
        this.edition = new Edition(file, this);
        Edition.isSaveProperty().set(true);
        if(!documentSaver.isAlive()) documentSaver.start();
    }
    public void close(){
        pdfPagesRender.close();
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
            alert.setTitle(TR.tr("Édition non sauvegardée"));
            alert.setHeaderText(TR.tr("L'édition du document n'est pas enregistrée."));
            alert.setContentText(TR.tr("Voulez-vous l'enregistrer ?"));
            ButtonType yesButton = new ButtonType(TR.tr("Oui"), ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(TR.tr("Non"), ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
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