package fr.themsou.document.render.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.utils.Builders;
import fr.themsou.utils.TR;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Pattern;

public class ExportRenderer {

    public int exportFile(File file, String directory, String prefix, String suffix, String replace, String by, String customName,
                          Boolean erase, boolean mkdirs, boolean onlyEdited, boolean textElements, boolean notesElements, boolean drawElements) throws Exception {

        File editFile = Edition.getEditFile(file);

        if(onlyEdited && !editFile.exists()) return 2;

        PDDocument doc = PDDocument.load(file);
        new PDStream(doc, new FileInputStream(file), COSName.FLATE_DECODE);
        doc.getDocumentInformation().setModificationDate(Calendar.getInstance());

        TextElementRenderer textElementRenderer = new TextElementRenderer(doc);
        NoteElementRenderer noteElementRenderer = new NoteElementRenderer(doc);

        Element[] elements = Edition.simpleLoad(editFile);
        for(int pageNumber = 0 ; pageNumber < doc.getNumberOfPages() ; pageNumber++){

            PDPage page = doc.getPage(pageNumber);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);

            float pageHeight = page.getBleedBox().getHeight();
            float pageWidth = page.getBleedBox().getWidth();
            // ROTATE PAGES ADAPT
            if(page.getRotation() == 90 || page.getRotation() == 270){
                pageHeight = page.getBleedBox().getWidth();
                pageWidth = page.getBleedBox().getHeight();
            }

            for(Element element : elements){

                if(element.getPageNumber() != pageNumber) continue;

                if(element instanceof TextElement){
                    if(textElements) textElementRenderer.renderElement((TextElement) element, contentStream, page, pageWidth, pageHeight);
                }else if(element instanceof NoteElement){
                    if(notesElements) noteElementRenderer.renderElement((NoteElement) element, contentStream, page, pageWidth, pageHeight);
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
                Alert alert = new Alert(Alert.AlertType.WARNING);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                alert.setTitle(TR.tr("Dossier introuvable"));
                alert.setHeaderText(TR.tr("Le dossier d'exportation n'est pas existant."));
                alert.setContentText(TR.tr("Voulez-vous en le créer ou modifier la destination ?"));
                ButtonType yesButton = new ButtonType(TR.tr("Créer le dossier"), ButtonBar.ButtonData.YES);
                ButtonType cancelButton = new ButtonType(TR.tr("Modifier la destination"), ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(yesButton, cancelButton);
                Builders.secureAlert(alert);
                Optional<ButtonType> option = alert.showAndWait();
                if(option.get() == yesButton){
                    new File(directory).mkdirs();
                }else{
                    return 0;
                }
            }
        }

        String uri = directory + File.separator+ customName;
        if(customName.isEmpty()){
            uri = directory + File.separator + prefix + file.getName().substring(0, file.getName().length() - 4).replaceAll(Pattern.quote(replace), by) + suffix + ".pdf";
        }

        if(new File(uri).exists() && !erase){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            alert.setTitle(TR.tr("Fichier déjà existant"));
            alert.setHeaderText(TR.tr("Le fichier de destination") + " \"" + uri.replace(directory + File.separator, "") + "\" " +TR.tr("existe déjà"));
            alert.setContentText(TR.tr("Voulez-vous l'écraser ?"));
            ButtonType yesButton = new ButtonType(TR.tr("Écraser"), ButtonBar.ButtonData.YES);
            ButtonType yesAlwaysButton = new ButtonType(TR.tr("Toujours écraser"), ButtonBar.ButtonData.YES);
            ButtonType renameButton = new ButtonType(TR.tr("Renomer"), ButtonBar.ButtonData.OTHER);
            ButtonType cancelButton = new ButtonType(TR.tr("Sauter"), ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType cancelAllButton = new ButtonType(TR.tr("Tout Arrêter"), ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, yesAlwaysButton, renameButton, cancelButton, cancelAllButton);
            Builders.secureAlert(alert);
            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == cancelAllButton){
                return 0;
            }else if(option.get() == cancelButton){
                return 1;
            }else if(option.get() == yesAlwaysButton){
                ExportWindow.erase = true;
            }else if(option.get() == renameButton){
                int i = 1;
                String tmpUri = uri;
                while(new File(tmpUri).exists()){
                    tmpUri = uri.substring(0, uri.length() - 4) + " (" + i + ").pdf";
                    i++;
                }
                uri = tmpUri;
            }
        }
        doc.save(uri);
        doc.close();
        return 1;
    }
}
