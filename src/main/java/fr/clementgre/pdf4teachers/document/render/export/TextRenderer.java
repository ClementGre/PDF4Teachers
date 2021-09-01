/*
 * Copyright (c) 2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TextRenderer {
    
    private final HashMap<Map.Entry<String, String>, PDType0Font> fonts = new HashMap<>();
    private final PDDocument doc;
    
    public TextRenderer(PDDocument doc){
        this.doc = doc;
    }
    
    public static record TextSpecs(float boundsHeight, float boundsWidth, float bottomMargin, float baseLineY,
                                   float realX, float realY,
                                   String text) {}
    
    public void drawText(PDPageContentStream contentStream, Map.Entry<String, String> fontEntry, TextSpecs textSpecs, PageSpecs pageSpecs) throws IOException{
        
        int lineNumber = textSpecs.text().split("\\n").length;
        double lineHeight = textSpecs.boundsHeight() / lineNumber;
        
        
        contentStream.newLineAtOffset(pageSpecs.startX() + textSpecs.realX() / Element.GRID_WIDTH * pageSpecs.width(),
                textSpecs.bottomMargin() + pageSpecs.realHeight() - textSpecs.baseLineY() - textSpecs.realY() / Element.GRID_HEIGHT * pageSpecs.height());
        
        for(String line : textSpecs.text().split("\\n")){
            try{
                contentStream.showText(line);
            }catch(IllegalArgumentException ignored){
                PDType0Font font = fonts.get(fontEntry);
                
                // Find an "Unknown char" in the UTF attributed interval
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
                    for(char c : line.toCharArray()){
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
        
        
    }
    
    public Map.Entry<String, String> setContentStreamFont(PDPageContentStream contentStream, Font font, float pageWidth) throws IOException{
        boolean bold = FontUtils.getFontWeight(font) == FontWeight.BOLD;
        boolean italic = FontUtils.getFontPosture(font) == FontPosture.ITALIC;
        font = FontUtils.getFont(font.getFamily(), italic, bold, font.getSize() / 596.0 * pageWidth);
        
        contentStream.beginText();
        
        // Add font to content stream
        Map.Entry<String, String> fontEntry = Map.entry(font.getFamily(), FontUtils.getDefaultFontFileName(italic, bold));
        
        if(!fonts.containsKey(fontEntry)){
            InputStream is = FontUtils.getFontFile(font.getFamily(), italic, bold);
            PDType0Font pdFont = PDType0Font.load(doc, is);
            contentStream.setFont(pdFont, (float) font.getSize());
            
            fonts.put(fontEntry, pdFont);
        }else{
            contentStream.setFont(fonts.get(fontEntry), (float) font.getSize());
        }
        
        return fontEntry;
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
