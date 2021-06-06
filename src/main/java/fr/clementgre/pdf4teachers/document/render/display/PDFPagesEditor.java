package fr.clementgre.pdf4teachers.document.render.display;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ButtonPosition;
import fr.clementgre.pdf4teachers.document.Document;
import fr.clementgre.pdf4teachers.document.editions.Edition;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertWindow;
import fr.clementgre.pdf4teachers.document.render.convert.ConvertedFile;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.AlreadyExistDialogManager;
import fr.clementgre.pdf4teachers.utils.dialogs.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.dialogs.FIlesChooserManager;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ComboBoxDialog;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ConfirmAlert;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListAction;
import fr.clementgre.pdf4teachers.utils.interfaces.TwoStepListInterface;
import fr.clementgre.pdf4teachers.utils.objects.PositionDimensions;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class PDFPagesEditor{
    
    private final PDDocument document;
    private final File file;
    
    public PDFPagesEditor(PDDocument document, File file){
        this.document = document;
        this.file = file;
    }
    
    public void ascendPage(PageRenderer page){
        movePage(page, -1);
    }
    
    public void descendPage(PageRenderer page){
        movePage(page, +1);
    }
    public void movePage(PageRenderer page, int index){
        assert index != 0 : "You can't move a page with an index of 0, this means to not move the page.";
        PDPage docPage = document.getPage(page.getPage());
        
        document.removePage(docPage);
        addDocumentPage(page.getPage() + index, docPage);
        try{
            document.save(file);
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Document document = MainWindow.mainScreen.document;
        
        // move page
        document.getPages().remove(page);
        document.getPages().add(page.getPage() + index, page);
        
        // Update pages of all pages
        for(int i = 0; i < document.totalPages; i++) document.getPage(i).setPage(i);
        
        // update coordinates of the pages
        document.getPages().get(0).updatePosition(30, true);
        document.updateShowsStatus();
        
        // update current page
        document.setCurrentPage(page.getPage() + index);
    }
    
    public void rotateLeftPage(PageRenderer page, boolean animated){
        document.getPage(page.getPage()).setRotation(document.getPage(page.getPage()).getRotation() - 90);
        try{
            document.setAllSecurityToBeRemoved(true);
            document.save(file);
        }catch(IOException e){
            e.printStackTrace();
        }
        
        if(!animated){
            page.updatePosition((int) page.getTranslateY(), true);
            page.updateRender();
        }else{
            Timeline timeline = new Timeline(60);
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(page.rotateProperty(), -90))
            );
            timeline.play();
            
            AtomicBoolean timelineFinished = new AtomicBoolean(false);
            AtomicBoolean renderFinished = new AtomicBoolean(false);
            timeline.setOnFinished((e) -> {
                timelineFinished.set(true);
                if(renderFinished.get()) endRotateAnimation(page, timeline);
            });
            page.updateRenderAsync(() -> {
                renderFinished.set(true);
                if(timelineFinished.get()) endRotateAnimation(page, timeline);
            }, false);
        }
        
        
    }
    
    public void rotateRightPage(PageRenderer page, boolean animated){
        document.getPage(page.getPage()).setRotation(document.getPage(page.getPage()).getRotation() + 90);
        try{
            document.setAllSecurityToBeRemoved(true);
            document.save(file);
        }catch(IOException e){
            e.printStackTrace();
        }
        if(!animated){
            page.updatePosition((int) page.getTranslateY(), true);
            page.updateRender();
        }else{
            Timeline timeline = new Timeline(60);
            timeline.getKeyFrames().clear();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(200), new KeyValue(page.rotateProperty(), 90))
            );
            timeline.play();
            
            AtomicBoolean timelineFinished = new AtomicBoolean(false);
            AtomicBoolean renderFinished = new AtomicBoolean(false);
            timeline.setOnFinished((e) -> {
                timelineFinished.set(true);
                if(renderFinished.get()) endRotateAnimation(page, timeline);
            });
            page.updateRenderAsync(() -> {
                renderFinished.set(true);
                if(timelineFinished.get()) endRotateAnimation(page, timeline);
            }, false);
        }
    }
    private void endRotateAnimation(PageRenderer page, Timeline timeline){
        timeline.setOnFinished(null);
        timeline.stop();
        page.setRotate(0);
        page.updatePosition((int) page.getTranslateY(), true);
    }
    
    public void deletePage(PageRenderer page){
        
        if(MainWindow.mainScreen.document.save() && Edition.isSave()){
            ConfirmAlert alert = new ConfirmAlert(true, TR.tr("document.pageActions.delete.confirmationDialog.header", (page.getPage() + 1)));
            
            if(alert.execute()){
                document.removePage(page.getPage());
                try{
                    document.setAllSecurityToBeRemoved(true);
                    document.save(file);
                }catch(IOException e){
                    e.printStackTrace();
                }
                
                int pageNumber = page.getPage();
                
                // remove page elements
                while(page.getElements().size() != 0){
                    if(page.getElements().get(0) instanceof GradeElement grade){
                        grade.setValue(-1);
                        grade.switchPage(pageNumber == 0 ? 1 : pageNumber - 1);
                    }else{
                        page.getElements().get(0).delete(true);
                    }
                }
                Document document = MainWindow.mainScreen.document;
                // remove page
                page.remove();
                document.totalPages--;
                document.getPages().remove(pageNumber);
                MainWindow.mainScreen.pane.getChildren().remove(page);
                
                // Update pages of all pages
                for(int i = 0; i < document.totalPages; i++) document.getPage(i).setPage(i);
                
                // update coordinates of the pages
                document.getPage(0).updatePosition(30, true);
                document.updateShowsStatus();
                
                // update current page
                document.setCurrentPage(document.totalPages == pageNumber ? pageNumber - 1 : pageNumber);
                
                Edition.setUnsave("DeletePage");
                document.edition.save();
            }
        }
    }
    
    public void newBlankPage(int originalPage, int index){
        PageRenderer page = new PageRenderer(index);
        PDPage docPage = new PDPage(MainWindow.mainScreen.document.pdfPagesRender.getPageSize(originalPage));
        
        addDocumentPage(index, docPage);
        try{
            document.setAllSecurityToBeRemoved(true);
            document.save(file);
        }catch(IOException e){
            e.printStackTrace();
        }
        
        Document document = MainWindow.mainScreen.document;
        
        // add page
        document.getPages().add(index, page);
        MainWindow.mainScreen.addPage(page);
        document.totalPages++;
        
        // Update pages of all pages
        for(int i = 0; i < document.totalPages; i++) document.getPage(i).setPage(i);
        
        // update coordinates of the pages
        document.getPage(0).updatePosition(30, true);
        document.updateShowsStatus();
        
        // update current page
        document.setCurrentPage(index);
    }
    
    public void newConvertPage(int originalPage, int index){
        
        Document document = MainWindow.mainScreen.document;
        
        new ConvertWindow(MainWindow.mainScreen.document.pdfPagesRender.getPageSize(originalPage), (convertedFiles) -> {
            if(convertedFiles.size() == 0) return;
            ConvertedFile file = convertedFiles.get(0);
            
            PDFMergerUtility merger = new PDFMergerUtility();
            
            int addedPages = file.document.getNumberOfPages();
            try{
                merger.appendDocument(this.document, file.document);
                merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            }catch(IOException e){
                e.printStackTrace();
            }
            
            for(int j = 0; j < addedPages; j++){
                PageRenderer page = new PageRenderer(index);
                
                moveDocumentPage(this.document.getNumberOfPages() - 1, index);
                
                try{
                    this.document.setAllSecurityToBeRemoved(true);
                    this.document.save(this.file);
                }catch(IOException e){
                    e.printStackTrace();
                }
                
                // add page
                document.getPages().add(index, page);
                MainWindow.mainScreen.addPage(page);
                document.totalPages++;
                
                // Update pages of all pages
                for(int k = 0; k < document.totalPages; k++) document.getPage(k).setPage(k);
            }
            
            try{
                file.document.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            
            // update coordinates of the pages
            document.getPage(0).updatePosition(30, true);
            document.updateShowsStatus();
            
            // update current page
            document.setCurrentPage(index);
        });
    }
    
    public void newPdfPage(int index){
        
        Document document = MainWindow.mainScreen.document;
        
        final FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(TR.tr("dialog.file.extensionType.PDF"), "*.pdf"));
        chooser.setTitle(TR.tr("dialog.file.selectFile.title"));
        chooser.setInitialDirectory((new File(MainWindow.userData.lastOpenDir).exists() ? new File(MainWindow.userData.lastOpenDir) : new File(System.getProperty("user.home"))));
        
        File file = chooser.showOpenDialog(Main.window);
        if(file != null){
            if(file.getParentFile().exists()) MainWindow.userData.lastOpenDir = file.getParentFile().getAbsolutePath();
            try{
                PDDocument fileDoc = PDDocument.load(file);
                
                PDFMergerUtility merger = new PDFMergerUtility();
                
                int addedPages = fileDoc.getNumberOfPages();
                try{
                    merger.appendDocument(this.document, fileDoc);
                    merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                }catch(IOException e){
                    e.printStackTrace();
                }
                
                for(int j = 0; j < addedPages; j++){
                    PageRenderer page = new PageRenderer(index);
                    
                    moveDocumentPage(this.document.getNumberOfPages() - 1, index);
                    
                    try{
                        this.document.setAllSecurityToBeRemoved(true);
                        this.document.save(this.file);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    
                    // add page
                    document.getPages().add(index, page);
                    MainWindow.mainScreen.addPage(page);
                    document.totalPages++;
                    
                    // Update pages of all pages
                    for(int k = 0; k < document.totalPages; k++) document.getPage(k).setPage(k);
                }
                
                try{
                    fileDoc.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                
                // update coordinates of the pages
                document.getPage(0).updatePosition(30, true);
                document.updateShowsStatus();
                
                // update current page
                document.setCurrentPage(index);
                
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        
    }
    
    // "UTILS"
    
    private void addDocumentPage(final int index, final PDPage page){
        
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
    
    private void moveDocumentPage(final int from, final int to){
        
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
        Image pageImage;
        List<Image> images = new ArrayList<>();
        PageRenderer page;
        if(pageIndex == -1){
            page = MainWindow.mainScreen.document.getPage(0);
        }else{
            page = MainWindow.mainScreen.document.getPage(pageIndex);
        }
        pageImage = capturePagePreview(page, null);
        images.add(capturePagePreview(page, dimensions));
    
        List<String> definitions = ConvertWindow.definitions;
        definitions.set(0, (pageImage.getWidth() * pageImage.getHeight()) / 1000000d + "Mp (" + TR.tr("document.pageActions.capture.dialog.definitionComboBox.thisDocumentDisplayDefinition") + ")");
        
        ComboBoxDialog<String> alert = new ComboBoxDialog<>(TR.tr("document.pageActions.capture.dialog.title"), TR.tr("document.pageActions.capture.dialog.title"), TR.tr("document.pageActions.capture.dialog.details"));
        alert.setItems(FXCollections.observableList(definitions));
        alert.setSelected(definitions.get(0));
    
        ImageView graphic = new ImageView(images.get(0));
        graphic.setPreserveRatio(true);
        graphic.setFitWidth(600);
        graphic.setFitHeight(250);
        VBox.setMargin(graphic, new Insets(10, 0, 10, 10));
        ((VBox) alert.getDialogPane().getContent()).getChildren().add(graphic);
        
        alert.getButtonTypes().clear();
        alert.addDefaultButton(TR.tr("actions.save"));
        alert.addCancelButton(ButtonPosition.CLOSE);
        if(pageIndex != -1) alert.addRightButton(TR.tr("actions.copyClipboard"));
        
        ButtonPosition buttonPos = alert.getShowAndWaitGetButtonPosition(ButtonPosition.CLOSE);
        if(buttonPos == ButtonPosition.CLOSE) return;
        String choosed = alert.getSelected();
        if(choosed != null){
            int definition = (int) (Double.parseDouble(choosed.split("Mp")[0]) * 1000000);
            
            AlreadyExistDialogManager alreadyExistDialogManager = new AlreadyExistDialogManager(pageIndex == -1);
            new TwoStepListAction<>(true, pageIndex == -1, new TwoStepListInterface<Integer, Map.Entry<File, Integer>>(){
                File exportDir = null;
                
                @Override
                public List<Integer> prepare(boolean recursive){
                    if(recursive){
                        return MainWindow.mainScreen.document.getPages().stream().map(PageRenderer::getPage).collect(Collectors.toList());
                    }else{
                        return Collections.singletonList(pageIndex);
                    }
                }
                
                @Override
                public Map.Entry<Map.Entry<File, Integer>, Integer> sortData(Integer pageIndex, boolean recursive){
                    File file;
                    if(!recursive){
                        if(buttonPos == ButtonPosition.DEFAULT){
                            file = FIlesChooserManager.showSaveDialog(FIlesChooserManager.SyncVar.LAST_GALLERY_OPEN_DIR, MainWindow.mainScreen.document.getFileName() + " (" + (pageIndex + 1) + "-" + MainWindow.mainScreen.document.getPagesNumber() + ").png", TR.tr("dialog.file.extensionType.png"), ".png");
                            if(file == null){
                                return Map.entry(Map.entry(new File(""), pageIndex), TwoStepListAction.CODE_STOP);
                            }
                            exportDir = file.getParentFile();
                        }else{
                            return Map.entry(Map.entry(new File(""), pageIndex), TwoStepListAction.CODE_OK);
                        }
                    }else{
                        if(exportDir == null){
                            exportDir = FIlesChooserManager.showDirectoryDialog(FIlesChooserManager.SyncVar.LAST_GALLERY_OPEN_DIR);
                            if(exportDir == null)
                                return Map.entry(Map.entry(new File(""), pageIndex), TwoStepListAction.CODE_STOP);
                        }
                        file = new File(exportDir.getAbsolutePath() + File.separator + MainWindow.mainScreen.document.getFileName() + " (" + (pageIndex + 1) + "-" + MainWindow.mainScreen.document.getPagesNumber() + ").png");
                    }
                    if(file.exists() && recursive){
                        AlreadyExistDialogManager.ResultType result = alreadyExistDialogManager.showAndWait(file);
                        if(result == AlreadyExistDialogManager.ResultType.SKIP)
                            return Map.entry(Map.entry(file, pageIndex), TwoStepListAction.CODE_SKIP_1);
                        if(result == AlreadyExistDialogManager.ResultType.STOP)
                            return Map.entry(Map.entry(file, pageIndex), TwoStepListAction.CODE_STOP);
                        if(result == AlreadyExistDialogManager.ResultType.RENAME) file = AlreadyExistDialogManager.rename(file);
                    }
                    return Map.entry(Map.entry(file, pageIndex), TwoStepListAction.CODE_OK);
                }
                
                @Override
                public String getSortedDataName(Map.Entry<File, Integer> data, boolean recursive){
                    return data.getKey().getName();
                }
                
                @Override
                public TwoStepListAction.ProcessResult completeData(Map.Entry<File, Integer> data, boolean recursive){
                    PageRenderer page = MainWindow.mainScreen.document.getPage(data.getValue());
                    try{
                        if(buttonPos == ButtonPosition.DEFAULT){
                            BufferedImage image = capturePage(page, dimensions, definition);
                        
                            try{
                                ImageIO.write(image, "png", data.getKey());
                            }catch(IOException e){
                                e.printStackTrace();
                                boolean result = PlatformUtils.runAndWait(() -> new ErrorAlert(TR.tr("dialog.file.saveError.header", FilesUtils.getPathReplacingUserHome(data.getKey())), e.getMessage(), recursive).execute());
                                if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                                if(result) return TwoStepListAction.ProcessResult.STOP;
                                else return TwoStepListAction.ProcessResult.SKIPPED;
                            }
                        }else{ // clipboard copy
                            Image image = capturePageInFXImage(page, dimensions, definition);
                            PlatformUtils.runAndWait(() -> {
                                final Clipboard clipboard = Clipboard.getSystemClipboard();
                                final ClipboardContent content = new ClipboardContent();
    
                                content.putImage(image);
                                clipboard.setContent(content);
                                return null;
                            });
                        }
                        
                    }catch(Exception e){
                        e.printStackTrace();
                        boolean result = PlatformUtils.runAndWait(() -> new ErrorAlert(null, e.getMessage(), recursive).execute());
                        if(!recursive) return TwoStepListAction.ProcessResult.STOP_WITHOUT_ALERT;
                        if(result) return TwoStepListAction.ProcessResult.STOP;
                        else return TwoStepListAction.ProcessResult.SKIPPED;
                    }
                    return TwoStepListAction.ProcessResult.OK;
                }
                
                @Override
                public void finish(int originSize, int sortedSize, int completedSize, HashMap<Integer, Integer> excludedReasons, boolean recursive){
                    if(exportDir == null){
                        MainWindow.footerBar.showAlert(Color.web("#008e00"), Color.WHITE, TR.tr("messages.copied"));
                        return;
                    }
                    String alreadyExistText = !excludedReasons.containsKey(1) ? "" : "\n(" + TR.tr("document.pageActions.capture.completedDialog.ignored.alreadyExisting", excludedReasons.get(1)) + ")";
                    String details = TR.tr("document.pageActions.capture.completedDialog.exported", completedSize, originSize) + alreadyExistText;
                    
                    DialogBuilder.showAlertWithOpenDirButton(TR.tr("document.pageActions.capture.completedDialog.title"), TR.tr("document.pageActions.capture.completedDialog.header"), details, exportDir);
                    
                }
            });
            
        }
    }
    
    private Image capturePagePreview(PageRenderer page, PositionDimensions dimensions){
        if(!page.hasRenderedImage())
            return SwingFXUtils.toFXImage(capturePage(page, dimensions, 200000), null);
        if(dimensions == null){
            return page.getRenderedImage();
        }else{
            Image image = page.getRenderedImage();
            double factor = image.getHeight() / page.getHeight();
            
            int subX = (int) (dimensions.getX() * factor);
            int subY = (int) (dimensions.getY() * factor);
            int subWidth = (int) (dimensions.getWidth() * factor);
            int subHeight = (int) (dimensions.getHeight() * factor);
            
            return new WritableImage(image.getPixelReader(),
                    subX, subY, (int) (subWidth + subX > image.getWidth() ? image.getWidth() - subX : subWidth), (int) (subHeight + subY > image.getHeight() ? image.getHeight() - subY : subHeight));
        }
    }
    
    private BufferedImage capturePage(PageRenderer page, PositionDimensions dimensions, int pixels){ // A4 : 594 : 841
        
        int width = (int) (Math.sqrt(pixels) / (841d / 594d));
        int height = (int) (width * (page.getHeight() / page.getWidth()));
        
        BufferedImage image = MainWindow.mainScreen.document.pdfPagesRender.renderPageBasic(page.getPage(), width, height);
        
        if(dimensions == null){
            return image;
        }else{
            double factor = image.getHeight() / page.getHeight();
            int subX = (int) (dimensions.getX() * factor);
            int subY = (int) (dimensions.getY() * factor);
            int subWidth = (int) (dimensions.getWidth() * factor);
            int subHeight = (int) (dimensions.getHeight() * factor);
            return image.getSubimage(subX, subY, subWidth + subX > image.getWidth() ? image.getWidth() - subX - 1 : subWidth, subHeight + subY > image.getHeight() ? image.getHeight() - subY - 1 : subHeight);
        }
    }
    
    private Image capturePageInFXImage(PageRenderer page, PositionDimensions dimensions, int pixels){ // A4 : 594 : 841
        
        int width = (int) (Math.sqrt(pixels) / (841d / 594d));
        int height = (int) (width * (page.getHeight() / page.getWidth()));
        
        BufferedImage image = MainWindow.mainScreen.document.pdfPagesRender.renderPageBasic(page.getPage(), width, height);
        
        if(dimensions == null){
            return SwingFXUtils.toFXImage(image, null);
        }else{
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            double factor = image.getHeight() / page.getHeight();
            int subX = (int) (dimensions.getX() * factor);
            int subY = (int) (dimensions.getY() * factor);
            int subWidth = (int) (dimensions.getWidth() * factor);
            int subHeight = (int) (dimensions.getHeight() * factor);
            return new WritableImage(fxImage.getPixelReader(),
                    subX, subY, subWidth + subX > image.getWidth() ? image.getWidth() - subX : subWidth, subHeight + subY > image.getHeight() ? image.getHeight() - subY : subHeight);
        }
    }
    
}
