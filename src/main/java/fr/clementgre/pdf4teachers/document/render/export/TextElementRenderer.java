/*
 * Copyright (c) 2019-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.TextElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public record TextElementRenderer(PDDocument doc, TextRenderer textRenderer) {
    
    // Returns false if the user cancelled the export process.
    public boolean renderElement(TextElement element, PDPageContentStream cs, PDPage page, PageSpecs ps) throws IOException{
        
        ////////// LATEX RENDER
        
        if(element.isMath()){
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(element.renderAwtLatex(), "png", bos);
            byte[] data = bos.toByteArray();
            
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, data, element.getLaTeXText());
            
            
            cs.drawImage(pdImage,
                    ps.realXToPDCoo(element.getRealX()),
                    ps.realYToPDCoo(element.getRealY() + ps.layoutYToReal(pdImage.getHeight() / TextElement.RENDER_FACTOR)),
                    ps.layoutWToPDCoo(pdImage.getWidth() / TextElement.RENDER_FACTOR),
                    ps.layoutHToPDCoo(pdImage.getHeight() / TextElement.RENDER_FACTOR));
            
            return true;
        }
        ////////// TEXT RENDER
        
        element.updateText();
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs(element.getBoundsHeight(), element.getBoundsWidth(), ps.getYTopOrigin(),
                element.getBaseLineY(), element.getRealX(), element.getRealY(), element.getTextNodeText(), element.getAwtColor(), element.isURL(), (float) element.getFont().getSize());
        
        // FONT
        // Entry: (Font family | weight and style)
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(cs, element.getFont(), ps.width());
        // DRAW TEXT
        return textRenderer.drawText(page, cs, fontEntry, textSpecs, ps);
        
    }
    
}
