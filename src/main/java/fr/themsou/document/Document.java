package fr.themsou.document;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.render.display.PDFPagesRender;
import fr.themsou.document.render.display.PageRenderer;
import fr.themsou.document.render.display.PageStatus;
import fr.themsou.main.Main;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import fr.themsou.windows.MainWindow;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

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
            MainWindow.mainScreen.addPage(page);
            pages.add(page);
        }
        pages.get(0).updatePosition(30);
        updateShowsStatus();
    }
    public void updateShowsStatus(){
        for(PageRenderer page : pages){
            page.updateShowStatus();
        }
    }
    public void updateZoom(){
        for(PageRenderer page : pages){
            page.updateZoom();
        }
    }
    public void updateBackgrounds(){
        for(PageRenderer page : pages){
            page.setStatus(PageStatus.HIDE);
        }
        updateShowsStatus();
    }

    public void loadEdition(){
        this.edition = new Edition(file, this);
        if(!documentSaver.isAlive()) documentSaver.start();
    }
    public void close(){
        pdfPagesRender.close();
        for(int i = 0 ; i < totalPages ; i++){
            pages.get(i).remove();
        }
        pages.clear();
    }

    public PageRenderer getPreciseMouseCurrentPage(){
        for(PageRenderer page : pages){
            double bottomY = page.getBottomY();
            if(MainWindow.mainScreen.mouseY < bottomY){
                return page;
            }
        }
        return null;
    }

    public boolean save(){

        if(Edition.isSave()){
            return true;
        }

        if(Main.settings.isAutoSave()){
            edition.save();
        }else{
            Alert alert = Builders.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Édition non sauvegardée"));
            alert.setHeaderText(TR.tr("L'édition du document n'est pas enregistrée."));
            alert.setContentText(TR.tr("Voulez-vous l'enregistrer ?"));
            ButtonType yesButton = new ButtonType(TR.tr("Oui"), ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(TR.tr("Non"), ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType(TR.tr("Annuler"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

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
        MainWindow.footerBar.repaint();
    }
}