package fr.clementgre.pdf4teachers.document.render.export;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.VectorElement;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.svg.SVGUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;

public class VectorElementRenderer{

    private PDDocument doc;
    
    public VectorElementRenderer(PDDocument doc){
        this.doc = doc;
    }
    
    public void renderElement(VectorElement element, PDPageContentStream contentStream, PDPage page, float pageWidth, float pageHeight, float pageRealWidth, float pageRealHeight, float startX, float startY) throws IOException{
        
        
        if(element.getRepeatMode() == GraphicElement.RepeatMode.MULTIPLY){
        
        
        
        }else if(element.getRepeatMode() == GraphicElement.RepeatMode.CROP){
        
        
        
        }else{ // Stretch
        
        }
    
        int width = (int) ((element.getRealWidth()) / Element.GRID_WIDTH * pageWidth);
        int height = (int) ((element.getRealHeight()) / Element.GRID_HEIGHT * pageHeight);
        
        PdfBoxGraphics2D graphics = new PdfBoxGraphics2D(doc, width, height);
        try {
            graphics.setStroke(new BasicStroke(element.getStrokeWidth()));
            graphics.setColor(StyleManager.fxColorToAWT(element.getStroke()));
            graphics.setBackground(Color.BLACK);
            graphics.draw(SVGUtils.convertToAwtShape(
                    element.getScaledPath(width, height)));
        } finally {
            graphics.dispose();
        }
        
        PDFormXObject xForm = graphics.getXFormObject();
    
        float bottomMargin = pageRealHeight - pageHeight - startY;
        AffineTransform transform = AffineTransform.getTranslateInstance(
                startX + element.getRealX() / Element.GRID_WIDTH * pageWidth,
                bottomMargin + pageRealHeight - (element.getRealHeight() / Element.GRID_HEIGHT * pageHeight) - element.getRealY() / Element.GRID_HEIGHT * pageHeight);
        xForm.setMatrix(transform);
        
        contentStream.drawForm(xForm);
        
    }
}
