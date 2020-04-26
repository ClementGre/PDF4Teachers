package fr.themsou.document.render.export;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.NoteElement;
import fr.themsou.document.editions.elements.TextElement;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NoteElementRenderer {

    HashMap<Map.Entry<String, String>, PDFont> fonts = new HashMap<>();

    PDDocument doc;
    public NoteElementRenderer(PDDocument doc){
        this.doc = doc;
    }

    public void renderElement(NoteElement element, PDPageContentStream contentStream, PDPage page, float pageWidth, float pageHeight) throws IOException {

        if(!element.isVisible()) return;

        // COLOR
        Color color = (Color) element.getFill();
        contentStream.setNonStrokingColor(new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()));

        // FONT
        boolean bold = false;
        if (Element.getFontWeight(element.getFont()) == FontWeight.BOLD) bold = true;
        boolean italic = false;
        if (Element.getFontPosture(element.getFont()) == FontPosture.ITALIC) italic = true;
        InputStream fontFile = Element.getFontFile(element.getFont().getFamily(), italic, bold);
        element.setFont(Element.getFont(element.getFont().getFamily(), italic, bold, element.getFont().getSize() / 596.0 * pageWidth));

        contentStream.beginText();

        // ROTATE PAGES ADAPT
        switch(page.getRotation()){
            case 90: contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageHeight, 0));
                break;
            case 180: contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageWidth, pageHeight));
                break;
            case 270: contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), 0, pageWidth));
                break;
        }
        // CUSTOM STREAM

        Map.Entry<String, String> entry = Map.entry(element.getFont().getFamily(), Element.getFontFileName(italic, bold));

        if(!fonts.containsKey(entry)){
            PDFont font = PDTrueTypeFont.loadTTF(doc, fontFile);
            contentStream.setFont(font, (float) element.getFont().getSize());
            fonts.put(entry, font);
        }else{
            contentStream.setFont(fonts.get(entry), (float) element.getFont().getSize());
        }

        contentStream.newLineAtOffset(element.getRealX() / Element.GRID_WIDTH * pageWidth, pageHeight - element.getRealY() / Element.GRID_HEIGHT * pageHeight);
        try{
            contentStream.showText(element.getText());
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            System.err.println("Erreur : impossible d'écrire la note : \"" + element.getText() + "\" avec la police " + element.getFont().getFamily());
            System.err.println("Message d'erreur : " + e.getMessage());
        }
        contentStream.endText();

    }

}
