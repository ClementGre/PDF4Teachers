package fr.themsou.document.render.export;

import fr.themsou.document.editions.elements.Element;
import fr.themsou.document.editions.elements.TextElement;
import fr.themsou.utils.FontUtils;
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

public class TextElementRenderer {

    HashMap<Map.Entry<String, String>, PDFont> fonts = new HashMap<>();

    PDDocument doc;
    public TextElementRenderer(PDDocument doc){
        this.doc = doc;
    }

    public void renderElement(TextElement element, PDPageContentStream contentStream, PDPage page, float pageWidth, float pageHeight, float pageRealWidth, float pageRealHeight, float startX, float startY) throws IOException {

        // COLOR
        Color color = element.getColor();
        contentStream.setNonStrokingColor(new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()));

        // FONT
        boolean bold = false;
        if (FontUtils.getFontWeight(element.getFont()) == FontWeight.BOLD) bold = true;
        boolean italic = false;
        if (FontUtils.getFontPosture(element.getFont()) == FontPosture.ITALIC) italic = true;
        InputStream fontFile = FontUtils.getFontFile(element.getFont().getFamily(), italic, bold);
        element.setFont(FontUtils.getFont(element.getFont().getFamily(), italic, bold, element.getFont().getSize() / 596.0 * pageWidth));

        // LINE HEIGHT VARIABLES
        double height = element.getAlwaysHeight();
        int lineNumber = element.getText().split("\\n").length;
        double lineHeight = height / lineNumber;

        contentStream.beginText();

        // ROTATE PAGES ADAPT
        switch(page.getRotation()){
            case 90: contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealHeight, 0));
                break;
            case 180: contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), pageRealWidth, pageRealHeight));
                break;
            case 270: contentStream.setTextMatrix(Matrix.getRotateInstance(Math.toRadians(page.getRotation()), 0, pageRealWidth));
                break;
        }
        // CUSTOM STREAM

        Map.Entry<String, String> entry = Map.entry(element.getFont().getFamily(), FontUtils.getFontFileName(italic, bold));

        if(!fonts.containsKey(entry)){
            PDFont font = PDTrueTypeFont.loadTTF(doc, fontFile);
            contentStream.setFont(font, (float) element.getFont().getSize());
            fonts.put(entry, font);
        }else{
            contentStream.setFont(fonts.get(entry), (float) element.getFont().getSize());
        }

        float bottomMargin = pageRealHeight-pageHeight-startY;
        contentStream.newLineAtOffset(startX + element.getRealX() / Element.GRID_WIDTH * pageWidth, (float) (bottomMargin + pageRealHeight - element.getBaseLineY() - element.getRealY() / Element.GRID_HEIGHT * pageHeight));

        // DRAW LINES
        for(String text : element.getText().split("\\n")){

            try{
                contentStream.showText(text);
            }catch(IllegalArgumentException e){
                e.printStackTrace();
                System.err.println("Erreur : impossible d'écrire la ligne : \"" + text + "\" avec la police " + element.getFont().getFamily());
                System.err.println("Message d'erreur : " + e.getMessage());
            }
            contentStream.newLineAtOffset(0, (float) -lineHeight);
        }
        contentStream.endText();

    }

}
