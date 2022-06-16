/*
 * Copyright (c) 2019-2022. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
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
    public boolean renderElement(TextElement element, PDPageContentStream contentStream, PDPage page, PageSpecs pageSpecs) throws IOException{
        
        ////////// LATEX RENDER
        
        if(element.isMath()){
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(element.renderAwtLatex(), "png", bos);
            byte[] data = bos.toByteArray();
            
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, data, element.getLaTeXText());
            
            float bottomMargin = pageSpecs.realHeight() - pageSpecs.height() - pageSpecs.startY();
            contentStream.drawImage(pdImage,
                    pageSpecs.startX() + element.getRealX() / Element.GRID_WIDTH * pageSpecs.width(),
                    (float) (bottomMargin + pageSpecs.realHeight() - ((pdImage.getHeight() / TextElement.RENDER_FACTOR) / 596.0 * pageSpecs.width()) - element.getRealY() / Element.GRID_HEIGHT * pageSpecs.height()),
                    (float) ((pdImage.getWidth() / TextElement.RENDER_FACTOR) / 596.0 * pageSpecs.width()),
                    (float) ((pdImage.getHeight() / TextElement.RENDER_FACTOR) / 596.0 * pageSpecs.width()));
            
            return true;
        }
        ////////// TEXT RENDER
        
        element.updateText();
        float bottomMargin = pageSpecs.realHeight() - pageSpecs.height() - pageSpecs.startY();
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs(element.getBoundsHeight(), element.getBoundsWidth(), bottomMargin,
                element.getBaseLineY(), element.getRealX(), element.getRealY(), element.getTextNodeText(), element.getAwtColor(), element.isURL());
        
        // FONT
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(contentStream, element.getFont(), pageSpecs.width());
        // DRAW TEXT
        return textRenderer.drawText(page, contentStream, fontEntry, textSpecs, pageSpecs);
        
    }
    
}
