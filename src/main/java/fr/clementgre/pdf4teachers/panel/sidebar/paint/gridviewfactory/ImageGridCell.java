package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import com.drew.imaging.ImageProcessingException;
import fr.clementgre.pdf4teachers.components.menus.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.menus.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialogs.alerts.ConfirmAlert;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import jfxtras.styles.jmetro.Style;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

public class ImageGridCell extends GridCell<ImageGridElement>{
    
    private final ImageView imageView;
    private final DropShadow shadow = new DropShadow();
    
    public static final int PADDING = 2;
    
    public ImageGridCell(){
        this.imageView = new ImageView();
        
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        imageView.fitWidthProperty().bind(widthProperty().subtract(2*PADDING));
        imageView.fitHeightProperty().bind(heightProperty().subtract(2*PADDING));
        imageView.setTranslateX(PADDING);
        //imageView.setTranslateY(PADDING);
        
        shadow.setColor(Color.TRANSPARENT);
        shadow.setSpread(.90);
        shadow.setOffsetY(0);
        shadow.setOffsetX(0);
        shadow.setRadius(2);
        setEffect(shadow);
        
        setOnMouseEntered((e) -> shadow.setColor(Color.web("#0078d7")));
        setOnMouseExited((e) -> shadow.setColor(Color.TRANSPARENT));
    }
    
    @Override
    protected void updateItem(ImageGridElement item, boolean empty) {
        super.updateItem(item, empty);
        if(empty){
            setGraphic(null);
            setOnMouseClicked(null);
            setContextMenu(null);
            imageView.imageProperty().unbind();
            imageView.setImage(null);
        }else{
            
            if(item.getImage() == null){
                if(!item.isRendering()){
                    item.setRendering(true);
                    ((ImageGridView) getGridView()).getExecutor().submit(() -> loadImage(item, ((ImageGridView) getGridView()).getImageRenderSize(), () -> item.setRendering(false)));
                }
            }
            imageView.imageProperty().bind(item.imageProperty());
            setContextMenu(item.getMenu());
            updateTooltip(item);
            
            setGraphic(imageView);
            setOnMouseClicked((e) -> {
                if(e.getButton() == MouseButton.PRIMARY){
                    if(e.getClickCount() >= 2){
                        item.addToDocument();
                        updateGalleryAndFavoritesSort();
                    }else if(e.getClickCount() == 1){
                        item.setAsToPlaceElement();
                    }
                }
            });
        }
        
    }
    
    public static void updateGalleryAndFavoritesSort(){
        if(MainWindow.paintTab.galleryWindow != null && MainWindow.paintTab.galleryWindow.isShowing()){
            SortManager gallerySM = MainWindow.paintTab.galleryWindow.getList().getSortManager();
            if(ShapesGridView.SORT_USE.equals(gallerySM.getSortKey()) || ShapesGridView.SORT_LAST_USE.equals(gallerySM.getSortKey())){
                MainWindow.paintTab.galleryWindow.getList().getSortManager().simulateCall();
            }
        }
        MainWindow.paintTab.favouriteImages.getList().getSortManager().simulateCall();
    }
    
    public void updateTooltip(ImageGridElement item){
        Tooltip tooltip = PaneUtils.genWrappedToolTip(FilesUtils.getPathReplacingUserHome(item.getImageIdDirectory()) + File.separator + item.getImageIdFileName());
        tooltip.setShowDelay(new Duration(1000));
        
        tooltip.setOnShowing(e -> {
            if(item.isFavorite()){
                String color = StyleManager.DEFAULT_STYLE == Style.DARK ? "yellow" : "#dbce00";
                Region graphic = SVGPathIcons.generateImage(SVGPathIcons.PLAIN_STAR, color, 0, 16, 16);
                graphic.setPadding(new Insets(0, 5, 0, 0));
                tooltip.setGraphic(graphic);
            }else{
                tooltip.setGraphic(null);
            }
        });
        
        setTooltip(tooltip);
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
        
        BufferedImage cropped = new BufferedImage(renderSize, renderSize, BufferedImage.TYPE_INT_ARGB);
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
