/*
 * Copyright (c) 2021-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ErrorAlert;
import fr.clementgre.pdf4teachers.utils.fonts.FontUtils;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TextRenderer {
    
    private final HashMap<Map.Entry<String, String>, PDType0Font> fonts = new HashMap<>();
    private final PDDocument doc;
    
    public TextRenderer(PDDocument doc){
        this.doc = doc;
    }
    
    public record TextSpecs(float boundsHeight, float boundsWidth, float getYTopOrigin, float baseLineY,
                            float realX, float realY, String text, Color color, boolean isURL, float fontSize) {}
    
    // Returns false if the user cancelled the export process.
    public boolean drawText(PDPage page, PDPageContentStream cs, Map.Entry<String, String> fontEntry, TextSpecs ts, PageSpecs ps) throws IOException{
        
        int lineNumber = ts.text().split("\\n").length;
        float lineHeight = ts.boundsHeight() / lineNumber;
        
        cs.setNonStrokingColor(ts.color());
        cs.newLineAtOffset(ps.realXToPDCoo(ts.realX()),
                ps.realYToPDCoo(ts.realY() + ps.layoutYToReal(ts.baseLineY())));
        
        ArrayList<PDRectangle> underlines = new ArrayList<>();
        
        int i = 0;
        for(String line : ts.text().split("\\n")){
            try{
                cs.showText(line);
            }catch(IllegalArgumentException e){
                if(Log.doDebug()) Log.eNotified(e);
                // A character isn't supported by the current font
                
                boolean cancel = PlatformUtils.runAndWait(() -> {
                    ErrorAlert alert = new ErrorAlert(TR.tr("export.missingGlyphError.header", fontEntry.getKey()), e.getMessage(), true);
                    alert.setContentText(TR.tr("export.missingGlyphError.description", line));
                    return alert.getShowAndWaitIsCancelButton();
                });
                if(cancel){
                    cs.endText();
                    return false;
                }
                
                
                PDType0Font font = fonts.get(fontEntry);
                // Find an "Unknown char" in the Unicode attributed interval
                char[] replacements = {'�'};
                if(!canRender(font, '�')){
                    replacements = IntStream.range(0, 1 << 16)
                            .map(c -> canRender(font, c) ? c : '?').filter(c -> Character.toChars(c)[0] == '?')
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString().toCharArray();
                    if(replacements.length == 0) replacements = new char[]{'?'};
                }
    
                try{
                    // Add chars one by one, only if they can print.
                    StringBuilder newText = new StringBuilder();
                    for(char c : line.toCharArray()){
                        if(canRender(font, c)) newText.append(c);
                        else newText.append(replacements[0]);
                    }
                    cs.showText(newText.toString());
        
                }catch(IllegalArgumentException ex){
                    Log.eNotified(ex);
                }
            }
            
            if(ts.isURL()){

                // NO Border effect | PS: the rotation does not rotate the STYLE_UNDERLINE on all client (only Adobe).
                // On some clients it will not appear correctly. => This is then made manually using lines.
                final PDBorderStyleDictionary linkBorder = new PDBorderStyleDictionary();
                linkBorder.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
                linkBorder.setWidth(0);

                // Rectangle
                PDRectangle position = new PDRectangle();
                position.setLowerLeftX(ps.realXToPDCoo(ts.realX()));
                position.setLowerLeftY(ps.realYToPDCoo(ts.realY() + ps.layoutYToReal((i+1)*lineHeight - (lineHeight-ts.baseLineY()) * .83f))); // .78f because the underline is between the bound and the baseline
                position.setUpperRightX(ps.realXToPDCoo(ts.realX() + ps.layoutXToReal(ts.boundsWidth())));
                position.setUpperRightY(ps.realYToPDCoo(ts.realY() + ps.layoutYToReal(i*lineHeight)));
                underlines.add(position);
                position = ExportRenderer.transformRectangle(position, ps.rotation());
                
                // Action (link)
                PDActionURI action = new PDActionURI();
                action.setURI(ts.text().replace("\n", "").split(" ")[0]);
                
                // Anotation
                final PDAnnotationLink txtLink = new PDAnnotationLink();
                txtLink.setColor(ExportRenderer.toPDColor(ts.color()));
                txtLink.setBorderStyle(linkBorder);
                txtLink.setRectangle(position);
                txtLink.setAction(action);
    
                page.getAnnotations().add(txtLink);
            }
            
            cs.newLineAtOffset(0, ps.layoutHToPDCoo(-lineHeight));
            i++;
        }
        cs.endText();
    
        cs.setStrokingColor(ts.color());
        cs.setLineWidth(ps.layoutHToPDCoo(ts.fontSize()/21f));
        for(PDRectangle rect : underlines){
            cs.moveTo(rect.getLowerLeftX(), rect.getLowerLeftY());
            cs.lineTo(rect.getUpperRightX(), rect.getLowerLeftY());
            cs.stroke();
        }
        
        return true;
    }
    
    // Entry: (Font family | weight and style)
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
