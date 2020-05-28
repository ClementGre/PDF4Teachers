package fr.themsou.document.render.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.GradeElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.utils.Builders;
import fr.themsou.utils.PlatformTools;
import fr.themsou.utils.TR;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Pattern;

public class ExportRenderer {

    String uri = "";
    public int exportFile(File file, String directory, String prefix, String suffix, String replace, String by, String customName,
                          Boolean erase, boolean mkdirs, boolean onlyEdited, boolean textElements, boolean gradesElements, boolean drawElements) throws Exception {

        File editFile = Edition.getEditFile(file);

        if(onlyEdited && !editFile.exists()) return 2;

        PDDocument doc = PDDocument.load(file);
        new PDStream(doc, new FileInputStream(file), COSName.FLATE_DECODE);
        doc.getDocumentInformation().setModificationDate(Calendar.getInstance());

        TextElementRenderer textElementRenderer = new TextElementRenderer(doc);
        GradeElementRenderer gradeElementRenderer = new GradeElementRenderer(doc);

        Element[] elements = Edition.simpleLoad(editFile);
        for(int pageNumber = 0 ; pageNumber < doc.getNumberOfPages() ; pageNumber++){

            PDPage page = doc.getPage(pageNumber);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            page.setBleedBox(page.getCropBox());

            float startX = page.getBleedBox().getLowerLeftX();
            float startY = page.getBleedBox().getLowerLeftY();
            float pageRealHeight = page.getBleedBox().getHeight();
            float pageRealWidth = page.getBleedBox().getWidth();
            float pageHeight = page.getCropBox().getHeight();
            float pageWidth = page.getCropBox().getWidth();
            // ROTATE PAGES ADAPT
            if(page.getRotation() == 90 || page.getRotation() == 270){
                startY = page.getBleedBox().getLowerLeftX();
                startX = page.getBleedBox().getLowerLeftY();

                pageRealHeight = page.getBleedBox().getWidth();
                pageRealWidth = page.getBleedBox().getHeight();

                pageHeight = page.getCropBox().getWidth();
                pageWidth = page.getCropBox().getHeight();
            }

            // ROTATE PAGES ADAPT
            switch(page.getRotation()){
                case 90: contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealHeight, 0));
                    break;
                case 180: contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealWidth, pageRealHeight));
                    break;
                case 270: contentStream.transform(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), 0, pageRealWidth));
                    break;
            }

            for(Element element : elements){

                if(element.getPageNumber() != pageNumber) continue;

                if(element instanceof TextElement){
                    if(textElements) textElementRenderer.renderElement((TextElement) element, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }else if(element instanceof GradeElement){
                    if(gradesElements) gradeElementRenderer.renderElement((GradeElement) element, contentStream, page, pageWidth, pageHeight, pageRealWidth, pageRealHeight, startX, startY);
                }/*else if(element instanceof DrawElement){
                    if(drawElements)
                }*/
            }

            contentStream.close();
        }



        if(!new File(directory).exists()){
            if(mkdirs){
                new File(directory).mkdirs();
            }else{
                if(PlatformTools.runAndWait(() -> {
                    Alert alert = Builders.getAlert(Alert.AlertType.WARNING, TR.tr("Dossier introuvable"));
                    alert.setHeaderText(TR.tr("Le dossier d'exportation n'existe pas"));
                    alert.setContentText(TR.tr("Créer le dossier, ou modifier la destination ?"));

                    ButtonType yesButton = new ButtonType(TR.tr("Créer le dossier"), ButtonBar.ButtonData.YES);
                    ButtonType cancelButton = new ButtonType(TR.tr("Modifier la destination"), ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(yesButton, cancelButton);

                    Optional<ButtonType> option = alert.showAndWait();
                    if(option.get() == yesButton){
                        new File(directory).mkdirs();
                    }else{
                        return true;
                    }
                    return false;
                })) return 0;

            }
        }

        uri = directory + File.separator+ customName;
        if(customName.isEmpty()){
            uri = directory + File.separator + prefix + file.getName().substring(0, file.getName().length() - 4).replaceAll(Pattern.quote(replace), by) + suffix + ".pdf";
        }

        if(new File(uri).exists() && !erase){
            int i = PlatformTools.runAndWait(() -> {
                Alert alert = Builders.getAlert(Alert.AlertType.WARNING, TR.tr("Fichier déjà existant"));
                alert.setHeaderText(TR.tr("Le fichier de destination") + " \"" + uri.replace(directory + File.separator, "") + "\" " +TR.tr("existe déjà"));
                alert.setContentText(TR.tr("Voulez-vous l'écraser ?"));

                ButtonType yesButton = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.YES);
                ButtonType yesAlwaysButton = new ButtonType(TR.tr("Toujours écraser"), ButtonBar.ButtonData.YES);
                ButtonType renameButton = new ButtonType(TR.tr("Renommer"), ButtonBar.ButtonData.OTHER);
                ButtonType cancelButton = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                ButtonType cancelAllButton = new ButtonType(TR.tr("Tout Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(yesButton, yesAlwaysButton, renameButton, cancelButton, cancelAllButton);

                Optional<ButtonType> option = alert.showAndWait();
                if(option.get() == cancelAllButton){
                    return 0;
                }else if(option.get() == cancelButton){
                    return 2;
                }else if(option.get() == yesAlwaysButton){
                    ExportWindow.erase = true;
                }else if(option.get() == renameButton){
                    int k = 1;
                    String tmpUri = uri;
                    while(new File(tmpUri).exists()){
                        tmpUri = uri.substring(0, uri.length() - 4) + " (" + k + ").pdf";
                        k++;
                    }
                    uri = tmpUri;
                }
                return -1;
            });
            if(i != -1) return i;

        }
        doc.save(uri);
        doc.close();
        return 1;
    }
}
