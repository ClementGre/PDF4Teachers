package fr.themsou.document.render.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.utils.Builders;
import javafx.geometry.VPos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextBoundsType;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

public class ExportRenderer {

    public int exportFile(File file, String directory, String prefix, String suffix, String replace, String by, String customName,
                          boolean erase, boolean mkdirs, boolean textElements, boolean notesElements, boolean drawElements) throws Exception {

        File editFile = Edition.getEditFile(file);
        editFile.createNewFile();

        PDDocument doc = PDDocument.load(file);
        doc.getDocumentInformation().setModificationDate(Calendar.getInstance());

        Element[] elements = Edition.simpleLoad(editFile);

        for(int pageNumber = 0 ; pageNumber < doc.getNumberOfPages() ; pageNumber++){

            PDPage page = doc.getPage(pageNumber);
            PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true, true);
            PDRectangle pageSize = page.getBleedBox();

            for(Element element : elements){

                if(element.getPageNumber() != pageNumber) continue;

                if(element instanceof TextElement){
                    if(!textElements) continue;

                    TextElement txtElement = (TextElement) element;
                    txtElement.setBoundsType(TextBoundsType.LOGICAL);


                    double height = txtElement.getLayoutBounds().getHeight() / 1080 * pageSize.getHeight();
                    int lineNumber = txtElement.getText().split("\\n").length;
                    double lineHeight = height / lineNumber;

                    contentStream.beginText();

                    // Text Style
                    Color color = (Color) txtElement.getFill();
                    contentStream.setNonStrokingColor(new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()));

                    boolean bold = false;
                    if (TextElement.getFontWeight(txtElement.getFont()) == FontWeight.BOLD) bold = true;
                    boolean italic = false;
                    if (TextElement.getFontPosture(txtElement.getFont()) == FontPosture.ITALIC) italic = true;
                    InputStream fontFile = getClass().getResourceAsStream("/fonts/" + TextElement.getFontPath(txtElement.getFont().getFamily(), italic, bold));
                    contentStream.setFont(PDTrueTypeFont.loadTTF(doc, fontFile), (float) txtElement.getRealFont().getSize());

                    contentStream.newLineAtOffset((float) (txtElement.getRealX() / 500.0 * pageSize.getWidth()), (float) (pageSize.getHeight() - (txtElement.getRealY()) / 800.0 * pageSize.getHeight() + height));

                    for(String text : txtElement.getText().split("\\n")){

                        float shiftY = (float) -lineHeight;
                        System.out.println(text + " "  + lineHeight + " " + shiftY);
                        contentStream.newLineAtOffset(0, shiftY);

                        contentStream.showText(text);
                    }
                    contentStream.endText();

                }/*else if(element instanceof NoteElement){
                    if(!notesElements) continue;

                }else if(element instanceof DrawElement){
                    if(!drawElements) continue;

                }*/
            }
            contentStream.close();
        }



        if(!new File(directory).exists()){
            if(mkdirs){
                new File(directory).mkdirs();
            }else{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                alert.setTitle("Dossier introuvable");
                alert.setHeaderText("Le dossier d'exportation n'est pas existant.");
                alert.setContentText("Voulez-vous en le créer ou modifier la destination ?");
                ButtonType yesButton = new ButtonType("Créer le dossier", ButtonBar.ButtonData.YES);
                ButtonType cancelButton = new ButtonType("Modifier la destination", ButtonBar.ButtonData.CANCEL_CLOSE);
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
            uri = directory + File.separator + prefix + file.getName().substring(0, file.getName().length() - 4).replaceAll(replace, by) + suffix + ".pdf";
        }

        if(new File(uri).exists() && !erase){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            new JMetro(alert.getDialogPane(), Style.LIGHT);
            alert.setTitle("Fichier existant");
            alert.setHeaderText("Le fichier de destination \"" + uri.replace(directory + File.separator, "") + "\" existe déjà");
            alert.setContentText("Voulez-vous l'écraser ?");
            ButtonType yesButton = new ButtonType("Écraser", ButtonBar.ButtonData.YES);
            ButtonType yesAlwaysButton = new ButtonType("Toujours écraser", ButtonBar.ButtonData.YES);
            ButtonType renameButton = new ButtonType("Renomer", ButtonBar.ButtonData.OTHER);
            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType cancelAllButton = new ButtonType("Tout Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yesButton, yesAlwaysButton, renameButton, cancelButton, cancelAllButton);
            Builders.secureAlert(alert);
            Optional<ButtonType> option = alert.showAndWait();
            if(option.get() == cancelAllButton){
                return 0;
            }else if(option.get() == cancelButton){
                return 1;
            }else if(option.get() == yesAlwaysButton){
                mkdirs = true;
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

        if(!new File(directory).exists()){
            if(mkdirs){
                new File(directory).mkdirs();
            }else{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                new JMetro(alert.getDialogPane(), Style.LIGHT);
                alert.setTitle("Dossier introuvable");
                alert.setHeaderText("Le dossier d'exportation n'est pas existant.");
                alert.setContentText("Voulez-vous en le créer ou modifier la destination ?");
                ButtonType yesButton = new ButtonType("Créer le dossier", ButtonBar.ButtonData.YES);
                ButtonType cancelButton = new ButtonType("Modifier la destination", ButtonBar.ButtonData.CANCEL_CLOSE);
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
        doc.save(uri);
        doc.close();
        return 1;
    }
}
