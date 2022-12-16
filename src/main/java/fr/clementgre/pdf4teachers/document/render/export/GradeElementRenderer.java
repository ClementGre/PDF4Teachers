/*
 * Copyright (c) 2020-2022. Clément Grennerat
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
    
    // Returns false if the user cancelled the export process.
    public boolean renderElement(GradeElement element, PDPageContentStream contentStream, PDPage page, PageSpecs ps) throws IOException{
        
        if(!element.isShouldVisibleOnExport()) return true;
        
        TextRenderer.TextSpecs textSpecs = new TextRenderer.TextSpecs(element.getBoundsHeight(), element.getBoundsWidth(), ps.getYTopOrigin(),
                element.getBaseLineY(), (float) element.getRealX(), (float) element.getRealY(), element.getText(), element.getAwtColor(), false, (float) element.getFont().getSize());
        
        // FONT
        // Entry: (Font family | weight and style)
        Map.Entry<String, String> fontEntry = textRenderer.setContentStreamFont(contentStream, element.getFont(), ps.width());
        // DRAW TEXT
        return textRenderer.drawText(page, contentStream, fontEntry, textSpecs, ps);
        
    }
    
}
