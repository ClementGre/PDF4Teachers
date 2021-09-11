/*
 * Copyright (c) 2020-2021. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.GradeElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.Map;

public record GradeElementRenderer(PDDocument doc, TextRenderer textRenderer) {
    
    public void renderElement(GradeElement element, PDPageContentStream contentStream, PDPage page, PageSpecs pageSpecs) throws IOException{
        
        if(!element.isShouldVisibleOnExport()) return;
        
        
        float bottomMargin = pageSpecs.realHeight() - pageSpecs.height() - pageSpecs.startY();
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs(element.getBoundsHeight(), element.getBoundsWidth(), bottomMargin,
                element.getBaseLineY(), element.getRealX(), element.getRealY(), element.getText(), element.getAwtColor(), false);
        
        // FONT
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(contentStream, element.getFont(), pageSpecs.width());
        // DRAW TEXT
        textRenderer.drawText(page, contentStream, fontEntry, textSpecs, pageSpecs);
        
    }
    
}
