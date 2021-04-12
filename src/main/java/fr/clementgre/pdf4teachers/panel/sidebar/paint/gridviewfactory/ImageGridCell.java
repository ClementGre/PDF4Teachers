package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import com.drew.imaging.ImageProcessingException;
import fr.clementgre.pdf4teachers.components.NodeMenuItem;
import fr.clementgre.pdf4teachers.components.NodeRadioMenuItem;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import fr.clementgre.pdf4teachers.utils.PlatformUtils;
import fr.clementgre.pdf4teachers.utils.dialog.DialogBuilder;
import fr.clementgre.pdf4teachers.utils.image.SVGPathIcons;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.controlsfx.control.GridCell;

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
    
    private final ContextMenu menu = new ContextMenu();
    private final NodeRadioMenuItem isFavoriteItem = new NodeRadioMenuItem(new HBox(), TR.tr("gallery.imageContextMenu.isFavorite"), false, true, true);
    private final NodeMenuItem addItem = new NodeMenuItem(new HBox(), TR.tr("gallery.imageContextMenu.addOnCurentDocument"), false);
    private final NodeMenuItem openItem = new NodeMenuItem(new HBox(), TR.tr("gallery.imageContextMenu.openFileInExplorer"), false);
    private final NodeMenuItem deleteItem = new NodeMenuItem(new HBox(), TR.tr("gallery.imageContextMenu.deleteFile"), false);
    
    public static final int PADDING = 2;
    
    private final ImageGridView gridView;
    public ImageGridCell(ImageGridView gridView){
        this.gridView = gridView;
        this.imageView = new ImageView();
        
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        imageView.fitWidthProperty().bind(widthProperty().subtract(2*PADDING));
        imageView.fitHeightProperty().bind(heightProperty().subtract(2*PADDING));
        imageView.setTranslateX(PADDING);
        //imageView.setTranslateY(PADDING);
        
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
    
        menu.getItems().setAll(addItem, isFavoriteItem, new SeparatorMenuItem(), openItem, deleteItem);
        
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
            setContextMenu(menu);
            menu.setOnShowing((e) -> {
                isFavoriteItem.setSelected(item.isFavorite());
                addItem.setDisable(!MainWindow.mainScreen.hasDocument(false));
                
                isFavoriteItem.setOnAction((event) -> {
                    item.toggleFavorite();
                });
                addItem.setOnAction((event) -> {
                    item.addToDocument();
                    updateGalleryAndFavoritesSort();
                });
                openItem.setOnAction((event) -> {
                    PlatformUtils.openDirectory(item.getImageIdDirectory());
                });
                deleteItem.setOnAction((event) -> {
                    if(DialogBuilder.showConfirmationDialog(true, TR.tr("dialog.confirmation.deleteFile.header", item.getImageIdFileName()))){
                        if(new File(item.getImageId()).delete()){
                            gridView.removeItems(Collections.singletonList(item));
                        }else{
                            System.err.println("Unable to delete file " + item.getImageId());
                        }
                    }
                });
            });
            updateTooltip(item);
            
            setGraphic(imageView);
            setOnMouseClicked((e) -> {
                if(e.getClickCount() >= 2){
                    item.addToDocument();
                    updateGalleryAndFavoritesSort();
                }else if(e.getClickCount() == 1){
                    item.setAsToPlaceElement();
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
            MainWindow.paintTab.favouriteImages.getList().getSortManager().simulateCall();
        }
    }
    
    public void updateTooltip(ImageGridElement item){
        Tooltip tooltip = PaneUtils.genToolTip(FilesUtils.getPathReplacingUserHome(item.getImageIdDirectory()) + File.separator + item.getImageIdFileName());
        tooltip.setShowDelay(Duration.ZERO);
        
        tooltip.setOnShowing(e -> {
            if(item.isFavorite()){
                Region graphic = SVGPathIcons.generateImage(SVGPathIcons.PLAIN_STAR, "yellow", 0, 16, 16);
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
