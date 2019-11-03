package fr.themsou.document.render.export;

import fr.themsou.document.editions.Edition;
import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;

import java.io.File;
import java.io.InputStream;

public class ExportRenderer {

    public void exportFile(File file) throws Exception {

        File editFile = Edition.getEditFile(file);
        editFile.createNewFile();

        PDDocument doc = PDDocument.load(file);
        PDPage page = doc.getPage(0);

        PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, false);

        PDRectangle pageSize = page.getBleedBox();


        for(Element element : Edition.simpleLoad(editFile)){

            if(element instanceof TextElement){
                TextElement txtElement = (TextElement) element;

                contentStream.beginText();
                contentStream.newLineAtOffset((float) (txtElement.getRealX() / 500.0 * pageSize.getWidth()),
                                              (float) (pageSize.getHeight() - txtElement.getRealY() / 800.0 * pageSize.getHeight()) );

                Color color = (Color) txtElement.getFill();
                contentStream.setNonStrokingColor(new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()));

                boolean bold = false;
                if(TextElement.getFontWeight(txtElement.getFont()) == FontWeight.BOLD) bold = true;
                boolean italic = false;
                if(TextElement.getFontPosture(txtElement.getFont()) == FontPosture.ITALIC) italic = true;

                InputStream fontFile = getClass().getResourceAsStream("/fonts/" + TextElement.getFontPath(txtElement.getFont().getFamily(), italic, bold));
                contentStream.setFont(PDTrueTypeFont.loadTTF(doc, fontFile), (float) txtElement.getRealFont().getSize());

                contentStream.showText(txtElement.getText());

                contentStream.endText();

            }

        }

        contentStream.close();

        doc.save("/home/clement/exported.pdf");
        doc.close();
    }
}
