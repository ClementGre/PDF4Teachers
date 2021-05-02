package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.scene.paint.Color;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GradeElementRenderer{
    
    HashMap<Map.Entry<String, String>, PDFont> fonts = new HashMap<>();
    
    PDDocument doc;
    
    public GradeElementRenderer(PDDocument doc){
        this.doc = doc;
    }
    
    public void renderElement(GradeElement element, PDPageContentStream contentStream, PDPage page, float pageWidth, float pageHeight, float pageRealWidth, float pageRealHeight, float startX, float startY) throws IOException{
        
        if(!element.isShouldVisibleOnExport()) return;
        
        // COLOR
        Color color = element.getColor();
        contentStream.setNonStrokingColor(new java.awt.Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()));
        
        // FONT
        boolean bold = false;
        if(FontUtils.getFontWeight(element.getFont()) == FontWeight.BOLD) bold = true;
        boolean italic = false;
        if(FontUtils.getFontPosture(element.getFont()) == FontPosture.ITALIC) italic = true;
        
        element.setFont(FontUtils.getFont(element.getFont().getFamily(), italic, bold, element.getFont().getSize() / 596.0 * pageWidth));
        
        contentStream.beginText();
        
        // CUSTOM STREAM
        
        Map.Entry<String, String> entry = Map.entry(element.getFont().getFamily(), FontUtils.getDefaultFontFileName(italic, bold));
        
        if(!fonts.containsKey(entry)){
            InputStream is = FontUtils.getFontFile(element.getFont().getFamily(), italic, bold);
            PDType0Font font =  PDType0Font.load(doc, is);
            contentStream.setFont(font, (float) element.getFont().getSize());
            fonts.put(entry, font);
            
        }else{
            contentStream.setFont(fonts.get(entry), (float) element.getFont().getSize());
        }
        
        float bottomMargin = pageRealHeight - pageHeight - startY;
        contentStream.newLineAtOffset(startX + element.getRealX() / Element.GRID_WIDTH * pageWidth, bottomMargin + pageRealHeight - element.getBaseLineY() - element.getRealY() / Element.GRID_HEIGHT * pageHeight);
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
