package fr.clementgre.pdf4teachers.panel.sidebar.paint.lists;

import fr.clementgre.pdf4teachers.interfaces.windows.MainWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.gallery.GalleryWindow;
import fr.clementgre.pdf4teachers.interfaces.windows.language.TR;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridElement;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ImageGridView;
import fr.clementgre.pdf4teachers.panel.sidebar.paint.gridviewfactory.ShapesGridView;
import fr.clementgre.pdf4teachers.utils.PaneUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

import java.util.ArrayList;

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
       
        if(isFavouriteImages()){
            list.setupSortManager(sortPanel, ShapesGridView.SORT_USE, ShapesGridView.SORT_USE, ShapesGridView.SORT_LAST_USE);
        }else if(isGallery()){
            list.setupSortManager(sortPanel, ShapesGridView.SORT_FILE_EDIT_TIME, ShapesGridView.SORT_FOLDER, ShapesGridView.SORT_FILE_EDIT_TIME);
            Button openGallery = new Button(TR.tr("actions.open"));
            PaneUtils.setHBoxPosition(openGallery, 0, 26, new Insets(0, 7, 0, 0));
            graphics.getChildren().add(2, openGallery);
            
            openGallery.setOnAction((e) -> paintTab.openGallery());
        }
    
        expandedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && !isLoaded()){
                if(isGallery()){
                    reloadGalleryImageList();
                }
                setLoaded(true);
            }
        });
    }
    
    public void reloadGalleryImageList(){
        if(paintTab.galleryWindow != null){
            list.setItems(paintTab.galleryWindow.getListItems());
        }else{
            list.setItems(GalleryWindow.getImages());
        }
    }
    public void reloadFavouritesImageList(ArrayList<ImageData> images){
        list.setItems(images.stream().map(ImageGridElement::new).toList());
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
