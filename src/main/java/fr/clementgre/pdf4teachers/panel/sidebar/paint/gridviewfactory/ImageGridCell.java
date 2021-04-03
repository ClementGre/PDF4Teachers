package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import fr.clementgre.pdf4teachers.utils.image.ExifUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.controlsfx.control.GridCell;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageGridCell extends GridCell<ImageGridElement>{
    
    private final ImageView imageView;
    private final DropShadow shadow = new DropShadow();
    
    public static final int PADDING = 2;
    
    private final ImageGridView gridView;
    public ImageGridCell(ImageGridView gridView){
        this.gridView = gridView;
        this.imageView = new ImageView();
        
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        imageView.fitWidthProperty().bind(widthProperty().subtract(2*PADDING));
        imageView.fitHeightProperty().bind(heightProperty().subtract(2*PADDING));
        
        shadow.setColor(Color.web("#0078d7"));
        shadow.setSpread(.90);
        shadow.setOffsetY(0);
        shadow.setOffsetX(0);
        shadow.setRadius(0);
        setEffect(shadow);
        
        setOnMouseEntered((e) -> {
            shadow.setRadius(2);
        });
        setOnMouseExited((e) -> {
            shadow.setRadius(0);
        });
        
    }
    
    @Override
    protected void updateItem(ImageGridElement item, boolean empty) {
        super.updateItem(item, empty);
        
        if(empty){
            setGraphic(null);
            setOnMouseClicked(null);
        }else{
            
            if(item.getImage() == null){
                if(!item.isRendering()){
                    item.setRendering(true);
                    gridView.getExecutor().submit(() -> loadImage(item, gridView.getImageRenderSize(), () -> item.setRendering(false)));
                }
            }
            imageView.imageProperty().bind(item.imageProperty());
            
            setGraphic(imageView);
            setOnMouseClicked((e) -> {
                System.out.println("clicked on " + item.getImageIdDirectory() + " -> " + item.getImageIdFileName());
            });
        }
    }
    
    // IMAGE RENDER
    
    private static void loadImage(ImageGridElement image, int renderSize, CallBack callBack){
        if(image == null) return;
        
        try{
            image.setImage(getImageCropped(image, renderSize));
        }catch(IOException | ImageProcessingException e){ e.printStackTrace(); }
        
        Platform.runLater(callBack::call);
    }
    
    private static Image getImageCropped(ImageGridElement image, int renderSize) throws IOException, ImageProcessingException{
        File file = new File(image.getImageId());
        
        BufferedImage cropped = new BufferedImage(renderSize, renderSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = cropped.createGraphics();
    
        image.getExifData().getRotation().applyTransformToGraphics2D(g, renderSize, renderSize);
        g.drawImage(getImageSquare(ImageIO.read(new FileInputStream(file))),
                    0, 0, renderSize, renderSize, null);
        
        g.dispose();
        return SwingFXUtils.toFXImage(cropped, null);
    }
    private static BufferedImage getImageSquare(BufferedImage image){
        int w = image.getWidth();
        int h = image.getHeight();
        if(w > h){
            int sideMargin = (w - h) / 2;
            return image.getSubimage(sideMargin, 0, h, h);
        }else{
            int sideMargin = (h - w) / 2;
            return image.getSubimage(0, sideMargin, w, w);
        }
    }
}
