package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.document.editions.elements.ImageElement;
import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.gallery.GalleryWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ImageListPane extends ListPane<ImageGridElement>{

    private final ImageGridView list = new ImageGridView(true,300, zoomSlider);
    
    public ImageListPane(){
        super(MainWindow.paintTab);
    }
    
    
    @Override
    protected void setupGraphics(){
        super.setupGraphics();
        root.getChildren().add(list);
        
        list.cellSizeProperty().bindBidirectional(zoomSlider.valueProperty());
        VBox.setVgrow(list, Priority.ALWAYS);
       
        if(isFavouriteImages()){
            list.setupSortManager(sortPanel, ShapesGridView.SORT_USE, ShapesGridView.SORT_USE, ShapesGridView.SORT_LAST_USE);
            setupMenu(list);
        }else if(isGallery()){
            list.setupSortManager(sortPanel, ShapesGridView.SORT_FILE_EDIT_TIME, ShapesGridView.SORT_FOLDER, ShapesGridView.SORT_FILE_EDIT_TIME);
            Button openGallery = new Button(TR.tr("actions.open"));
            openGallery.setPadding(new Insets(0, 4, 0, 4));
            PaneUtils.setHBoxPosition(openGallery, 0, 26, new Insets(0, 7, 0, 0));
            graphics.getChildren().add(2, openGallery);
            
            openGallery.setOnAction((e) -> paintTab.openGallery());
        }
    
        expandedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && !isLoaded()){
                if(isGallery()){
                    reloadGalleryImageList();
                }else{
                    list.updateItemsFiltered();
                }
                setLoaded(true);
            }
        });
        
        if(isFavouriteImages()) setEmptyMessage(TR.tr("paintTab.favouriteImages.emptyList"));
        else if(isGallery()) setEmptyMessage(TR.tr("paintTab.gallery.emptyList"));
        list.getItems().addListener((InvalidationListener) o -> updateMessage());
        updateMessage();
    }
    
    private void updateMessage(){
        if(list.getAllItems().isEmpty()){
            list.setMaxHeight(0);
        }else{
            list.setMaxHeight(Double.MAX_VALUE);
        }
        super.updateMessage(list.getAllItems().isEmpty());
    }
    
    public void reloadGalleryImageList(){
        if(paintTab.galleryWindow != null){
            list.setItems(paintTab.galleryWindow.getListItems());
        }else{
            list.setItems(GalleryWindow.getImages(list));
        }
    }
    public void reloadFavouritesImageList(ArrayList<ImageData> images, boolean updateVisual){
        list.setItems(images.stream()
                .filter((imageData) -> new File(imageData.getImageId()).exists())
                .map((i) -> new ImageGridElement(i, list))
                .toList(), updateVisual);
    }
    public ImageData toggleFavoriteImage(ImageElement element){
        // If image is already favorite
        for(ImageGridElement gridElement : MainWindow.paintTab.favouriteImages.getList().getAllItems()){
            if(gridElement.getImageId().equals(element.getImageId())){
                gridElement.toggleFavorite();
                return null;
            }
        }
        // Else, create new ImageGrid element
        ImageData linkedImageData = new ImageData(element.getImageId(), element.getRealWidth(), element.getRealHeight(), element.getRepeatMode(), element.getResizeMode(), 0, 0);
        MainWindow.paintTab.favouriteImages.getList().addItems(Collections.singletonList(new ImageGridElement(linkedImageData, MainWindow.paintTab.favouriteImages.getList())));
        return linkedImageData;
    }
    public boolean isFavoriteImage(ImageElement element){
        for(ImageGridElement gridElement : MainWindow.paintTab.favouriteImages.getList().getAllItems()){
            if(gridElement.getImageId().equals(element.getImageId())) return true;
        }
        return false;
    }
    
    
    @Override
    public void updateGraphics(){
        list.getSortManager().updateGraphics();
    }
    
    @Override
    public ImageGridView getList(){
        return list;
    }
    
}
