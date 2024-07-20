/*
 * Copyright (c) 2021-2023. Clément Grennerat
 * All rights reserved. You must refer to the licence Apache 2.
 */

package fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory;

import com.drew.imaging.ImageProcessingException;
import fr.clementgre.pdf4teachers.Main;
import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.log.Log;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.PaintTab;
import fr.clementgre.pdf4teachers.utils.FilesUtils;
import fr.clementgre.pdf4teachers.utils.interfaces.CallBack;
import fr.clementgre.pdf4teachers.utils.panes.PaneUtils;
import fr.clementgre.pdf4teachers.utils.sort.SortManager;
import fr.clementgre.pdf4teachers.utils.style.StyleManager;
import fr.clementgre.pdf4teachers.utils.svg.SVGPathIcons;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import jfxtras.styles.jmetro.Style;
import org.controlsfx.control.GridCell;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageGridCell extends GridCell<ImageGridElement>{
    
    public static final int PADDING = 2;
    
    private final ImageView imageView;
    private final DropShadow shadow = new DropShadow();
    
    private final boolean hasContextMenu;
    public ImageGridCell(boolean hasContextMenu){
        this.imageView = new ImageView();
        this.hasContextMenu = hasContextMenu;
        
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
            setOnDragDetected(null);
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
            if(hasContextMenu) setContextMenu(item.getMenu());
            updateTooltip(item);
            
            setGraphic(imageView);
            setOnMouseClicked((e) -> {
                if(e.getButton() == MouseButton.PRIMARY){
                    if(e.getClickCount() >= 2){
                        item.addToDocument(false);
                        updateGalleryAndFavoritesSort();
                    }else if(e.getClickCount() == 1){
                        item.setAsToPlaceElement();
                    }
                }
            });
            setOnDragDetected(e -> {
                Dragboard dragboard = startDragAndDrop(TransferMode.COPY);
                
                Image image = ImageElement.renderImage(item.getImageId(), 100, -1);
                if(image != null) dragboard.setDragView(image, image.getWidth()/2, image.getHeight()/2);
        
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.put(Main.INTERNAL_FORMAT, PaintTab.PAINT_ITEM_DRAG_KEY);
                dragboard.setContent(clipboardContent);
                
                Main.window.requestFocus(); // When dragging from gallery, MainWindow shows up
                
                PaintTab.draggingItem = item;
                e.consume();
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
                Region graphic = SVGPathIcons.generateImage(SVGPathIcons.PLAIN_STAR, color, 0, 16);
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
        }catch(IOException | ImageProcessingException e){ Log.eNotified(e); }
        
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
        }
        int sideMargin = (h - w) / 2;
        return image.getSubimage(0, sideMargin, w, w);
    }
}
