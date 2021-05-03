package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TextElementRenderer{
    
    public HashMap<Map.Entry<String, String>, PDType0Font> fonts = new HashMap<>();
    
    PDDocument doc;
    
    public TextElementRenderer(PDDocument doc){
        this.doc = doc;
    }
    
    public void renderElement(TextElement element, PDPageContentStream contentStream, PDPage page, float pageWidth, float pageHeight, float pageRealWidth, float pageRealHeight, float startX, float startY) throws IOException{
        
        ////////// LATEX RENDER
        
        if(element.isLatex()){
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(element.renderAwtLatex(), "png", bos);
            byte[] data = bos.toByteArray();
            
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, data, element.getLaTeXText());
            
            float bottomMargin = pageRealHeight - pageHeight - startY;
            contentStream.drawImage(pdImage,
                    startX + element.getRealX() / Element.GRID_WIDTH * pageWidth,
                    (float) (bottomMargin + pageRealHeight - ((pdImage.getHeight() / TextElement.imageFactor) / 596.0 * pageWidth) - element.getRealY() / Element.GRID_HEIGHT * pageHeight),
                    (float) ((pdImage.getWidth() / TextElement.imageFactor) / 596.0 * pageWidth),
                    (float) ((pdImage.getHeight() / TextElement.imageFactor) / 596.0 * pageWidth));
            
            return;
        }
        ////////// TEXT RENDER
        
        // COLOR
        contentStream.setNonStrokingColor(element.getAwtColor());
        
        // FONT
        boolean bold = false;
        if(FontUtils.getFontWeight(element.getFont()) == FontWeight.BOLD) bold = true;
        boolean italic = false;
        if(FontUtils.getFontPosture(element.getFont()) == FontPosture.ITALIC) italic = true;
        element.setFont(FontUtils.getFont(element.getFont().getFamily(), italic, bold, element.getFont().getSize() / 596.0 * pageWidth));
        
        // LINE HEIGHT VARIABLES
        double height = element.getAlwaysHeight();
        int lineNumber = element.getText().split("\\n").length;
        double lineHeight = height / lineNumber;
        
        contentStream.beginText();
        
        // CUSTOM STREAM
        Map.Entry<String, String> entry = Map.entry(element.getFont().getFamily(), FontUtils.getDefaultFontFileName(italic, bold));
        
        if(!fonts.containsKey(entry)){
            InputStream is = FontUtils.getFontFile(element.getFont().getFamily(), italic, bold);
            PDType0Font font = PDType0Font.load(doc, is);
            contentStream.setFont(font, (float) element.getFont().getSize());
            fonts.put(entry, font);
        }else{
            contentStream.setFont(fonts.get(entry), (float) element.getFont().getSize());
        }
        
        float bottomMargin = pageRealHeight - pageHeight - startY;
        contentStream.newLineAtOffset(startX + element.getRealX() / Element.GRID_WIDTH * pageWidth,
                bottomMargin + pageRealHeight - element.getBaseLineY() - element.getRealY() / Element.GRID_HEIGHT * pageHeight);
        
        // DRAW LINES
        for(String text : element.getText().split("\\n")){
            try{
                contentStream.showText(text);
            }catch(IllegalArgumentException ignored){
                PDType0Font font = fonts.get(entry);
                
                // Find a "Unknown char" in the UTF attributed interval
                char[] replacements = new char[]{'�'};
                if(!canRender(font, '�')){
                    replacements = IntStream.range(0, 1 << 16)
                            .map(c -> canRender(font, c) ? c : '?').filter(c -> Character.toChars(c)[0] == '?')
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString().toCharArray();
                    if(replacements.length == 0) replacements = new char[]{'?'};
                }
                
                try{
                    StringBuilder newText = new StringBuilder();
                    for(char c : text.toCharArray()){
                        if(canRender(font, c)) newText.append(c);
                        else newText.append(replacements[0]);
                    }
                    contentStream.showText(newText.toString());
                    
                }catch(IllegalArgumentException e){
                    e.printStackTrace();
                }
            }
            contentStream.newLineAtOffset(0, (float) -lineHeight);
        }
        contentStream.endText();
        
        if(element.isURL()){
            final PDAnnotationLink txtLink = new PDAnnotationLink();
            txtLink.setColor(ExportRenderer.toPDColor(element.getColor()));
            
            // Border bottom
            final PDBorderStyleDictionary linkBorder = new PDBorderStyleDictionary();
            
            linkBorder.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
            linkBorder.setWidth(1);
            txtLink.setBorderStyle(linkBorder);
            
            // Border color
            final PDRectangle position = new PDRectangle();
            position.setLowerLeftX(startX + element.getRealX() / Element.GRID_WIDTH * pageWidth);
            position.setLowerLeftY((float) (bottomMargin + pageRealHeight - height - element.getRealY() / Element.GRID_HEIGHT * pageHeight));
            position.setUpperRightX(position.getLowerLeftX() + element.getAlwaysWidth());
            position.setUpperRightY((float) (position.getLowerLeftY() + height));
            
            txtLink.setRectangle(position);
            page.getAnnotations().add(txtLink);
            
            PDActionURI action = new PDActionURI();
            action.setURI(element.getText());
            txtLink.setAction(action);
        }
        
    }
    
    // From https://stackoverflow.com/questions/46439548/how-to-ignore-missing-glyphs-in-font-used-by-pdfbox-2-0-7
    private boolean canRender(PDType0Font font, int codepoint){
        try{
            font.getStringWidth(new String(Character.toChars(codepoint)));
            return true;
        }catch(final Exception e){
            return false;
        }
    }
    
}
