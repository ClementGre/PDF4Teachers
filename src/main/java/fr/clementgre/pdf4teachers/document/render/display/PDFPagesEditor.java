package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertWindow;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertedFile;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.AlreadyExistDialog;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.interfaces.ReturnCallBack;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import fr.clementgre.pdf4teachers.utils.objects.PositionDimensions;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
            Alert alert = DialogBuilder.getAlert(Alert.AlertType.CONFIRMATION, TR.tr("Confirmation"));
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
    public void newPdfPage(int index){

        Document document = MainWindow.mainScreen.document;

        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(TR.tr("Fichier PDF"), "*.pdf"));
        chooser.setTitle(TR.tr("Sélectionner un fichier"));
        chooser.setInitialDirectory(( new File(MainWindow.userData.lastOpenDir).exists() ?  new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home"))));

        File file = chooser.showOpenDialog(Main.window);
        if(file != null){
            if(file.getParentFile().exists()) MainWindow.userData.lastOpenDir = file.getParentFile().getAbsolutePath();
            try{
                PDDocument fileDoc = PDDocument.load(file);

                PDFMergerUtility merger = new PDFMergerUtility();

                int addedPages = fileDoc.getNumberOfPages();
                try{
                    merger.appendDocument(this.document, fileDoc);
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

                try{ fileDoc.close(); }catch(IOException e){ e.printStackTrace(); }

                // update coordinates of the pages
                document.pages.get(0).updatePosition(30);
                document.updateShowsStatus();

                // update current page
                document.setCurrentPage(index);

            }catch(IOException e){ e.printStackTrace(); }
        }

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

    // OTHER

    public void capture(int pageIndex, PositionDimensions dimensions){
        List<Image> images = new ArrayList<>();
        if(pageIndex == -1){
            PageRenderer page = MainWindow.mainScreen.document.pages.get(0);
            images.add(capturePage(page, dimensions));
        }else{
            PageRenderer page = MainWindow.mainScreen.document.pages.get(pageIndex);
            images.add(capturePage(page, dimensions));
        }

        List<String> definitions = ConvertWindow.definitions;
        definitions.set(0, (images.get(0).getWidth() * images.get(0).getHeight()) / 1000000d + "Mp (" + TR.tr("Définition d'affichage du document") + ")");

        ChoiceDialog<String> alert = DialogBuilder.getChoiceDialog(definitions.get(0), definitions);
        alert.setTitle(TR.tr("Capture de pages sous forme d'image"));
        alert.setHeaderText(TR.tr("Capture de pages sous forme d'image"));
        Label contentText = new Label(TR.tr("Choisissez une définition (Le nombre de pixels est basé sur un format A4, la définition varie en fonction de la hauteur de la page)"));

        ImageView graphic = new ImageView(images.get(0));
        graphic.setFitHeight(500);
        graphic.setFitWidth(800);
        graphic.setPreserveRatio(true);

        VBox.setMargin(contentText, new Insets(10, 0, 10, 10));
        VBox.setMargin(graphic, new Insets(10, 0, 10, 10));
        VBox pane = new VBox();
        pane.getChildren().addAll(
                contentText,
                alert.getDialogPane().getContent(),
                graphic);
        alert.getDialogPane().setContent(pane);

        Optional<String> choosed = alert.showAndWait();
        if(choosed.isPresent()){
            int definition = (int) (Double.parseDouble(choosed.get().split("Mp")[0]) * 1000000);

            AlreadyExistDialog alreadyExistDialog = new AlreadyExistDialog(pageIndex == -1);
            new TwoStepListAction<>(true, pageIndex == -1, new TwoStepListInterface<Integer, Map.Entry<File, Integer>>() {
                File exportDir = null;
                @Override
                public List<Integer> prepare(boolean recursive) {
                    if(recursive){
                        return MainWindow.mainScreen.document.pages.stream().map(PageRenderer::getPage).collect(Collectors.toList());
                    }else{
                        return Collections.singletonList(pageIndex);
                    }
                }

                @Override
                public Map.Entry<Map.Entry<File, Integer>, Integer> sortData(Integer pageIndex, boolean recursive) throws IOException, Exception {
                    File file;
                    if(!recursive){
                        file = DialogBuilder.showSaveDialog(false, MainWindow.mainScreen.document.getFileName() + " (" + (pageIndex+1) + "-" + MainWindow.mainScreen.document.pages.size()  + ").png", TR.tr("Fichier PNG"), ".png");
                        if(file == null) return Map.entry(Map.entry(new File(""), pageIndex), TwoStepListAction.CODE_STOP);
                        exportDir = file.getParentFile();

                    }else{
                        if(exportDir == null){
                            exportDir = DialogBuilder.showDirectoryDialog(false);
                            if(exportDir == null) return Map.entry(Map.entry(new File(""), pageIndex), TwoStepListAction.CODE_STOP);
                        }
                        file = new File(exportDir.getAbsolutePath() + File.separator + MainWindow.mainScreen.document.getFileName() + " (" + (pageIndex+1) + "-" + MainWindow.mainScreen.document.pages.size()  + ").png");
                    }
                    if(file.exists() && recursive){
                        AlreadyExistDialog.ResultType result = alreadyExistDialog.showAndWait(file);
                        if(result == AlreadyExistDialog.ResultType.SKIP) return Map.entry(Map.entry(file, pageIndex), TwoStepListAction.CODE_SKIP_1);
                        if(result == AlreadyExistDialog.ResultType.STOP) return Map.entry(Map.entry(file, pageIndex), TwoStepListAction.CODE_STOP);
                        if(result == AlreadyExistDialog.ResultType.RENAME) file = AlreadyExistDialog.rename(file);
                    }
                    return Map.entry(Map.entry(file, pageIndex), TwoStepListAction.CODE_OK);
                }

                @Override
                public String getSortedDataName(Map.Entry<File, Integer> data, boolean recursive) {
                    return data.getKey().getName();
                }

                @Override
                public TwoStepListAction.ProcessResult completeData(Map.Entry<File, Integer> data, boolean recursive) {
                    PageRenderer page = MainWindow.mainScreen.document.pages.get(data.getValue());
                    try{
                        BufferedImage image = capturePage(page, dimensions, definition);
                        try{
                            ImageIO.write(image, "png", data.getKey());
                        }catch(IOException e){
                            e.printStackTrace();
                            boolean result = PlatformUtils.runAndWait(() -> DialogBuilder.showErrorAlert(TR.tr("Impossible d'enregistrer le fichier") + " \"" + data.getKey().getAbsolutePath() + "\"", e.getMessage(), recursive));
                            if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                            if(result) return TwoStepListAction.ProcessResult.STOP;
                            else return TwoStepListAction.ProcessResult.SKIPPED;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                        boolean result = PlatformUtils.runAndWait(() -> DialogBuilder.showErrorAlert(TR.tr("Une erreur est survenue"), e.getMessage(), recursive));
                        if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                        if(result) return TwoStepListAction.ProcessResult.STOP;
                        else return TwoStepListAction.ProcessResult.SKIPPED;
                    }
                    return TwoStepListAction.ProcessResult.OK;
                }

                @Override
                public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive) {
                    Alert endAlert = DialogBuilder.getAlert(Alert.AlertType.INFORMATION, TR.tr("Enregistrement terminée"));
                    ButtonType open = new ButtonType(TR.tr("Ouvrir le dossier"), ButtonBar.ButtonData.YES);
                    endAlert.getButtonTypes().add(open);
                    endAlert.setHeaderText(TR.tr("Les captures ont bien été enregistrées"));

                    String alreadyExistText = !excludedReasons.containsKey(1) ? "" : "\n(" + excludedReasons.get(1) + " " + TR.tr("images ignorées car elles existaient déjà") + ")";
                    endAlert.setContentText(completedSize + "/" + originSize + " " + TR.tr("éditions exportées") + alreadyExistText);

                    Optional<ButtonType> optionSelected = endAlert.showAndWait();
                    if(optionSelected.get() == open){
                        PlatformUtils.openFile(exportDir.getAbsolutePath());
                    }
                }
            });

        }
    }
    private Image capturePage(PageRenderer page, PositionDimensions dimensions){
        if(page.getBackground().getImages().size() == 0) return SwingFXUtils.toFXImage(capturePage(page, dimensions, 200000), null);
        if(dimensions == null){
            return page.getBackground().getImages().get(0).getImage();
        }else{
            Image image = page.getBackground().getImages().get(0).getImage();
            double factor = image.getHeight() / page.getHeight();

            int subX = (int) (dimensions.getX() * factor);
            int subY = (int) (dimensions.getY() * factor);
            int subWidth = (int) (dimensions.getWidth() * factor);
            int subHeight = (int) (dimensions.getHeight() * factor);

            return new WritableImage(image.getPixelReader(),
                    subX, subY, (int) (subWidth+subX > image.getWidth() ? image.getWidth()-subX : subWidth), (int) (subHeight+subY > image.getHeight() ? image.getHeight()-subY : subHeight));
        }
    }
    private BufferedImage capturePage(PageRenderer page, PositionDimensions dimensions, int pixels){ // A4 : 594 : 841

        if(dimensions == null){
            int width = (int) (Math.sqrt(pixels) / (841d / 594d));
            int height = (int) (width * (page.getHeight() / page.getWidth()));

            BufferedImage image = MainWindow.mainScreen.document.pdfPagesRender.renderPageBasic(page.getPage(), width, height);
            return image;
        }else{
            int width = (int) (Math.sqrt(pixels) / (841d / 594d));
            int height = (int) (width * (dimensions.getHeight() / dimensions.getWidth()));

            int renderWidth = (int) (width / dimensions.getWidth() * page.getWidth());
            int renderHeight = (int) (height / dimensions.getHeight() * page.getHeight());
            BufferedImage image = MainWindow.mainScreen.document.pdfPagesRender.renderPageBasic(page.getPage(), renderWidth, renderHeight);

            double factor = image.getHeight() / page.getHeight();
            int subX = (int) (dimensions.getX() * factor);
            int subY = (int) (dimensions.getY() * factor);
            int subWidth = (int) (dimensions.getWidth() * factor);
            int subHeight = (int) (dimensions.getHeight() * factor);
            return image.getSubimage(subX, subY, subWidth+subX > image.getWidth() ? image.getWidth()-subX-1 : subWidth, subHeight+subY > image.getHeight() ? image.getHeight()-subY-1 : subHeight);
        }
    }

}
