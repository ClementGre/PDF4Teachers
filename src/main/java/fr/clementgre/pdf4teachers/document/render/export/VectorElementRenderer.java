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
    
        int safetyPadding = element.getStrokeWidth();
        
        int width = (int) ((element.getRealWidth() / Element.GRID_WIDTH * pageWidth) + safetyPadding*2);
        int height = (int) ((element.getRealHeight() / Element.GRID_HEIGHT * pageHeight) + safetyPadding*2);
        
        PdfBoxGraphics2D g = new PdfBoxGraphics2D(doc, width, height);
        try{
            AffineTransform originalTransform = g.getTransform();
            g.transform(AffineTransform.getTranslateInstance(safetyPadding + element.getSVGPadding(), safetyPadding + element.getSVGPadding()));
    
            Shape shape = SVGUtils.convertToAwtShape(element.getScaledPath(width, height, (float) (safetyPadding + element.getSVGPadding())));
    
            // Fill
            if(element.isDoFill()){
                g.setColor(StyleManager.fxColorToAWT(element.getFill()));
                g.fill(shape);
            }
            
            // Stroke
            g.setStroke(new BasicStroke(element.getStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, element.getStrokeWidth()));
            g.setColor(StyleManager.fxColorToAWT(element.getStroke()));
            g.draw(shape);
            
            // Restore original transform
            g.setTransform(originalTransform);
        }finally{
            g.dispose();
        }
        
        PDFormXObject xForm = g.getXFormObject();
    
        float bottomMargin = pageRealHeight - pageHeight - startY;
        AffineTransform transform = AffineTransform.getTranslateInstance(
                startX + (element.getRealX() / Element.GRID_WIDTH * pageWidth) - safetyPadding,
                bottomMargin + pageRealHeight - (element.getRealHeight() / Element.GRID_HEIGHT * pageHeight) - (element.getRealY() / Element.GRID_HEIGHT * pageHeight) - safetyPadding);
        xForm.setMatrix(transform);
        
        contentStream.drawForm(xForm);
        
    }
}
