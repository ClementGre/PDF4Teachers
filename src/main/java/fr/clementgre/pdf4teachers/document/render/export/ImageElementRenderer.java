package fr.clementgre.pdf4teachers.document.render.export;

import fr.clementgre.pdf4teachers.document.editions.elements.Element;
import fr.clementgre.pdf4teachers.document.editions.elements.GraphicElement;
import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.utils.DPIManager;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageElementRenderer{
    
    private final PDDocument doc;
    private final DPIManager dpiManager;
    
    public ImageElementRenderer(PDDocument doc, int dpi){
        this.doc = doc;
        dpiManager = new DPIManager(dpi);
    }
    
    public void renderElement(ImageElement element, PDPageContentStream contentStream, PDPage page, float pageWidth, float pageHeight, float pageRealWidth, float pageRealHeight, float startX, float startY) throws IOException{
        
        dpiManager.initOneCmWidthFromA4Width(pageWidth);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedImage image;
        
        if(element.getRepeatMode() == GraphicElement.RepeatMode.MULTIPLY || element.getRepeatMode() == GraphicElement.RepeatMode.CROP){
            
            int showWidth = dpiManager.getPixelsLength(element.getRealWidth() / Element.GRID_WIDTH * pageWidth);
            int showHeight = dpiManager.getPixelsLength(element.getRealHeight() / Element.GRID_HEIGHT * pageHeight);
    
            javafx.scene.image.Image fxImage = element.renderImage(0, 0);
            if(fxImage == null) return;
            image = SwingFXUtils.fromFXImage(fxImage, null);
            double imageRatio = ((double) image.getWidth()) / image.getHeight();
    
            if(element.getRepeatMode() == GraphicElement.RepeatMode.CROP){
                if(showWidth > showHeight*imageRatio){ // Crop Y
                    image = resizeImage(image, showWidth, (int) (showWidth/imageRatio)).getSubimage(0, 0, showWidth, showHeight);
                }else{ // Crop X
                    image = resizeImage(image, (int) (showHeight*imageRatio), showHeight).getSubimage(0, 0, showWidth, showHeight);
                }
        
            }else{ // MULTIPLY
                if(showWidth > showHeight*imageRatio){ // Multiply X
                    image = resizeImage(image, (int) (showHeight*imageRatio), showHeight);
                    image = multiplyImage(image, true, showWidth, showHeight, imageRatio);
                }else{ // Multiply Y
                    image = resizeImage(image, showWidth, (int) (showWidth/imageRatio));
                    image = multiplyImage(image, false, showWidth, showHeight, imageRatio);
                }
            }
            
        }else{ // Stretch
            javafx.scene.image.Image fxImage = element.renderImage(
                    dpiManager.getPixelsLength(element.getRealWidth() / Element.GRID_WIDTH * pageWidth),
                    dpiManager.getPixelsLength(element.getRealHeight() / Element.GRID_HEIGHT * pageHeight)
            );
            if(fxImage == null) return;
            image = SwingFXUtils.fromFXImage(fxImage, null);
        }
        
        ImageIO.write(image, "png", bos);
        byte[] data = bos.toByteArray();
        
        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, data, element.getImageId());
        
        float bottomMargin = pageRealHeight - pageHeight - startY;
        contentStream.drawImage(pdImage,
                startX + element.getRealX() / Element.GRID_WIDTH * pageWidth,
                bottomMargin + pageRealHeight - (element.getRealHeight() / Element.GRID_HEIGHT * pageHeight) - element.getRealY() / Element.GRID_HEIGHT * pageHeight,
                element.getRealWidth() / Element.GRID_WIDTH * pageWidth,
                element.getRealHeight() / Element.GRID_HEIGHT * pageHeight);
        
    }
    
    private BufferedImage multiplyImage(BufferedImage image, boolean horizontal, int width, int height, double ratio){
        
        int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
        BufferedImage multipliedImage = new BufferedImage(width, height, type);
    
        Graphics2D g2d = multipliedImage.createGraphics();
        
        if(horizontal){
            int imagesWidth = (int) (height * ratio)+1;
            int currentX = 0;
            
            while(currentX < width){
                g2d.drawImage(image, currentX, 0, imagesWidth, height, null);
                currentX += imagesWidth;
            }
        
        }else{ // vertical
    
            int imagesHeight = (int) (width / ratio)+1;
            int currentY = 0;
    
            while(currentY < height){
                g2d.drawImage(image, 0, currentY, width, imagesHeight, null);
                currentY += imagesHeight;
            }
        
        }
    
        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.dispose();
        
        return multipliedImage;
    }
    
    private BufferedImage resizeImage(BufferedImage image, int width, int height){
        
        Image originalImage = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        
        int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
        BufferedImage resizedImage = new BufferedImage(width, height, type);
        
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, width, height, null);
    
        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.dispose();
        
        return resizedImage;

    }
}
